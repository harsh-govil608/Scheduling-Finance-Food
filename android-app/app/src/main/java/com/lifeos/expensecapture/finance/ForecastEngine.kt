package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.BillEntity
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionStatus
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity

/**
 * Pattern Engine design, 2026-08-12 - the Forecast Engine step in the LIFE OS diagram. Combines
 * IncomePatternDetector's output with the already-existing subscription/bill detection into one
 * deterministic, confidence-tagged monthly picture - real code doing the arithmetic, not an LLM
 * asked to reason over raw numbers and hope it applies the golden rule correctly every time.
 * FinanceQaEngine's job becomes explaining this pre-computed result, the same division of labor
 * FinanceInsightsRepository.observeBudgetProgress already uses (deterministic math; AI only
 * touches what it's explicitly handed).
 *
 * Every figure here is monthly-normalized (a biweekly income source's average amount is doubled,
 * a quarterly bill's is divided by three) so they can be added together meaningfully - summing
 * raw per-occurrence amounts across different cadences would silently misrepresent the monthly
 * picture.
 */
object ForecastEngine {

    private const val DAYS_PER_MONTH = 30.0
    private const val MIN_HISTORY_DAYS_TO_FORECAST = 30

    data class MonthlyForecast(
        /** Sum of IncomePatternDetector's CONFIRMED sources, monthly-normalized. The only income
         * figure safe to describe as close to guaranteed. */
        val confirmedMonthlyIncome: Double,
        /** ESTIMATED sources - real, observed, but thinner evidence than CONFIRMED. */
        val estimatedMonthlyIncome: Double,
        /** Credits that never formed a recurring pattern at all (one-off payers, or too
         * irregular to call recurring), averaged over the actual history window - real money
         * that arrived, explicitly NOT framed as anything resembling guaranteed future income. */
        val variableMonthlyIncomeAverage: Double,
        /** Subscriptions/bills the user has explicitly confirmed as tracked. */
        val confirmedMonthlyExpenses: Double,
        /** Subscriptions/bills detected but not yet confirmed by the user - real pattern, one
         * fewer confirmation step than the CONFIRMED figures. */
        val estimatedMonthlyExpenses: Double,
        /** Average of every other debit (not a transfer, not part of a recurring group above) -
         * day-to-day discretionary spending, the least predictable figure in this whole result. */
        val discretionaryMonthlyAverage: Double,
        val historyDays: Int,
        /** False when there's under a month of usable history - ForecastEngine still returns its
         * best-effort numbers (all likely 0 or near it at that point anyway), but callers must
         * treat this as "don't forecast yet," not just a lower-confidence forecast. Structural
         * enforcement of the golden rule, not a prompt instruction hoping the AI remembers it. */
        val hasEnoughHistoryToForecast: Boolean
    ) {
        /** A conservative floor: confirmed income only, minus every expense (confirmed,
         * estimated, and discretionary) - the number safe to use for "can I afford X" without
         * leaning on anything less than confirmed-recurring income. */
        val conservativeNetMonthly: Double
            get() = confirmedMonthlyIncome - confirmedMonthlyExpenses - estimatedMonthlyExpenses - discretionaryMonthlyAverage

        /** Includes estimated + variable income too - a fuller picture, but must always be
         * presented alongside the conservative figure, never instead of it, so the person asking
         * can see how much of the total is less certain. */
        val fullNetMonthly: Double
            get() = conservativeNetMonthly + estimatedMonthlyIncome + variableMonthlyIncomeAverage
    }

    fun compute(
        transactions: List<TransactionEntity>,
        subscriptions: List<SubscriptionEntity>,
        bills: List<BillEntity>
    ): MonthlyForecast {
        val historyDays = historyWindowDays(transactions)

        val incomeScan = IncomePatternDetector.detect(transactions)
        val confirmedIncome = incomeScan.sources
            .filter { it.confidence == IncomePatternDetector.IncomeConfidence.CONFIRMED }
            .sumOf { monthlyNormalize(it.averageAmount, it.cadenceDays) }
        val estimatedIncome = incomeScan.sources
            .filter { it.confidence == IncomePatternDetector.IncomeConfidence.ESTIMATED }
            .sumOf { monthlyNormalize(it.averageAmount, it.cadenceDays) }
        val variableIncomeMonthly = if (historyDays > 0) {
            incomeScan.variableIncomeTotal / (historyDays / DAYS_PER_MONTH)
        } else {
            0.0
        }

        val activeSubs = subscriptions.filter { it.status != SubscriptionStatus.CANCELLED }
        val confirmedSubsMonthly = activeSubs
            .filter { it.status == SubscriptionStatus.CONFIRMED_TRACKED }
            .sumOf { monthlyNormalize(it.amount, it.cadenceDays.toDouble()) }
        val estimatedSubsMonthly = activeSubs
            .filter { it.status == SubscriptionStatus.DETECTED_UNCONFIRMED }
            .sumOf { monthlyNormalize(it.amount, it.cadenceDays.toDouble()) }

        val activeBills = bills.filter { it.status != BillStatus.CANCELLED }
        // Bills are monthly by nature (rent, utilities) - typicalAmount IS the monthly figure,
        // no cadence normalization needed the way subscriptions (which can bill at any interval)
        // require.
        val confirmedBillsMonthly = activeBills
            .filter { it.status == BillStatus.CONFIRMED_TRACKED }
            .sumOf { it.typicalAmount }
        val estimatedBillsMonthly = activeBills
            .filter { it.status == BillStatus.DETECTED_UNCONFIRMED }
            .sumOf { it.typicalAmount }

        val recurringExpenseMerchants = (activeSubs.map { it.merchantNormalized } + activeBills.map { it.payeeNormalized }).toSet()
        val discretionaryDebits = transactions.filter {
            it.direction == TransactionDirection.DEBIT && !it.isTransfer && it.merchantNormalized !in recurringExpenseMerchants
        }
        val discretionaryMonthly = if (historyDays > 0) {
            discretionaryDebits.sumOf { it.amount } / (historyDays / DAYS_PER_MONTH)
        } else {
            0.0
        }

        return MonthlyForecast(
            confirmedMonthlyIncome = confirmedIncome,
            estimatedMonthlyIncome = estimatedIncome,
            variableMonthlyIncomeAverage = variableIncomeMonthly,
            confirmedMonthlyExpenses = confirmedSubsMonthly + confirmedBillsMonthly,
            estimatedMonthlyExpenses = estimatedSubsMonthly + estimatedBillsMonthly,
            discretionaryMonthlyAverage = discretionaryMonthly,
            historyDays = historyDays,
            hasEnoughHistoryToForecast = historyDays >= MIN_HISTORY_DAYS_TO_FORECAST
        )
    }

    private fun monthlyNormalize(amount: Double, cadenceDays: Double): Double =
        if (cadenceDays <= 0) 0.0 else amount * (DAYS_PER_MONTH / cadenceDays)

    private fun historyWindowDays(transactions: List<TransactionEntity>): Int {
        val oldest = transactions.minOfOrNull { it.date } ?: return 0
        val days = ((System.currentTimeMillis() - oldest) / 86_400_000L).toInt()
        return days.coerceAtLeast(0)
    }
}
