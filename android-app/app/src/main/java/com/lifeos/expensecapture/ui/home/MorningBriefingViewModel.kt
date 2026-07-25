package com.lifeos.expensecapture.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class MorningBriefingUiState(
    val visible: Boolean = false,
    val leadItem: String? = null,
    val homeLine: String? = null,
    val yesterdaySpendLine: String = ""
)

private data class MorningFinanceSnapshot(
    val bills: List<FinanceInsightsRepository.BillWithComputedStatus>,
    val budgets: List<FinanceInsightsRepository.BudgetProgress>,
    val yesterdaySpend: Double
)

private data class MorningHomeSnapshot(
    val dueTodayTaskTitle: String?,
    val pendingHabitCount: Int
)

/**
 * Morning Dashboard PRD, Phase 3 Doc 01, scoped drastically down from the full spec: the real
 * PRD calls for cross-pillar candidate-item ranking with a tie-break engine, a Proactivity
 * Ladder, a Context Engine, and a memory-recall service for "why this, why now" justification -
 * none of which exist, and none of which make sense to build for one rule-based app with no ML
 * anywhere in the stack. What's implemented instead is the one testable idea from the PRD's own
 * Success Criteria that still applies at this scope: a synthesized first-open-of-the-day view
 * that leads with the single most relevant Finance item (an overdue bill beats one due today
 * beats an over-budget category), backed by an explicit reason, and an honest "nothing needs
 * attention" state on a genuinely quiet day rather than padding with manufactured content.
 *
 * `homeLine` is the direct response to a real product gap: this briefing used to only ever
 * mention Finance, even after the Home pillar existed - a proactive surface that stayed silent
 * about half the app. It's a second, lower-priority line (Finance still leads, since a real
 * financial consequence outranks a to-do reminder), and follows the same "don't pad" rule: if
 * there's no task due today and no habit still open, this line is simply absent rather than
 * manufacturing something to say.
 *
 * "First open of the day" is a simple date-watermark check, not the full PRD's app-lifecycle/
 * timezone-aware trigger window - a traveling user or a midnight-opener may see this fire
 * slightly differently than the full spec intends. Shown once per calendar day regardless of
 * hour, since gating on a strict "morning window" would make the feature invisible to anyone who
 * opens the app for the first time in the afternoon.
 */
class MorningBriefingViewModel(
    private val context: Context,
    transactionDao: TransactionDao,
    insightsRepository: FinanceInsightsRepository,
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao
) : ViewModel() {

    private val _dismissed = MutableStateFlow(false)

    private val financeSnapshot = combine(
        insightsRepository.observeBills(),
        insightsRepository.observeBudgetProgress(),
        transactionDao.observeAll()
    ) { bills, budgets, transactions ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfYesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val yesterdaySpend = transactions
            .filter { it.date in startOfYesterday until startOfToday && it.direction == TransactionDirection.DEBIT }
            .sumOf { it.amount }
        MorningFinanceSnapshot(bills, budgets, yesterdaySpend)
    }

    private val homeSnapshot = combine(
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll()
    ) { tasks, habits, completions ->
        val zone = ZoneId.systemDefault()
        val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val dueTodayTask = tasks.firstOrNull { !it.completed && it.dueDate != null && it.dueDate < endOfToday }

        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val doneTodayIds = completions.filter { it.dateEpochDay == todayEpochDay }.map { it.habitId }.toSet()
        val pendingHabitCount = habits.count { it.id !in doneTodayIds }

        MorningHomeSnapshot(dueTodayTask?.title, pendingHabitCount)
    }

    val uiState: StateFlow<MorningBriefingUiState> = combine(
        financeSnapshot, homeSnapshot, _dismissed
    ) { finance, home, dismissed ->
        val overdueBill = finance.bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE }
        val dueTodayBill = finance.bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.DUE_TODAY }
        val overBudget = finance.budgets.firstOrNull { it.spentThisMonth > it.budget.monthlyLimit }

        val leadItem = when {
            overdueBill != null -> "${overdueBill.bill.payeeDisplay} (~₹${"%.2f".format(overdueBill.bill.typicalAmount)}) is overdue - it usually lands around day ${overdueBill.bill.dueDayOfMonth}"
            dueTodayBill != null -> "${dueTodayBill.bill.payeeDisplay} (~₹${"%.2f".format(dueTodayBill.bill.typicalAmount)}) is due today"
            overBudget != null -> "${overBudget.categoryName} is already ₹${"%.2f".format(overBudget.spentThisMonth - overBudget.budget.monthlyLimit)} over budget this month"
            else -> null
        }

        val homeLine = when {
            home.dueTodayTaskTitle != null && home.pendingHabitCount > 0 ->
                "\"${home.dueTodayTaskTitle}\" is due today, and ${home.pendingHabitCount} habit${if (home.pendingHabitCount == 1) "" else "s"} still open."
            home.dueTodayTaskTitle != null -> "\"${home.dueTodayTaskTitle}\" is due today."
            home.pendingHabitCount > 0 -> "${home.pendingHabitCount} habit${if (home.pendingHabitCount == 1) "" else "s"} still open today."
            else -> null
        }

        MorningBriefingUiState(
            visible = !dismissed && !alreadyShownToday(),
            leadItem = leadItem,
            homeLine = homeLine,
            yesterdaySpendLine = if (finance.yesterdaySpend > 0) {
                "Yesterday you spent ₹${"%.2f".format(finance.yesterdaySpend)}."
            } else {
                "No spending captured yesterday."
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MorningBriefingUiState())

    fun dismiss() {
        markShownToday()
        _dismissed.value = true
    }

    private fun prefs() = context.getSharedPreferences("morning_briefing", Context.MODE_PRIVATE)

    private fun alreadyShownToday(): Boolean {
        val lastShown = prefs().getString("last_shown_date", null)
        return lastShown == LocalDate.now(ZoneId.systemDefault()).toString()
    }

    private fun markShownToday() {
        prefs().edit().putString("last_shown_date", LocalDate.now(ZoneId.systemDefault()).toString()).apply()
    }
}
