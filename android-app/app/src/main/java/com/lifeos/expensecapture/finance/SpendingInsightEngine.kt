package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * The actual "does this feel like AI" surface of the app: not a chat box, a narrative synthesis
 * of what changed and what to do about it - "Spending is up 18% this month, mostly Swiggy and
 * Uber. Cutting back to last month's pace here would free up ~₹4,500 this month." Every number
 * in it is real and traceable to the ledger; nothing here is a model call, in the same spirit as
 * every other insight in this app (Spend Prediction, recurring detection) - a rule-based
 * synthesis that reads like a person looked at your numbers, not a template that lists them.
 *
 * Deliberately returns null on a quiet month rather than manufacturing a "insight" out of noise -
 * same discipline as every other "nothing needs attention" state in this app.
 */
object SpendingInsightEngine {

    data class GoalAcceleration(val goalTitle: String, val monthsSooner: Double)

    data class SpendingInsight(
        val categoryName: String,
        val increaseAmount: Double,
        val increasePercent: Double,
        val topMerchants: List<String>,
        val monthlySavingsIfMatchLastMonth: Double,
        val goalAcceleration: GoalAcceleration?
    )

    private const val MIN_INCREASE_AMOUNT = 300.0
    private const val MIN_INCREASE_PERCENT = 10.0
    private const val MILLIS_PER_DAY = 86_400_000L
    private const val MIN_MONTHS_SOONER_TO_MENTION = 0.5

    fun compute(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        goals: List<GoalEntity>,
        now: Long = System.currentTimeMillis()
    ): SpendingInsight? {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val thisMonthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val daysElapsedThisMonth = ChronoUnit.DAYS.between(today.withDayOfMonth(1), today).coerceAtLeast(1)

        val lastMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        // Same-length window last month, not the whole prior month - a half-elapsed month
        // shouldn't get compared against a full one.
        val lastMonthComparableEnd = lastMonthStart + daysElapsedThisMonth * MILLIS_PER_DAY

        val debits = transactions.filter { it.direction == TransactionDirection.DEBIT }
        val thisMonth = debits.filter { it.date >= thisMonthStart }
        val lastMonthComparable = debits.filter { it.date >= lastMonthStart && it.date < lastMonthComparableEnd }

        val thisByCategory = thisMonth.groupBy { it.categoryId }.mapValues { (_, txns) -> txns.sumOf { it.amount } }
        val lastByCategory = lastMonthComparable.groupBy { it.categoryId }.mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val topCategoryDelta = thisByCategory.keys
            .map { categoryId ->
                val thisAmount = thisByCategory[categoryId] ?: 0.0
                val lastAmount = lastByCategory[categoryId] ?: 0.0
                val increase = thisAmount - lastAmount
                val percent = if (lastAmount > 0) (increase / lastAmount) * 100.0 else 100.0
                Triple(categoryId, increase, percent)
            }
            .filter { (_, increase, percent) -> increase >= MIN_INCREASE_AMOUNT && percent >= MIN_INCREASE_PERCENT }
            .maxByOrNull { (_, increase, _) -> increase } ?: return null

        val (categoryId, increaseAmount, increasePercent) = topCategoryDelta
        val categoryName = categories.firstOrNull { it.id == categoryId }?.name ?: "Uncategorized"

        val thisMonthMerchantTotals = thisMonth
            .filter { it.categoryId == categoryId }
            .groupBy { it.merchantNormalized }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } to txns.first().merchantRaw }
        val lastMonthMerchantTotals = lastMonthComparable
            .filter { it.categoryId == categoryId }
            .groupBy { it.merchantNormalized }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val topMerchants = thisMonthMerchantTotals.entries
            .map { (merchant, pair) -> pair.second to (pair.first - (lastMonthMerchantTotals[merchant] ?: 0.0)) }
            .filter { (_, delta) -> delta > 0 }
            .sortedByDescending { (_, delta) -> delta }
            .take(2)
            .map { (name, _) -> name }

        val monthlySavingsIfMatchLastMonth = (increaseAmount / daysElapsedThisMonth) * 30.0

        val goalAcceleration = computeGoalAcceleration(
            transactions = transactions,
            goals = goals,
            monthlySavingsIfCut = monthlySavingsIfMatchLastMonth,
            daysElapsedThisMonth = daysElapsedThisMonth,
            thisMonthStart = thisMonthStart
        )

        return SpendingInsight(
            categoryName = categoryName,
            increaseAmount = increaseAmount,
            increasePercent = increasePercent,
            topMerchants = topMerchants,
            monthlySavingsIfMatchLastMonth = monthlySavingsIfMatchLastMonth,
            goalAcceleration = goalAcceleration
        )
    }

    /** Current pace is approximated from this app's own captured net cash flow (credits minus
     * debits) - the only "money in vs money out" signal this app has, since it doesn't model
     * income or savings allocation directly. Treated the same way Spend Prediction treats its own
     * run-rate: an estimate, never stated as certain, and skipped entirely rather than shown as a
     * nonsense number when the current pace is zero or negative. */
    private fun computeGoalAcceleration(
        transactions: List<TransactionEntity>,
        goals: List<GoalEntity>,
        monthlySavingsIfCut: Double,
        daysElapsedThisMonth: Long,
        thisMonthStart: Long
    ): GoalAcceleration? {
        val goal = goals.firstOrNull { !it.completed && (it.targetAmount ?: 0.0) > 0.0 } ?: return null
        val targetAmount = goal.targetAmount ?: return null

        val thisMonthTxns = transactions.filter { it.date >= thisMonthStart }
        val credits = thisMonthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
        val debits = thisMonthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        val currentMonthlyPace = ((credits - debits) / daysElapsedThisMonth) * 30.0
        if (currentMonthlyPace <= 0.0) return null

        val monthsAtCurrentPace = targetAmount / currentMonthlyPace
        val monthsWithCut = targetAmount / (currentMonthlyPace + monthlySavingsIfCut)
        val monthsSooner = monthsAtCurrentPace - monthsWithCut
        if (monthsSooner < MIN_MONTHS_SOONER_TO_MENTION) return null

        return GoalAcceleration(goal.title, monthsSooner)
    }
}
