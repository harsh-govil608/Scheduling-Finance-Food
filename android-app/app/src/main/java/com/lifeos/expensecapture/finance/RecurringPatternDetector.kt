package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity

/**
 * Shared recurring-transaction detection used by both Subscription Manager and Bills
 * (Phase 3 Docs 19 and 22). Those PRDs' own stated distinction between the two features is
 * whether the amount is fixed or variable across occurrences - this detector finds recurring
 * merchant groups once; callers then classify each group as subscription-like (low amount
 * variance) or bill-like (higher variance) instead of duplicating grouping/interval logic in
 * two places.
 *
 * Deliberately a simple rule-based heuristic, not a learned model - consistent with this
 * pilot's "rules first, prove the need before reaching for ML" approach already used for SMS
 * parsing. Runs on-demand (when a screen loads) rather than as a background job - see
 * docs/coders-documentation/day-2.md for what that trades off.
 */
object RecurringPatternDetector {

    data class RecurringGroup(
        val merchantNormalized: String,
        val merchantDisplay: String,
        val occurrences: List<TransactionEntity>,
        val averageAmount: Double,
        val amountVariancePercent: Double, // (max - min) / average, as a percentage
        val averageIntervalDays: Double
    )

    private const val MIN_OCCURRENCES = 2
    private const val MIN_INTERVAL_DAYS = 20.0
    private const val MAX_INTERVAL_DAYS = 40.0
    private const val MILLIS_PER_DAY = 86_400_000.0

    /**
     * Amount variance below this threshold is treated as "fixed enough" to be a subscription
     * candidate; at or above it, a bill candidate. This is a product-level threshold per the
     * Subscription Manager PRD's requirement that it be an explicit decision, not a buried
     * algorithm detail - tune once real usage shows it's wrong.
     */
    const val SUBSCRIPTION_VARIANCE_THRESHOLD_PERCENT = 15.0

    fun detect(transactions: List<TransactionEntity>): List<RecurringGroup> {
        return transactions
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { it.merchantNormalized }
            .mapNotNull { (merchant, txns) ->
                if (txns.size < MIN_OCCURRENCES) return@mapNotNull null

                val sorted = txns.sortedBy { it.date }
                val intervals = sorted.zipWithNext { a, b -> (b.date - a.date) / MILLIS_PER_DAY }
                val averageInterval = intervals.average()

                if (averageInterval < MIN_INTERVAL_DAYS || averageInterval > MAX_INTERVAL_DAYS) {
                    return@mapNotNull null
                }

                val amounts = sorted.map { it.amount }
                val average = amounts.average()
                val variancePercent = if (average > 0) {
                    ((amounts.max() - amounts.min()) / average) * 100.0
                } else {
                    0.0
                }

                RecurringGroup(
                    merchantNormalized = merchant,
                    merchantDisplay = sorted.last().merchantRaw,
                    occurrences = sorted,
                    averageAmount = average,
                    amountVariancePercent = variancePercent,
                    averageIntervalDays = averageInterval
                )
            }
    }

    fun isSubscriptionLike(group: RecurringGroup): Boolean =
        group.amountVariancePercent < SUBSCRIPTION_VARIANCE_THRESHOLD_PERCENT
}
