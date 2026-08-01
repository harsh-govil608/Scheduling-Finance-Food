package com.lifeos.expensecapture.finance

import kotlin.math.roundToInt

/**
 * Deterministic (explicitly NOT AI/ML - same discipline as SpendingInsightEngine and
 * ProductivityInsightEngine) 0-100 score, shared by Analytics and Profile (2026-08 reference
 * mockups' "Financial Health Score" card, `ui2/` folder) - a plain weighted formula over real
 * numbers each screen already has, not a learned or opaque prediction.
 */
object FinancialHealthScore {
    data class Inputs(
        val incomeThisMonth: Double,
        val spentThisMonth: Double,
        val prevMonthSpent: Double,
        /** spent/limit ratio for each budget with a limit > 0; empty if no budgets are set. */
        val budgetUtilizationRatios: List<Double>
    )

    fun compute(inputs: Inputs): Int {
        // Savings rate (0-100): how much of income wasn't spent, capped both ends.
        val savingsRate = if (inputs.incomeThisMonth > 0) {
            ((inputs.incomeThisMonth - inputs.spentThisMonth) / inputs.incomeThisMonth * 100)
                .coerceIn(0.0, 100.0)
        } else {
            50.0 // no income recorded yet this month - neutral, not a penalty
        }

        // Budget adherence (0-100): full marks at/under every limit, falling off with overage.
        val budgetAdherence = if (inputs.budgetUtilizationRatios.isNotEmpty()) {
            val avgOverage = inputs.budgetUtilizationRatios.map { (it - 1.0).coerceAtLeast(0.0) }.average()
            (100.0 - avgOverage * 100.0).coerceIn(0.0, 100.0)
        } else {
            70.0 // no budgets set - neutral-leaning, nothing to measure adherence against
        }

        // Trend (0-100): spending less than last month scores above 50, more scores below.
        val trend = if (inputs.prevMonthSpent > 0) {
            val changePercent = (inputs.spentThisMonth - inputs.prevMonthSpent) / inputs.prevMonthSpent
            (50.0 - changePercent * 100.0).coerceIn(0.0, 100.0)
        } else {
            50.0
        }

        val score = savingsRate * 0.4 + budgetAdherence * 0.35 + trend * 0.25
        return score.roundToInt().coerceIn(0, 100)
    }
}
