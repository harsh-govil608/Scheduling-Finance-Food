package com.lifeos.expensecapture.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.ConsentDao
import com.lifeos.expensecapture.data.db.dao.GoalDao
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.NotificationDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.dao.UnparsedMessageDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.finance.SpendingInsightEngine
import com.lifeos.expensecapture.notifications.NotificationCheckWorker
import com.lifeos.expensecapture.productivity.HabitStreakCalculator
import com.lifeos.expensecapture.ui.onboarding.CONSENT_SMS
import com.lifeos.expensecapture.util.ConnectivityObserver
import com.lifeos.expensecapture.util.tickerFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Finance Tracker (Home) PRD, Phase 3 Doc 17. The "needs attention" arbitration rule (Section
 * 8: "when two or more features' signals compete for the single slot") is a fixed precedence
 * here: an overdue bill (real, specific, immediately-actionable) beats a cash-flow risk
 * (real but broader/forward-looking - AI Transformation Plan F1) beats an over-budget category
 * (informational) beats a pending review-queue count (lowest urgency).
 */
sealed class AttentionItem {
    data class OverdueBill(val payee: String, val amount: Double) : AttentionItem()
    /** AI Transformation Plan F1 (cross-module cash-flow guard): Budgets and Bills each already
     * show correct numbers, but never cross-checked against each other - a user could look "on
     * pace" in Budgets while several bills/subscriptions land the same week. Deterministic
     * projection, not a model: known due-dates within `windowDays` versus remaining budget
     * headroom at current pace. */
    data class CashFlowRisk(val upcomingTotal: Double, val availableHeadroom: Double, val windowDays: Int) : AttentionItem()
    data class OverBudget(val categoryName: String, val overspendAmount: Double) : AttentionItem()
    data class UnparsedMessages(val count: Int) : AttentionItem()
}

data class HomeUiState(
    val spentThisMonth: Double = 0.0,
    /** Found via a real user report, 2026-07: the month total alone didn't answer "how much did
     * I spend just today" - same day-boundary math NightSummaryViewModel already uses. */
    val spentToday: Double = 0.0,
    /** Threshold mark for the Last 7 Days trend graph (found via a real user report, 2026-07) -
     * the Overall budget's own monthly limit divided into a daily pace, so the trend line has a
     * reference for "is this an okay day" instead of being purely decorative. Null when no
     * Overall budget is set - there's nothing to project a pace against. */
    val dailySpendThreshold: Float? = null,
    /** Engagement hook (found via a real user request, 2026-07): the best current streak across
     * all habits, surfaced on Home - see HabitStreakCalculator's kdoc. 0 means no active streak
     * (or no habits at all), rendered as "no streak yet" rather than anything discouraging. */
    val bestHabitStreak: Int = 0,
    val attentionItem: AttentionItem? = null,
    val hasAnyData: Boolean = false,
    val unreadNotifications: Int = 0,
    val isOnline: Boolean = true,
    val smsPermissionRevoked: Boolean = false,
    val last7DaysSpend: List<Float> = emptyList(),
    val spendingInsight: SpendingInsightEngine.SpendingInsight? = null
)

/** Intermediate grouping to keep the combine() chain to 2-5 arg overloads instead of unsafe
 * array-casting across 7 heterogeneous flows. */
private data class FinanceSnapshot(
    val transactions: List<com.lifeos.expensecapture.data.db.entity.TransactionEntity>,
    val budgets: List<FinanceInsightsRepository.BudgetProgress>,
    val bills: List<FinanceInsightsRepository.BillWithComputedStatus>,
    val subscriptions: List<FinanceInsightsRepository.SubscriptionWithComputedStatus>,
    val unparsedCount: Int
)

private data class StatusSnapshot(
    val unreadNotifications: Int,
    val isOnline: Boolean,
    val smsConsentedButRevoked: Boolean
)

private data class InsightInputsSnapshot(
    val categories: List<com.lifeos.expensecapture.data.db.entity.CategoryEntity>,
    val goals: List<com.lifeos.expensecapture.data.db.entity.GoalEntity>
)

