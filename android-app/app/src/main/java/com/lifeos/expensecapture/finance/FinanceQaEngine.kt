package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.assistant.AiClient
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Proactive financial Q&A (2026-08, real user request via a forwarded suggestion: "instead of
 * just displaying transactions, let the app proactively answer questions like 'why did I spend
 * more this month', 'how much can I safely spend this weekend', 'what subscriptions am I not
 * using', 'if I save X more, when will I reach my goal', 'summarize my finances in one minute'").
 *
 * Deliberately ONE general-purpose engine, not five hand-coded handlers for those five examples -
 * it builds one real-data snapshot (this/last month spend by category, budget headroom,
 * subscriptions, bills, and goal pace using the exact same net-cash-flow model
 * SpendingInsightEngine's goal-acceleration line already uses) and hands it to the AI with the
 * user's raw question, instructed to answer ONLY from that data. That answers the five examples
 * *and* whatever else someone actually asks, without a growing pile of bespoke "why" logic. Every
 * number in the snapshot is real; the AI's only job is explaining/reasoning over it, not sourcing
 * its own facts - the same grounding principle AiTextPolisher/AiCategorySuggester already follow.
 *
 * Returns null when AI is unavailable (blank key, network failure) - there's no honest
 * deterministic fallback for an open-ended question the way RuleBasedCommandInterpreter's regex
 * patterns are for a structured action, so the caller (CommandExecutor.unrecognized) has to show a
 * plain "AI isn't available right now" message instead of a wrong or fabricated answer.
 */
object FinanceQaEngine {

    suspend fun answer(question: String, db: AppDatabase): String? {
        val snapshot = try {
            buildSnapshot(db)
        } catch (e: Exception) {
            AppLogger.e("FinanceQaEngine", "buildSnapshot failed", e)
            return null
        }
        return try {
            AiClient.generateText(prompt = "$snapshot\n\nQuestion: $question", systemInstruction = SYSTEM_PROMPT)
        } catch (e: Exception) {
            AppLogger.e("FinanceQaEngine", "answer failed", e)
            null
        }
    }

    private suspend fun buildSnapshot(db: AppDatabase): String {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val thisMonthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val daysElapsedThisMonth = ChronoUnit.DAYS.between(today.withDayOfMonth(1), today).coerceAtLeast(1)
        val lastMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val lastMonthComparableEnd = lastMonthStart + daysElapsedThisMonth * 86_400_000L

        val transactions = db.transactionDao().observeAll().first()
        val categories = db.categoryDao().observeAll().first()
        fun categoryName(id: Long?) = categories.firstOrNull { it.id == id }?.name ?: "Uncategorized"

        val thisMonthDebits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.date >= thisMonthStart }
        val lastMonthComparableDebits = transactions.filter {
            it.direction == TransactionDirection.DEBIT && it.date in lastMonthStart until lastMonthComparableEnd
        }
        val thisMonthCredits = transactions.filter { it.direction == TransactionDirection.CREDIT && it.date >= thisMonthStart }
            .sumOf { it.amount }

        val thisByCategory = thisMonthDebits.groupBy { categoryName(it.categoryId) }.mapValues { it.value.sumOf { t -> t.amount } }
        val lastByCategory = lastMonthComparableDebits.groupBy { categoryName(it.categoryId) }.mapValues { it.value.sumOf { t -> t.amount } }

        val insights = FinanceInsightsRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao(),
            subscriptionDao = db.subscriptionDao(),
            billDao = db.billDao()
        )
        val budgetProgress = insights.observeBudgetProgress().first()
        val subscriptions = db.subscriptionDao().observeAll().first()
        val bills = db.billDao().observeAll().first()
        val goals = db.goalDao().observeAll().first().filter { !it.completed }

        val netPaceMonthly = ((thisMonthCredits - thisMonthDebits.sumOf { it.amount }) / daysElapsedThisMonth) * 30.0

        return buildString {
            appendLine("Today's date: ${today} (day $daysElapsedThisMonth of this month so far)")
            appendLine()
            appendLine("This month so far - spent: Rs.${"%.2f".format(thisMonthDebits.sumOf { it.amount })}, received: Rs.${"%.2f".format(thisMonthCredits)}")
            appendLine("Spend by category this month (vs the same number of days last month):")
            (thisByCategory.keys + lastByCategory.keys).distinct().forEach { cat ->
                val thisAmt = thisByCategory[cat] ?: 0.0
                val lastAmt = lastByCategory[cat] ?: 0.0
                if (thisAmt > 0.0 || lastAmt > 0.0) {
                    appendLine("- $cat: Rs.${"%.2f".format(thisAmt)} this month vs Rs.${"%.2f".format(lastAmt)} last month (same-length window)")
                }
            }
            appendLine()
            if (budgetProgress.isEmpty()) {
                appendLine("No budgets are set.")
            } else {
                appendLine("Budgets (this month):")
                budgetProgress.forEach { b ->
                    val remaining = b.budget.monthlyLimit - b.spentThisMonth
                    appendLine(
                        "- ${b.categoryName}: Rs.${"%.2f".format(b.spentThisMonth)} spent of Rs.${"%.2f".format(b.budget.monthlyLimit)} " +
                            "limit (Rs.${"%.2f".format(remaining)} remaining, projected month-end Rs.${"%.2f".format(b.projectedMonthEndSpend)})"
                    )
                }
            }
            appendLine()
            val activeSubs = subscriptions.filter { it.status.name != "CANCELLED" }
            if (activeSubs.isEmpty()) {
                appendLine("No subscriptions tracked.")
            } else {
                appendLine("Tracked subscriptions (no usage data is tracked, only billing):")
                activeSubs.forEach { s ->
                    val daysSinceLastCharge = ChronoUnit.DAYS.between(
                        Instant.ofEpochMilli(s.lastTransactionDate).atZone(zone).toLocalDate(), today
                    )
                    appendLine("- ${s.merchantDisplay}: Rs.${"%.2f".format(s.amount)} every ~${s.cadenceDays} days, last charged $daysSinceLastCharge days ago")
                }
            }
            appendLine()
            val activeBills = bills.filter { it.status.name != "CANCELLED" }
            if (activeBills.isNotEmpty()) {
                appendLine("Tracked bills:")
                activeBills.forEach { b ->
                    appendLine("- ${b.payeeDisplay}: ~Rs.${"%.2f".format(b.typicalAmount)}, usually due day ${b.dueDayOfMonth} of the month")
                }
                appendLine()
            }
            appendLine("Net cash flow pace (income minus spending), extrapolated to a 30-day month: Rs.${"%.2f".format(netPaceMonthly)}/month")
            if (goals.isEmpty()) {
                appendLine("No active goals are set.")
            } else {
                appendLine("Active goals:")
                goals.forEach { g ->
                    val targetText = g.targetAmount?.let { "target Rs.${"%.2f".format(it)}" } ?: "no rupee target set"
                    val dateText = g.targetDate?.let {
                        ", target date ${Instant.ofEpochMilli(it).atZone(zone).toLocalDate()}"
                    } ?: ""
                    appendLine("- \"${g.title}\" ($targetText$dateText)")
                }
                appendLine(
                    "To estimate months-to-target for a goal, use: target amount / (current monthly pace above + any " +
                        "extra monthly saving mentioned in the question). Only do this if the current pace is positive."
                )
            }
        }
    }

    private const val SYSTEM_PROMPT = """
        You are a financial assistant inside a personal finance app, answering one user question
        using ONLY the real data given above it - never invent a number, merchant, category, or
        fact that isn't in that data. If the data doesn't have what's needed to answer confidently,
        say so plainly instead of guessing (e.g. subscription "usage" isn't tracked at all, only
        billing - be upfront about that rather than pretending to know which ones are unused).
        Be concise (2-4 sentences), practical, and direct - this is a quick chat answer, not a
        report. Amounts are in Indian Rupees; write them as e.g. Rs.500 or ₹500.
    """
}
