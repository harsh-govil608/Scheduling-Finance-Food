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

    fun search(query: String, transactions: List<TransactionEntity>): List<TransactionEntity> {
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
        }

        var remainder = lower
        overMatch?.let { remainder = remainder.replace(it.value, "") }
        underMatch?.let { remainder = remainder.replace(it.value, "") }
        remainder = remainder.replace("this week", "").replace("last month", "").replace("this month", "").trim()

        if (remainder.isNotBlank()) {
            results = results.filter { it.merchantRaw.lowercase(Locale.getDefault()).contains(remainder) }
        }

        return results.sortedByDescending { it.date }.toList()
    }
}
