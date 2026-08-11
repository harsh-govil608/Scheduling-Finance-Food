package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pattern Engine design, 2026-08-12 - the genuinely novel piece of the "financial planning, not
 * just tracking" direction: no competitor has both Habits and Finance in one place, so no
 * competitor can say "you've logged 'no eating out' 18 times this month, but Food & Dining spend
 * is still up 20%." That cross-domain fact is the whole point; this object only ever produces the
 * fact, never a causal claim ("the habit isn't working") - correlation, not causation, is
 * FinanceQaEngine/the AI's to reason about, not this deterministic layer's to assert.
 *
 * Deliberately name-based matching rather than an explicit habit-to-category link field - adding
 * that link field with no UI to ever set it would be a dead column nobody could use; a curated
 * keyword match against this app's own known category names ships something that actually works
 * today. Same "rules first, prove the need before reaching for more" discipline
 * RecurringPatternDetector's own kdoc describes - extend CATEGORY_KEYWORD_ALIASES as real habit
 * names show gaps, the same way TransactionParser's bank-sender list gets extended.
 */
object HabitSpendCorrelator {

    data class Correlation(
        val habit: HabitEntity,
        val categoryName: String,
        val completionCount: Int,
        val windowDays: Int,
        val categorySpendThisMonth: Double,
        val categorySpendLastMonth: Double
    )

    private const val WINDOW_DAYS = 30

    /** Keyed by this app's own real DefaultCategories names (see that file) - a habit name is
     * matched against a category's own name first (e.g. a habit literally called "Shopping"),
     * then against these aliases for the common "avoid X spending" phrasing a category name
     * alone wouldn't catch ("no eating out" doesn't contain "Food & Dining"). */
    private val CATEGORY_KEYWORD_ALIASES: Map<String, List<String>> = mapOf(
        "Food & Dining" to listOf("eating out", "dining", "restaurant", "takeout", "order food", "swiggy", "zomato", "food delivery"),
        "Groceries" to listOf("grocery", "groceries"),
        "Shopping" to listOf("shopping", "impulse buy", "impulse shopping", "online shopping"),
        "Entertainment" to listOf("movies", "streaming", "ott", "netflix"),
        "Transport" to listOf("cab", "uber", "ola", "taxi", "fuel", "petrol"),
        "Subscriptions" to listOf("subscription", "subscriptions")
    )

    fun correlate(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>
    ): List<Correlation> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val windowStartEpochDay = today.minusDays(WINDOW_DAYS.toLong()).toEpochDay()
        val thisMonthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val lastMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val completionsByHabit = completions.groupBy { it.habitId }

        return habits.filterNot { it.archived }.mapNotNull { habit ->
            val category = matchCategory(habit.name, categories) ?: return@mapNotNull null

            val completionCount = completionsByHabit[habit.id].orEmpty()
                .count { it.dateEpochDay >= windowStartEpochDay }
            if (completionCount == 0) return@mapNotNull null // nothing to correlate against yet

            val categoryTxns = transactions.filter {
                it.direction == TransactionDirection.DEBIT && !it.isTransfer && it.categoryId == category.id
            }
            val spendThisMonth = categoryTxns.filter { it.date >= thisMonthStart }.sumOf { it.amount }
            val spendLastMonth = categoryTxns.filter { it.date in lastMonthStart until thisMonthStart }.sumOf { it.amount }

            Correlation(
                habit = habit,
                categoryName = category.name,
                completionCount = completionCount,
                windowDays = WINDOW_DAYS,
                categorySpendThisMonth = spendThisMonth,
                categorySpendLastMonth = spendLastMonth
            )
        }
    }

    private fun matchCategory(habitName: String, categories: List<CategoryEntity>): CategoryEntity? {
        val lowerHabit = habitName.lowercase()

        categories.firstOrNull { lowerHabit.contains(it.name.lowercase()) }?.let { return it }

        CATEGORY_KEYWORD_ALIASES.forEach { (categoryName, aliases) ->
            if (aliases.any { alias -> lowerHabit.contains(alias) }) {
                categories.firstOrNull { it.name == categoryName }?.let { return it }
            }
        }
        return null
    }
}
