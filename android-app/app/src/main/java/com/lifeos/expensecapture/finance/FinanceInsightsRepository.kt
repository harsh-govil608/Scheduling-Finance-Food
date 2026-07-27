package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.dao.BillDao
import com.lifeos.expensecapture.data.db.dao.BudgetDao
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.SubscriptionDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.BillEntity
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.BudgetEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionStatus
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Needs at least this many charges from a merchant before a "prior average" is meaningful
 * enough to compare the latest one against - otherwise two charges could each read as 100% drift
 * off the other. See FinanceInsightsRepository.PriceDrift's kdoc. */
private const val MIN_CHARGES_FOR_DRIFT_CHECK = 3

/** A product-level threshold, same spirit as RecurringPatternDetector's own
 * SUBSCRIPTION_VARIANCE_THRESHOLD_PERCENT - an explicit, named decision rather than a buried
 * algorithm detail, tunable once real usage shows it's wrong. */
private const val PRICE_DRIFT_THRESHOLD_PERCENT = 20.0

/**
 * Backs Finance Tracker (Home), Budget Planner, Subscription Manager, Bills, and Spend
 * Prediction (Phase 3 Docs 17, 19, 20, 21, 22) - all "insights derived from the transaction
 * ledger," kept together deliberately rather than four near-empty repositories, since Budget
 * and Subscription/Bill detection all read the same transaction stream via
 * RecurringPatternDetector.
 *
 * Spend Prediction (Doc 21) is NOT a separate screen here - it's folded into
 * BudgetProgress.projectedMonthEndSpend (per-category and overall), a deliberate scope
 * consolidation for the pilot rather than the PRD's own separate surface. See day-2.md.
 */
class FinanceInsightsRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val subscriptionDao: SubscriptionDao,
    private val billDao: BillDao
) {

    // ---------------------------------------------------------------------
    // Budgets + Spend Prediction (Docs 20, 21)
    // ---------------------------------------------------------------------

    data class BudgetProgress(
        val budget: BudgetEntity,
        val categoryName: String,
        val spentThisMonth: Double,
        val projectedMonthEndSpend: Double,
        val predictionConfidence: PredictionConfidence
    )

    enum class PredictionConfidence { INSUFFICIENT_DATA, LOW, MEDIUM }

    fun observeBudgetProgress(): Flow<List<BudgetProgress>> {
        return combine(
            budgetDao.observeAll(),
            transactionDao.observeAll(),
            categoryDao.observeAll()
        ) { budgets, transactions, categories ->
            val monthStart = startOfCurrentMonthMillis()
            val now = System.currentTimeMillis()
            val daysElapsed = ((now - monthStart) / 86_400_000.0).coerceAtLeast(1.0)
            val daysInMonth = daysInCurrentMonth()

            budgets.map { budget ->
                val relevant = transactions.filter {
                    it.direction == TransactionDirection.DEBIT &&
                        it.date >= monthStart &&
                        (budget.categoryId == null || it.categoryId == budget.categoryId)
                }
                val spent = relevant.sumOf { it.amount }
                val runRate = spent / daysElapsed
                val projected = runRate * daysInMonth

                // Never HIGH: this is a naive linear run-rate model, not ML - staying honest
                // about confidence matters more than looking precise (Doc 21's core requirement:
                // never present a prediction as certain fact).
                val confidence = when {
                    relevant.size < 2 || daysElapsed < 3 -> PredictionConfidence.INSUFFICIENT_DATA
                    daysElapsed < 10 -> PredictionConfidence.LOW
                    else -> PredictionConfidence.MEDIUM
                }

                val categoryName = budget.categoryId
                    ?.let { id -> categories.firstOrNull { it.id == id }?.name }
                    ?: "Overall"

                BudgetProgress(budget, categoryName, spent, projected, confidence)
            }
        }
    }

    /** Simple average of the last ~90 days of spend in a category, projected to a month.
     * Explicitly NOT AI - a placeholder for the "AI-suggested default" the PRD calls for,
     * honest about being an average rather than a learned suggestion. */
    suspend fun suggestedDefaultForCategory(categoryId: Long?): Double {
        val since = System.currentTimeMillis() - (90L * 86_400_000)
        val transactions = transactionDao.getSince(since)
        val relevant = transactions.filter {
            it.direction == TransactionDirection.DEBIT &&
                (categoryId == null || it.categoryId == categoryId)
        }
        if (relevant.isEmpty()) return 0.0
        val spanDays = ((System.currentTimeMillis() - relevant.minOf { it.date }) / 86_400_000.0)
            .coerceAtLeast(1.0)
        return (relevant.sumOf { it.amount } / spanDays) * 30.0
    }

    suspend fun setBudget(categoryId: Long?, monthlyLimit: Double) {
        val existing = if (categoryId == null) budgetDao.findOverall() else budgetDao.findByCategory(categoryId)
        budgetDao.upsert(BudgetEntity(id = existing?.id ?: 0, categoryId = categoryId, monthlyLimit = monthlyLimit))
    }

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)

    // ---------------------------------------------------------------------
    // Recurring detection shared by Subscriptions + Bills (Docs 19, 22)
    // ---------------------------------------------------------------------

    suspend fun refreshRecurringDetection() {
        val allTransactions = transactionDao.getSince(0L)
        val groups = RecurringPatternDetector.detect(allTransactions)

        for (group in groups) {
            if (RecurringPatternDetector.isSubscriptionLike(group)) {
                upsertSubscription(group)
            } else {
                upsertBill(group)
            }
        }
    }

    private suspend fun upsertSubscription(group: RecurringPatternDetector.RecurringGroup) {
        val existing = subscriptionDao.findByMerchant(group.merchantNormalized)
        if (existing == null) {
            subscriptionDao.insert(
                SubscriptionEntity(
                    merchantNormalized = group.merchantNormalized,
                    merchantDisplay = group.merchantDisplay,
                    amount = group.averageAmount,
                    cadenceDays = group.averageIntervalDays.toInt(),
                    lastTransactionDate = group.occurrences.last().date,
                    status = SubscriptionStatus.DETECTED_UNCONFIRMED
                )
            )
        } else if (existing.status != SubscriptionStatus.CANCELLED) {
            subscriptionDao.update(
                existing.copy(
                    amount = group.averageAmount,
                    cadenceDays = group.averageIntervalDays.toInt(),
                    lastTransactionDate = group.occurrences.last().date
                )
            )
        }
    }

    private suspend fun upsertBill(group: RecurringPatternDetector.RecurringGroup) {
        val existing = billDao.findByPayee(group.merchantNormalized)
        val dueDayOfMonth = dayOfMonthOf(group.occurrences.last().date)
        if (existing == null) {
            billDao.insert(
                BillEntity(
                    payeeNormalized = group.merchantNormalized,
                    payeeDisplay = group.merchantDisplay,
                    typicalAmount = group.averageAmount,
                    dueDayOfMonth = dueDayOfMonth,
                    lastPaidDate = group.occurrences.last().date,
                    status = BillStatus.DETECTED_UNCONFIRMED
                )
            )
        } else if (existing.status != BillStatus.CANCELLED) {
            billDao.update(
                existing.copy(
                    typicalAmount = group.averageAmount,
                    dueDayOfMonth = dueDayOfMonth,
                    lastPaidDate = group.occurrences.last().date
                )
            )
        }
    }

    // ---------------------------------------------------------------------
    // Subscriptions (Doc 19) - upcoming/renewed/lapsed derived, not stored
    // ---------------------------------------------------------------------

    enum class SubscriptionDisplayStatus { UNCONFIRMED, TRACKED, RENEWAL_UPCOMING, POSSIBLY_LAPSED, CANCELLED }

    /** AI Transformation Plan F2 (recurring pattern intelligence, generalized): the existing
     * detector only ever asked "does this repeat," never "did this change" - a subscription
     * creeping from ₹199 to ₹649 went completely unflagged since `refreshRecurringDetection`
     * stores a rolling average that absorbs a price change into itself over a few cycles instead
     * of surfacing it. Computed at display time from the raw transaction history (the most recent
     * charge versus the average of every prior one for that merchant), not persisted - consistent
     * with how every other bill/subscription status in this file is derived, not stored. */
    data class PriceDrift(val latestAmount: Double, val priorAverageAmount: Double, val percentChange: Double)

    data class SubscriptionWithComputedStatus(
        val subscription: SubscriptionEntity,
        val displayStatus: SubscriptionDisplayStatus,
        val nextExpectedDate: Long,
        val priceDrift: PriceDrift?
    )

    fun observeSubscriptions(): Flow<List<SubscriptionWithComputedStatus>> {
        return combine(subscriptionDao.observeAll(), transactionDao.observeAll()) { subs, transactions ->
            subs.map { sub ->
                val nextExpected = sub.lastTransactionDate + (sub.cadenceDays * 86_400_000L)
                val daysUntilNext = (nextExpected - System.currentTimeMillis()) / 86_400_000.0
                val status = when {
                    sub.status == SubscriptionStatus.CANCELLED -> SubscriptionDisplayStatus.CANCELLED
                    sub.status == SubscriptionStatus.DETECTED_UNCONFIRMED -> SubscriptionDisplayStatus.UNCONFIRMED
                    daysUntilNext < -(sub.cadenceDays * 0.5) -> SubscriptionDisplayStatus.POSSIBLY_LAPSED
                    daysUntilNext in -3.0..3.0 -> SubscriptionDisplayStatus.RENEWAL_UPCOMING
                    else -> SubscriptionDisplayStatus.TRACKED
                }

                val merchantCharges = transactions
                    .filter { it.direction == TransactionDirection.DEBIT && it.merchantNormalized == sub.merchantNormalized }
                    .sortedBy { it.date }
                val priceDrift = if (merchantCharges.size >= MIN_CHARGES_FOR_DRIFT_CHECK) {
                    val latest = merchantCharges.last().amount
                    val priorAverage = merchantCharges.dropLast(1).map { it.amount }.average()
                    val percentChange = if (priorAverage > 0) ((latest - priorAverage) / priorAverage) * 100.0 else 0.0
                    if (kotlin.math.abs(percentChange) >= PRICE_DRIFT_THRESHOLD_PERCENT) {
                        PriceDrift(latest, priorAverage, percentChange)
                    } else null
                } else null

                SubscriptionWithComputedStatus(sub, status, nextExpected, priceDrift)
            }
        }
    }

    suspend fun confirmSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription.copy(status = SubscriptionStatus.CONFIRMED_TRACKED))
    }

    suspend fun dismissSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription.copy(status = SubscriptionStatus.CANCELLED))
    }

    /** Subscription Manager PRD, Doc 19: "as a user, I want to manually add a subscription the
     * system hasn't detected yet" - for subscriptions with only one charge so far, or ones the
     * amount-variance heuristic misclassified. */
    suspend fun addManualSubscription(merchantDisplay: String, amount: Double, cadenceDays: Int) {
        subscriptionDao.insert(
            SubscriptionEntity(
                merchantNormalized = merchantDisplay.trim().lowercase(),
                merchantDisplay = merchantDisplay,
                amount = amount,
                cadenceDays = cadenceDays,
                lastTransactionDate = System.currentTimeMillis(),
                status = SubscriptionStatus.CONFIRMED_TRACKED,
                isManuallyAdded = true
            )
        )
    }

    // ---------------------------------------------------------------------
    // Bills (Doc 22) - upcoming/due/overdue derived from dueDayOfMonth, not stored
    // ---------------------------------------------------------------------

    enum class BillDisplayStatus { UPCOMING, DUE_TODAY, OVERDUE, PAID_THIS_CYCLE, UNCONFIRMED, CANCELLED }

    data class BillWithComputedStatus(
        val bill: BillEntity,
        val displayStatus: BillDisplayStatus,
        /** Negative once overdue. Added for the AI Transformation Plan's H1 (bill-to-task
         * auto-creation) and F1 (cross-module cash-flow guard), both of which need a "due within
         * N days" window rather than just the coarser UPCOMING/DUE_TODAY/OVERDUE bucket. */
        val daysUntilDue: Long,
        /** This cycle's resolved due date (start-of-day millis), so downstream consumers don't
         * each re-derive dueThisMonth from dueDayOfMonth themselves. */
        val dueDateThisCycleMillis: Long
    )

    fun observeBills(): Flow<List<BillWithComputedStatus>> {
        return billDao.observeAll().map { bills ->
            bills.map { bill ->
                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val dueThisMonth = today.withDayOfMonth(bill.dueDayOfMonth.coerceAtMost(today.lengthOfMonth()))
                val paidThisCycle = bill.lastPaidDate?.let { paidMillis ->
                    val paidDate = Instant.ofEpochMilli(paidMillis).atZone(zone).toLocalDate()
                    ChronoUnit.DAYS.between(paidDate, dueThisMonth) in 0..27
                } ?: false

                val status = when {
                    bill.status == BillStatus.CANCELLED -> BillDisplayStatus.CANCELLED
                    bill.status == BillStatus.DETECTED_UNCONFIRMED -> BillDisplayStatus.UNCONFIRMED
                    paidThisCycle -> BillDisplayStatus.PAID_THIS_CYCLE
                    today.isAfter(dueThisMonth) -> BillDisplayStatus.OVERDUE
                    today.isEqual(dueThisMonth) -> BillDisplayStatus.DUE_TODAY
                    else -> BillDisplayStatus.UPCOMING
                }
                BillWithComputedStatus(
                    bill = bill,
                    displayStatus = status,
                    daysUntilDue = ChronoUnit.DAYS.between(today, dueThisMonth),
                    dueDateThisCycleMillis = dueThisMonth.atStartOfDay(zone).toInstant().toEpochMilli()
                )
            }
        }
    }

    suspend fun confirmBill(bill: BillEntity) {
        billDao.update(bill.copy(status = BillStatus.CONFIRMED_TRACKED))
    }

    suspend fun dismissBill(bill: BillEntity) {
        billDao.update(bill.copy(status = BillStatus.CANCELLED))
    }

    /** Bills PRD, Doc 22 Feature Scope: "manual bill add/edit" - for bills with no reliable
     * digital trail (cash rent, informal loans) or that haven't recurred often enough to
     * auto-detect yet. */
    suspend fun addManualBill(payeeDisplay: String, typicalAmount: Double, dueDayOfMonth: Int) {
        billDao.insert(
            BillEntity(
                payeeNormalized = payeeDisplay.trim().lowercase(),
                payeeDisplay = payeeDisplay,
                typicalAmount = typicalAmount,
                dueDayOfMonth = dueDayOfMonth.coerceIn(1, 31),
                lastPaidDate = null,
                status = BillStatus.CONFIRMED_TRACKED,
                isManuallyAdded = true
            )
        )
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun startOfCurrentMonthMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun daysInCurrentMonth(): Int = LocalDate.now().lengthOfMonth()

    private fun dayOfMonthOf(epochMillis: Long): Int =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).dayOfMonth
}
