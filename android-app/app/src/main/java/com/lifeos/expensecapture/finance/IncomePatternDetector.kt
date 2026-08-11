package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity

/**
 * Pattern Engine design, 2026-08-12 (real founder direction: "genuinely revolutionary... if we
 * give users financial planning... based on understanding the pattern of users' expense and
 * income"). The income-side half of the Pattern Engine in the LIFE OS diagram - RecurringPatternDetector
 * already found recurring DEBIT patterns (subscriptions/bills) for months; this is the same
 * proven interval/variance algorithm applied to CREDIT, which nothing in this app analyzed for
 * periodicity before now.
 *
 * Deliberately NOT persisted as a Room entity with a review/confirm lifecycle the way
 * Subscriptions/Bills are - unlike those, nothing here is a user-facing tracked list someone
 * confirms or dismisses; it's an input to ForecastEngine, recomputed from the live transaction
 * set every time, the same "derived at read time" pattern FinanceInsightsRepository already uses
 * for BudgetProgress/PriceDrift. That also means it costs nothing to keep improving as more
 * history arrives - there's no stale persisted state to reconcile.
 *
 * Confidence tiers exist because of the golden rule (never infer recurring income from a single
 * or short-term pattern): CONFIRMED is the only tier ForecastEngine should extrapolate as
 * guaranteed-feeling income. ESTIMATED is real but thinner evidence. Anything not forming a
 * group at all (RecurringPatternDetector's own MIN_OCCURRENCES=2 floor) is correctly absent from
 * this list entirely - insufficient data means no claimed pattern, not a low-confidence guess.
 */
object IncomePatternDetector {

    enum class IncomeConfidence { ESTIMATED, CONFIRMED }

    data class IncomeSource(
        val payerNormalized: String,
        val payerDisplay: String,
        val averageAmount: Double,
        val cadenceDays: Double,
        val confidence: IncomeConfidence,
        val lastReceivedDate: Long,
        val occurrenceCount: Int
    )

    /** Result of a whole scan, not just the recurring groups - `variableIncomeTotal` (credits
     * that never formed a recurring group at all: one-off payers, or repeat payers whose amount/
     * timing is too irregular to call a pattern) matters just as much to an honest forecast as
     * the recurring sources do. A user paid irregularly by multiple clients has real income
     * ForecastEngine must not silently drop just because none of it individually repeats. */
    data class IncomeScanResult(
        val sources: List<IncomeSource>,
        val variableIncomeTotal: Double,
        val variableIncomeTransactionCount: Int
    )

    /** Below this variance, a recurring credit group is confident enough to call CONFIRMED -
     * looser than Subscription Manager's fixed-price threshold (15%) since real salary varies
     * occurrence to occurrence (bonuses, small deductions) in a way a fixed subscription price
     * never should. Tune once real payslip data shows this is wrong - same explicit-threshold
     * discipline as every other product-level number in this file. */
    private const val CONFIRMED_VARIANCE_THRESHOLD_PERCENT = 20.0
    private const val CONFIRMED_MIN_OCCURRENCES = 3

    fun detect(transactions: List<TransactionEntity>): IncomeScanResult {
        val eligible = transactions.filter {
            it.direction == TransactionDirection.CREDIT && !it.isRefund && !it.isTransfer
        }
        val groups = RecurringPatternDetector.detect(eligible, TransactionDirection.CREDIT)

        val sources = groups.map { group ->
            val confidence = if (
                group.occurrences.size >= CONFIRMED_MIN_OCCURRENCES &&
                group.amountVariancePercent < CONFIRMED_VARIANCE_THRESHOLD_PERCENT
            ) {
                IncomeConfidence.CONFIRMED
            } else {
                IncomeConfidence.ESTIMATED
            }
            IncomeSource(
                payerNormalized = group.merchantNormalized,
                payerDisplay = group.merchantDisplay,
                averageAmount = group.averageAmount,
                cadenceDays = group.averageIntervalDays,
                confidence = confidence,
                lastReceivedDate = group.occurrences.last().date,
                occurrenceCount = group.occurrences.size
            )
        }

        val coveredPayers = groups.map { it.merchantNormalized }.toSet()
        val variableTransactions = eligible.filter { it.merchantNormalized !in coveredPayers }

        return IncomeScanResult(
            sources = sources,
            variableIncomeTotal = variableTransactions.sumOf { it.amount },
            variableIncomeTransactionCount = variableTransactions.size
        )
    }
}
