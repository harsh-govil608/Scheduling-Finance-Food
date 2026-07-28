package com.lifeos.expensecapture.util

import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Search PRD, Phase 3 Doc 05: "natural-language query interpretation," scoped down to what
 * actually exists in this app - transactions only (no tasks/meals, since Productivity/Health
 * pillars aren't built). The PRD itself defers embedding/model-based retrieval to later
 * phases; this is the rule-based entity extraction (dates, amounts) its Functional
 * Requirements call for at this stage, plus plain merchant-name matching. See day-2.md.
 */
object TransactionSearch {

    private val MONTH_NAMES = listOf(
        "january", "february", "march", "april", "may", "june",
        "july", "august", "september", "october", "november", "december"
    )

    /**
     * Bug fix (found via a real user report, 2026-07): search only ever matched merchant text -
     * typing a category name ("food") never matched anything, since TransactionEntity only
     * carries a categoryId, and this function had no way to resolve it to a name. Also, only the
     * three literal phrases "this week"/"last month"/"this month" were recognized as time
     * expressions - an actual month name ("july") fell through to the merchant-text filter and
     * matched nothing, since no merchant is ever literally named "july".
     */
    fun search(
        query: String,
        transactions: List<TransactionEntity>,
        categoryNameFor: (Long) -> String = { "" }
    ): List<TransactionEntity> {
        val lower = query.trim().lowercase(Locale.getDefault())
        if (lower.isBlank()) return emptyList()

        var results = transactions.asSequence()

        val overMatch = Regex("(?:over|above|more than)\\s+(\\d+(?:\\.\\d+)?)").find(lower)
        val underMatch = Regex("(?:under|below|less than)\\s+(\\d+(?:\\.\\d+)?)").find(lower)
        overMatch?.let { m ->
            val threshold = m.groupValues[1].toDouble()
            results = results.filter { it.amount > threshold }
        }
        underMatch?.let { m ->
            val threshold = m.groupValues[1].toDouble()
            results = results.filter { it.amount < threshold }
        }

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        // Word-boundary, not plain contains(): a short month name like "may" would otherwise
        // false-match inside an unrelated merchant/name like "Mayank" or "Maytas".
        val matchedMonthName = MONTH_NAMES.firstOrNull { Regex("\\b${it}\\b").containsMatchIn(lower) }
        when {
            lower.contains("this week") -> {
                val weekAgo = today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
                results = results.filter { it.date >= weekAgo }
            }
            lower.contains("last month") -> {
                val startOfThisMonth = today.withDayOfMonth(1)
                val startOfLastMonth = startOfThisMonth.minusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfLastMonth = startOfThisMonth.atStartOfDay(zone).toInstant().toEpochMilli()
                results = results.filter { it.date in startOfLastMonth until endOfLastMonth }
            }
            lower.contains("this month") -> {
                val startOfThisMonth = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                results = results.filter { it.date >= startOfThisMonth }
            }
            matchedMonthName != null -> {
                // A bare month name ("july") has no year to go on - every transaction in this
                // app is necessarily in the past, so the most recent occurrence of that month is
                // the only sensible reading: this year's if it's already happened, else last year's.
                val monthNumber = MONTH_NAMES.indexOf(matchedMonthName) + 1
                val year = if (monthNumber <= today.monthValue) today.year else today.year - 1
                val startOfMatchedMonth = LocalDate.of(year, monthNumber, 1)
                val start = startOfMatchedMonth.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = startOfMatchedMonth.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
                results = results.filter { it.date in start until end }
            }
        }

        var remainder = lower
        overMatch?.let { remainder = remainder.replace(it.value, "") }
        underMatch?.let { remainder = remainder.replace(it.value, "") }
        remainder = remainder.replace("this week", "").replace("last month", "").replace("this month", "")
        matchedMonthName?.let { remainder = remainder.replace(Regex("\\b${it}\\b"), "") }
        remainder = remainder.trim()

        if (remainder.isNotBlank()) {
            results = results.filter { txn ->
                txn.merchantRaw.lowercase(Locale.getDefault()).contains(remainder) ||
                    categoryNameFor(txn.categoryId).lowercase(Locale.getDefault()).contains(remainder)
            }
        }

        return results.sortedByDescending { it.date }.toList()
    }
}