class HomeViewModel(
    context: Context,
    transactionDao: TransactionDao,
    unparsedMessageDao: UnparsedMessageDao,
    notificationDao: NotificationDao,
    consentDao: ConsentDao,
    categoryDao: CategoryDao,
    goalDao: GoalDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao,
    private val insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    init {
        viewModelScope.launch { insightsRepository.refreshRecurringDetection() }
        // Also runs the notification check on every Home open, not just the 6-hour schedule,
        // so testers see fresh results without waiting.
        NotificationCheckWorker.runOnce(context)
    }

    private val financeSnapshot = combine(
        transactionDao.observeAll(),
        insightsRepository.observeBudgetProgress(),
        insightsRepository.observeBills(),
        insightsRepository.observeSubscriptions(),
        unparsedMessageDao.observeUnresolved()
    ) { transactions, budgets, bills, subscriptions, unparsed ->
        FinanceSnapshot(transactions, budgets, bills, subscriptions, unparsed.size)
    }

    private val statusSnapshot = combine(
        notificationDao.observeUnreadCount(),
        ConnectivityObserver.isOnlineFlow(context),
        consentDao.observeAll()
    ) { unreadCount, isOnline, consents ->
        val smsConsent = consents.firstOrNull { it.permissionType == CONSENT_SMS }
        val smsGrantedNow = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
        StatusSnapshot(
            unreadNotifications = unreadCount,
            isOnline = isOnline,
            smsConsentedButRevoked = smsConsent?.granted == true && !smsGrantedNow
        )
    }

    private val insightInputsSnapshot = combine(
        categoryDao.observeAll(),
        goalDao.observeAll()
    ) { categories, goals -> InsightInputsSnapshot(categories, goals) }

    /** Engagement hook (found via a real user request, 2026-07 - "increase user engagement"):
     * streaks were tracked and shown only inside the Habits screen itself, invisible anywhere
     * you'd actually see it day to day. Reuses HabitStreakCalculator (see its kdoc for why this
     * isn't a second hand-copied version) across every habit and takes the best one. */
    private val bestHabitStreak = combine(
        habitDao.observeAll(),
        habitCompletionDao.observeAll()
    ) { habits, completions ->
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val byHabit = completions.groupBy { it.habitId }
        habits.maxOfOrNull { habit ->
            val days = byHabit[habit.id]?.map { it.dateEpochDay }?.toSet() ?: emptySet()
            HabitStreakCalculator.currentStreak(days, today)
        } ?: 0
    }

    val uiState: StateFlow<HomeUiState> = combine(
        financeSnapshot, statusSnapshot, insightInputsSnapshot, tickerFlow(), bestHabitStreak
        // see TickerFlow's kdoc - "month wise not updating" bug fix: spentThisMonth/spentToday
        // are derived from LocalDate.now() inside this block, which only re-runs on data change,
        // not on time passing.
    ) { finance, status, insightInputs, _, bestStreak ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val spent = finance.transactions
            .filter { it.direction == TransactionDirection.DEBIT && it.date >= monthStart }
            .sumOf { it.amount }
        val spentToday = finance.transactions
            .filter { it.direction == TransactionDirection.DEBIT && it.date >= todayStart }
            .sumOf { it.amount }
        val last7DaysSpend = (6 downTo 0).map { today.minusDays(it.toLong()) }.map { day ->
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            finance.transactions
                .filter { it.direction == TransactionDirection.DEBIT && it.date in start until end }
                .sumOf { it.amount }
                .toFloat()
        }
        val dailySpendThreshold = finance.budgets
            .firstOrNull { it.budget.categoryId == null }
            ?.let { (it.budget.monthlyLimit / today.lengthOfMonth()).toFloat() }

        val overdueBill = finance.bills.firstOrNull { it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE }
        val overBudgets = finance.budgets.filter { it.spentThisMonth > it.budget.monthlyLimit }
        // Bug fix (found via a user report): a specific category being over budget is more
        // actionable than the generic "Overall" total budget (categoryId == null, see
        // FinanceInsightsRepository's categoryName resolution) being over. Previously this used
        // firstOrNull() with no preference, so whichever happened to come first in the DB's
        // return order could surface "Overall is ₹X over budget" instead of naming the actual
        // category - real numbers, but strictly less useful than the detail a category name gives.
        val overBudget = overBudgets.firstOrNull { it.budget.categoryId != null } ?: overBudgets.firstOrNull()
        val cashFlowRisk = computeCashFlowRisk(finance)
        val spendingInsight = SpendingInsightEngine.compute(
            transactions = finance.transactions,
            categories = insightInputs.categories,
            goals = insightInputs.goals
        )

        val attentionItem = when {
            overdueBill != null -> AttentionItem.OverdueBill(overdueBill.bill.payeeDisplay, overdueBill.bill.typicalAmount)
            cashFlowRisk != null -> cashFlowRisk
            overBudget != null -> AttentionItem.OverBudget(
                overBudget.categoryName,
                overBudget.spentThisMonth - overBudget.budget.monthlyLimit
            )
            finance.unparsedCount > 0 -> AttentionItem.UnparsedMessages(finance.unparsedCount)
            else -> null
        }

        HomeUiState(
            spentThisMonth = spent,
            spentToday = spentToday,
            dailySpendThreshold = dailySpendThreshold,
            bestHabitStreak = bestStreak,
            attentionItem = attentionItem,
            hasAnyData = finance.transactions.isNotEmpty(),
            unreadNotifications = status.unreadNotifications,
            isOnline = status.isOnline,
            smsPermissionRevoked = status.smsConsentedButRevoked,
            last7DaysSpend = last7DaysSpend,
            spendingInsight = spendingInsight
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /**
     * AI Transformation Plan F1. Projects known due-dates (Bills, plus Subscriptions - a real
     * upcoming debit even though it's automatic, see FinanceNotificationChecks.syncBillTasks' kdoc
     * for why Subscriptions are excluded from *that* feature but belong here) within
     * [CASH_FLOW_WINDOW_DAYS] against remaining budget headroom at current pace. Deliberately
     * returns null (no signal, not a false "you're fine") when no budget exists at all - there's
     * nothing to project a pace against.
     */
    private fun computeCashFlowRisk(finance: FinanceSnapshot): AttentionItem.CashFlowRisk? {
        if (finance.budgets.isEmpty()) return null

        val upcomingBillsTotal = finance.bills
            .filter {
                it.bill.status == com.lifeos.expensecapture.data.db.entity.BillStatus.CONFIRMED_TRACKED &&
                    it.displayStatus != FinanceInsightsRepository.BillDisplayStatus.PAID_THIS_CYCLE &&
                    it.daysUntilDue <= CASH_FLOW_WINDOW_DAYS
            }
            .sumOf { it.bill.typicalAmount }

        val now = System.currentTimeMillis()
        val upcomingSubsTotal = finance.subscriptions
            .filter {
                (it.displayStatus == FinanceInsightsRepository.SubscriptionDisplayStatus.TRACKED ||
                    it.displayStatus == FinanceInsightsRepository.SubscriptionDisplayStatus.RENEWAL_UPCOMING) &&
                    (it.nextExpectedDate - now) / 86_400_000.0 <= CASH_FLOW_WINDOW_DAYS
            }
            .sumOf { it.subscription.amount }

        val upcomingTotal = upcomingBillsTotal + upcomingSubsTotal
        val availableHeadroom = finance.budgets.sumOf { (it.budget.monthlyLimit - it.spentThisMonth).coerceAtLeast(0.0) }

        if (upcomingTotal <= availableHeadroom) return null
        return AttentionItem.CashFlowRisk(upcomingTotal, availableHeadroom, CASH_FLOW_WINDOW_DAYS)
    }

    companion object {
        private const val CASH_FLOW_WINDOW_DAYS = 14
    }
}
