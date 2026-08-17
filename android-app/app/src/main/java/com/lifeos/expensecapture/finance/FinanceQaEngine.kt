package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.assistant.AiClient
import com.lifeos.expensecapture.assistant.ConversationTurn
import com.lifeos.expensecapture.assistant.toChatMessages
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

    /** `languagePreference` (2026-08, real user request: local language support) - "auto"
     * (default) mirrors whatever language the question was written in; any other value forces
     * that language regardless. Kept as a plain String param rather than threading an Android
     * Context in here - callers (CommandExecutor, which already holds a Context) resolve
     * Prefs.getAiLanguage themselves, keeping this engine free of any Android dependency. */
    suspend fun answer(
        question: String,
        db: AppDatabase,
        languagePreference: String = "auto",
        history: List<ConversationTurn> = emptyList()
    ): String? {
        val snapshot = try {
            buildSnapshot(db)
        } catch (e: Exception) {
            AppLogger.e("FinanceQaEngine", "buildSnapshot failed", e)
            return null
        }
        return try {
            AiClient.generateText(
                prompt = "$snapshot\n\nQuestion: $question",
                systemInstruction = SYSTEM_PROMPT + "\n\n" + languageInstruction(languagePreference),
                history = history.toChatMessages()
            )
        } catch (e: Exception) {
            AppLogger.e("FinanceQaEngine", "answer failed", e)
            null
        }
    }

    private fun languageInstruction(languagePreference: String): String = if (languagePreference == "auto") {
        """
        Language: detect the language/script the user's question below is written in, and reply
        in that same language. If it's a mix (e.g. Hinglish - Hindi words in Latin script), reply
        the same way rather than switching to pure English or pure Hindi.
        """.trimIndent()
    } else {
        """
        Language: always reply in $languagePreference, regardless of what language the question
        below is written in.
        """.trimIndent()
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

        // Real bug fix (2026-08, user report - "why can't AI give summaries of past days, like a
        // past 7 day summary"): the snapshot only ever had "this month so far" (1-31 days
        // depending on when asked) and "last month" - nothing at a fixed recent-day granularity,
        // so a question about a specific window like the last 7 days had no matching real data to
        // answer from, and the system prompt's own "never invent a number" rule correctly refused
        // rather than guess. Mirrors the fixed 7-day window HomeScreen's own hero card already
        // uses (uiState.last7DaysSpend) - not an arbitrary new concept.
        val last7DaysStart = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        val last7DaysDebits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.date >= last7DaysStart }
        val last7DaysCredits = transactions.filter { it.direction == TransactionDirection.CREDIT && it.date >= last7DaysStart }
        val last7ByCategory = last7DaysDebits.groupBy { categoryName(it.categoryId) }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .entries.sortedByDescending { it.value }

        val thisMonthDebits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.date >= thisMonthStart }
        val lastMonthComparableDebits = transactions.filter {
            it.direction == TransactionDirection.DEBIT && it.date in lastMonthStart until lastMonthComparableEnd
        }
        val thisMonthCreditTxns = transactions.filter { it.direction == TransactionDirection.CREDIT && it.date >= thisMonthStart }
        val thisMonthCredits = thisMonthCreditTxns.sumOf { it.amount }
        val thisMonthRefunds = thisMonthCreditTxns.filter { it.isRefund }.sumOf { it.amount }

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

        // Investment AI integration (2026-08, real user request) - a manually-entered, non-synced
        // balance (see InvestmentEntity's kdoc), kept separate from the cash-flow forecast below.
        val investments = db.investmentDao().observeAll().first()

        // Pattern Engine design, 2026-08-12: replaces the old single "net cash flow pace"
        // extrapolation (this month's raw credits-minus-debits, stretched to 30 days) with
        // ForecastEngine's confidence-tagged breakdown - real code separating confirmed-recurring
        // income from estimated from variable/one-off, instead of asking the AI to correctly
        // apply that distinction to one undifferentiated number every single time.
        val forecast = ForecastEngine.compute(transactions, subscriptions, bills, investments)

        // Pattern Engine design, 2026-08-12 - the cross-domain piece: Habits sitting right next
        // to Finance is what no competitor's "AI insights" can do. See HabitSpendCorrelator's
        // own kdoc for why this is a plain fact, never a causal claim - "the habit isn't working"
        // is an inference the AI/user makes, not something asserted here.
        val habits = db.habitDao().observeAll().first()
        val habitCompletions = db.habitCompletionDao().observeAll().first()
        val habitCorrelations = HabitSpendCorrelator.correlate(habits, habitCompletions, categories, transactions)

        // "Learn and Adapt" (2026-08, real user feedback: "it predicts based on history, but if
        // AI learns then it can give more accurate predictions") - see ForecastAccuracyTracker's
        // kdoc. Real recorded track record, not a claim the AI invents about its own past.
        val accuracyHistory = db.forecastAccuracyDao().getRecent(6)

        return buildString {
            appendLine("Today's date: ${today} (day $daysElapsedThisMonth of this month so far)")
            appendLine()
            appendLine(
                "Last 7 days (${last7DaysStart.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }} to $today) - " +
                    "spent: Rs.${"%.2f".format(last7DaysDebits.sumOf { it.amount })}, received: Rs.${"%.2f".format(last7DaysCredits.sumOf { it.amount })}"
            )
            if (last7ByCategory.isNotEmpty()) {
                appendLine("Last 7 days by category:")
                last7ByCategory.forEach { (cat, amt) -> appendLine("- $cat: Rs.${"%.2f".format(amt)}") }
            }
            appendLine()
            appendLine("This month so far - spent: Rs.${"%.2f".format(thisMonthDebits.sumOf { it.amount })}, received: Rs.${"%.2f".format(thisMonthCredits)}")
            if (thisMonthRefunds > 0.0) {
                appendLine(
                    "Of that received amount, Rs.${"%.2f".format(thisMonthRefunds)} was refunds (money coming back from an " +
                        "earlier purchase, not new income) - the \"spent\" figure above is NOT reduced by refunds, it's every " +
                        "debit regardless. If asked about real/net/effective spending, mention both figures rather than " +
                        "picking one silently."
                )
            }
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
            appendLine()
            if (!forecast.hasEnoughHistoryToForecast) {
                appendLine(
                    "Forecast: only ${forecast.historyDays} days of transaction history exist - not enough to " +
                        "forecast income or affordability with any confidence yet. Say so plainly if asked; do not " +
                        "produce a confident monthly figure from this little data."
                )
            } else {
                appendLine("Monthly forecast (Pattern Engine - each figure is what it says, do not blur these together):")
                appendLine("- Confirmed recurring income (3+ regular payments, consistent amount): Rs.${"%.2f".format(forecast.confirmedMonthlyIncome)}/month")
                if (forecast.estimatedMonthlyIncome > 0.0) {
                    appendLine("- Estimated recurring income (a likely pattern, thinner evidence than confirmed): Rs.${"%.2f".format(forecast.estimatedMonthlyIncome)}/month")
                }
                if (forecast.variableMonthlyIncomeAverage > 0.0) {
                    appendLine("- Variable/irregular income (real money received, no repeating pattern - NOT guaranteed to recur): Rs.${"%.2f".format(forecast.variableMonthlyIncomeAverage)}/month average")
                }
                appendLine("- Confirmed recurring expenses (tracked subscriptions/bills): Rs.${"%.2f".format(forecast.confirmedMonthlyExpenses)}/month")
                if (forecast.estimatedMonthlyExpenses > 0.0) {
                    appendLine("- Estimated recurring expenses (detected, not yet confirmed by the user): Rs.${"%.2f".format(forecast.estimatedMonthlyExpenses)}/month")
                }
                appendLine("- Discretionary/day-to-day spending average: Rs.${"%.2f".format(forecast.discretionaryMonthlyAverage)}/month")
                appendLine("- Conservative net/month (confirmed income only, minus every expense): Rs.${"%.2f".format(forecast.conservativeNetMonthly)}")
                appendLine("- Fuller net/month (adds estimated + variable income too - less certain): Rs.${"%.2f".format(forecast.fullNetMonthly)}")
                appendLine(
                    "For any affordability question, lead with the conservative figure and mention the fuller one " +
                        "only as an upside case, clearly labeled as less certain - never present the fuller number " +
                        "as the safe answer."
                )
            }
            if (investments.isNotEmpty()) {
                appendLine()
                appendLine(
                    "Investment holdings (manually entered by the user, NOT live-synced - these values " +
                        "may be stale, say so if asked how current they are): Rs.${"%.2f".format(investments.sumOf { it.currentValue })} " +
                        "total across ${investments.size} holding${if (investments.size == 1) "" else "s"}."
                )
            }
            if (accuracyHistory.isNotEmpty()) {
                appendLine()
                appendLine(
                    "Forecast track record (what was predicted before each month started, using " +
                        "only data available at that time, vs what actually happened - conservative " +
                        "is deliberately pessimistic by design, so actual often exceeds it; fuller is " +
                        "the more directly comparable figure):"
                )
                accuracyHistory.forEach { r ->
                    appendLine(
                        "- ${r.monthKey}: predicted conservative Rs.${"%.2f".format(r.predictedConservativeNet)}, " +
                            "fuller Rs.${"%.2f".format(r.predictedFullNet)} - actual net was Rs.${"%.2f".format(r.actualNet)}"
                    )
                }
            }
            if (habitCorrelations.isNotEmpty()) {
                appendLine()
                appendLine(
                    "Habit/spending correlation (a fact, not a cause-and-effect claim - do not say the habit " +
                        "\"isn't working\" or \"caused\" a spend change, only report the two facts together):"
                )
                habitCorrelations.forEach { c ->
                    val change = if (c.categorySpendLastMonth > 0) {
                        ((c.categorySpendThisMonth - c.categorySpendLastMonth) / c.categorySpendLastMonth) * 100.0
                    } else null
                    val changeText = change?.let { " (${if (it >= 0) "+" else ""}${"%.0f".format(it)}% vs last month)" } ?: ""
                    appendLine(
                        "- \"${c.habit.name}\" logged ${c.completionCount} time${if (c.completionCount == 1) "" else "s"} in the " +
                            "last ${c.windowDays} days; ${c.categoryName} spend this month is Rs.${"%.2f".format(c.categorySpendThisMonth)}$changeText"
                    )
                }
            }
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
                    "To estimate months-to-target for a goal, use: target amount / (conservative net/month above + " +
                        "any extra monthly saving mentioned in the question). Only do this if that figure is " +
                        "positive, and only if hasEnoughHistoryToForecast was true above."
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

        Golden rule for any affordability, forecast, or "can I afford X" question: never infer
        recurring income from a single or short-term transaction pattern. The Monthly forecast
        section above already separates confirmed-recurring income from estimated from variable/
        irregular - never collapse those into one number or treat estimated/variable income as
        guaranteed. Lead every affordability answer with the conservative net/month figure; only
        mention the fuller figure as an explicitly-labeled upside case, never as the safe answer.
        If the data says there isn't enough history to forecast yet, say that plainly instead of
        producing a confident-sounding number anyway - do not work around that by falling back to
        the raw category/budget numbers elsewhere in the data to manufacture a forecast the
        Monthly forecast section itself declined to make.

        If a "Forecast track record" section is present, use it to calibrate how confidently you
        frame THIS month's forecast - if past fuller-net predictions have tracked close to actual,
        you can state the current estimate with a bit more confidence; if they've swung widely,
        say so and add extra caution. Never let a good track record override the golden rule above
        (still lead with the conservative figure, still say so plainly when there isn't enough
        history) - it only adjusts tone/confidence in how the numbers are framed, never which
        numbers get shown.

        If an "Investment holdings" line is present, it's a manually-entered, unverified,
        NOT-live-synced snapshot balance - useful for a net-worth-style question ("what am I
        worth"), but never substitute it for or blend it into the conservative/full net-monthly
        cash-flow figures above (a lump-sum balance and a monthly flow rate answer different
        questions). If asked how current the investment figure is, say plainly that it's whatever
        the user last typed in, not a live price.
    """
}
