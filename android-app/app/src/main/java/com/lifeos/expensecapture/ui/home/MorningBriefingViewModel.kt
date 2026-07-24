package com.lifeos.expensecapture.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val yesterdaySpendLine: String = ""
)

/**
 * Morning Dashboard PRD, Phase 3 Doc 01, scoped drastically down from the full spec: the real
 * PRD calls for cross-pillar candidate-item ranking with a tie-break engine, a Proactivity
 * Ladder, a Context Engine, and a memory-recall service for "why this, why now" justification -
 * none of which exist, and none of which make sense to build for a single rule-based Finance
 * pillar with no ML anywhere in the stack. What's implemented instead is the one testable idea
 * from the PRD's own Success Criteria that still applies at this scope: a synthesized
 * first-open-of-the-day view that leads with the single most relevant thing (an overdue bill
 * beats one due today beats an over-budget category - same precedence as Home's own
 * AttentionItem, reused rather than reinvented), backed by an explicit reason, and an honest
 * "nothing needs attention" state on a genuinely quiet day rather than padding with manufactured
 * content (the PRD's own "low-content composition rule", Section 3).
 *
 * "First open of the day" is a simple date-watermark check, not the full PRD's app-lifecycle/
 * timezone-aware trigger window (Section 7's edge cases) - a traveling user or a midnight-opener
 * may see this fire slightly differently than the full spec intends. Shown once per calendar
 * day regardless of hour, since gating on a strict "morning window" would make the feature
 * invisible to anyone who opens the app for the first time in the afternoon.
 */
class MorningBriefingViewModel(
    private val context: Context,
    transactionDao: TransactionDao,
    insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    private val _dismissed = MutableStateFlow(false)

    val uiState: StateFlow<MorningBriefingUiState> = combine(
        insightsRepository.observeBills(),
        insightsRepository.observeBudgetProgress(),
        transactionDao.observeAll(),
        _dismissed
    ) { bills, budgets, transactions, dismissed ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfYesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val yesterdaySpend = transactions
            .filter { it.date in startOfYesterday until startOfToday && it.direction == TransactionDirection.DEBIT }
            .sumOf { it.amount }

        val overdueBill = bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE }
        val dueTodayBill = bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.DUE_TODAY }
        val overBudget = budgets.firstOrNull { it.spentThisMonth > it.budget.monthlyLimit }

        val leadItem = when {
            overdueBill != null -> "${overdueBill.bill.payeeDisplay} (~₹${"%.2f".format(overdueBill.bill.typicalAmount)}) is overdue - it usually lands around day ${overdueBill.bill.dueDayOfMonth}"
            dueTodayBill != null -> "${dueTodayBill.bill.payeeDisplay} (~₹${"%.2f".format(dueTodayBill.bill.typicalAmount)}) is due today"
            overBudget != null -> "${overBudget.categoryName} is already ₹${"%.2f".format(overBudget.spentThisMonth - overBudget.budget.monthlyLimit)} over budget this month"
            else -> null
        }

        MorningBriefingUiState(
            visible = !dismissed && !alreadyShownToday(),
            leadItem = leadItem,
            yesterdaySpendLine = if (yesterdaySpend > 0) {
                "Yesterday you spent ₹${"%.2f".format(yesterdaySpend)}."
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
