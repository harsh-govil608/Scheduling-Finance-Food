package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Rule-based only, deliberately - see CommandIntent.kt's kdoc for why. Covers exactly the
 * phrasings actually requested: logging a transaction, adding a task/habit/shopping item, and
 * setting a budget. Order matters below - budget/shopping patterns are checked before the more
 * generic spend pattern so "set food budget to 500" isn't misread as a transaction.
 */
object RuleBasedCommandInterpreter : CommandInterpreter {

    private val budgetPattern = Regex(
        "(?:set|update)\\s+(.+?)\\s+budget\\s+to\\s+(?:rs\\.?|inr|₹)?\\s*(\\d+(?:\\.\\d+)?)",
        RegexOption.IGNORE_CASE
    )
    private val shoppingPattern = Regex(
        "(?:add|buy)\\s+(.+?)\\s+to\\s+(?:the\\s+)?shopping(?:\\s+list)?",
        RegexOption.IGNORE_CASE
    )
    private val shoppingShorthandPattern = Regex("shopping\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val spendPattern = Regex(
        "(?:spent|paid)\\s+(?:rs\\.?|inr|₹)?\\s*(\\d+(?:\\.\\d+)?)\\s+(?:on|for)\\s+(.+)",
        RegexOption.IGNORE_CASE
    )
    private val receivedPattern = Regex(
        "(?:received|got)\\s+(?:rs\\.?|inr|₹)?\\s*(\\d+(?:\\.\\d+)?)\\s+(?:from|for)\\s+(.+)",
        RegexOption.IGNORE_CASE
    )
    private val taskPattern = Regex(
        "(?:add task|remind me to|new task)\\s*:?\\s*(.+)",
        RegexOption.IGNORE_CASE
    )
    private val habitPattern = Regex(
        "(?:add habit|new habit|start habit)\\s*:?\\s*(.+)",
        RegexOption.IGNORE_CASE
    )

    // Quick one-shot actions (2026-08, real user request - "I want automation such that I ask
    // chatbot to do everything for me"). Explicit verb-first phrasing, same style as every
    // pattern above - AI's free-form understanding (AiCommandInterpreter) covers more natural
    // phrasing when a working key is configured; these are this fallback's own best effort.
    private val completeTaskPattern = Regex("(?:complete|finish)\\s+task\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val markTaskDonePattern = Regex("mark\\s+task\\s+(.+?)\\s+(?:as\\s+)?done", RegexOption.IGNORE_CASE)
    private val deleteTaskPattern = Regex("(?:delete|remove)\\s+task\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val completeHabitPattern = Regex("(?:complete|finish)\\s+habit\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val markHabitDonePattern = Regex("mark\\s+habit\\s+(.+?)\\s+(?:as\\s+)?done", RegexOption.IGNORE_CASE)
    private val checkShoppingItemPattern = Regex("(?:check off|bought)\\s+(.+)", RegexOption.IGNORE_CASE)
    private val confirmBillPattern = Regex("(?:confirm|track)\\s+bill\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val dismissBillPattern = Regex("(?:dismiss|not a)\\s+bill\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val confirmSubscriptionPattern = Regex("(?:confirm|track)\\s+subscription\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val dismissSubscriptionPattern = Regex("(?:dismiss|cancel)\\s+subscription\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE)
    private val recategorizePattern = Regex("(?:recategorize|categorize)\\s+(.+?)\\s+as\\s+(.+)", RegexOption.IGNORE_CASE)

    override suspend fun interpret(text: String, history: List<ConversationTurn>): CommandIntent {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return CommandIntent.Unrecognized(text)

        budgetPattern.find(trimmed)?.let { m ->
            val amount = m.groupValues[2].toDoubleOrNull()
            if (amount != null) return CommandIntent.SetBudget(categoryHint = m.groupValues[1].trim(), monthlyLimit = amount)
        }

        shoppingPattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.AddShoppingItem(name = name)
        }
        shoppingShorthandPattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.AddShoppingItem(name = name)
        }

        markTaskDonePattern.find(trimmed)?.let { m ->
            val title = m.groupValues[1].trim()
            if (title.isNotBlank()) return CommandIntent.CompleteTask(titleMatch = title)
        }
        completeTaskPattern.find(trimmed)?.let { m ->
            val title = m.groupValues[1].trim()
            if (title.isNotBlank()) return CommandIntent.CompleteTask(titleMatch = title)
        }
        deleteTaskPattern.find(trimmed)?.let { m ->
            val title = m.groupValues[1].trim()
            if (title.isNotBlank()) return CommandIntent.DeleteTask(titleMatch = title)
        }
        markHabitDonePattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.CompleteHabit(nameMatch = name)
        }
        completeHabitPattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.CompleteHabit(nameMatch = name)
        }
        checkShoppingItemPattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.CheckShoppingItem(nameMatch = name)
        }
        confirmBillPattern.find(trimmed)?.let { m ->
            val payee = m.groupValues[1].trim()
            if (payee.isNotBlank()) return CommandIntent.ConfirmBill(payeeMatch = payee)
        }
        dismissBillPattern.find(trimmed)?.let { m ->
            val payee = m.groupValues[1].trim()
            if (payee.isNotBlank()) return CommandIntent.DismissBill(payeeMatch = payee)
        }
        confirmSubscriptionPattern.find(trimmed)?.let { m ->
            val merchant = m.groupValues[1].trim()
            if (merchant.isNotBlank()) return CommandIntent.ConfirmSubscription(merchantMatch = merchant)
        }
        dismissSubscriptionPattern.find(trimmed)?.let { m ->
            val merchant = m.groupValues[1].trim()
            if (merchant.isNotBlank()) return CommandIntent.DismissSubscription(merchantMatch = merchant)
        }
        recategorizePattern.find(trimmed)?.let { m ->
            val merchant = m.groupValues[1].trim()
            val category = m.groupValues[2].trim()
            if (merchant.isNotBlank() && category.isNotBlank()) {
                return CommandIntent.RecategorizeTransaction(merchantMatch = merchant, categoryName = category)
            }
        }

        spendPattern.find(trimmed)?.let { m ->
            val amount = m.groupValues[1].toDoubleOrNull()
            if (amount != null) {
                return CommandIntent.AddTransaction(
                    amount = amount,
                    direction = TransactionDirection.DEBIT,
                    merchant = m.groupValues[2].trim()
                )
            }
        }

        receivedPattern.find(trimmed)?.let { m ->
            val amount = m.groupValues[1].toDoubleOrNull()
            if (amount != null) {
                return CommandIntent.AddTransaction(
                    amount = amount,
                    direction = TransactionDirection.CREDIT,
                    merchant = m.groupValues[2].trim()
                )
            }
        }

        taskPattern.find(trimmed)?.let { m ->
            val (title, dueDate) = extractDueDate(m.groupValues[1].trim())
            if (title.isNotBlank()) return CommandIntent.AddTask(title = title, dueDate = dueDate)
        }

        habitPattern.find(trimmed)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotBlank()) return CommandIntent.AddHabit(name = name)
        }

        return CommandIntent.Unrecognized(text)
    }

    /** Only "today"/"tomorrow" for now - anything more (a specific date, "next Friday") needs
     * real language understanding, exactly the part this app is deliberately deferring to a real
     * AI key later rather than hand-rolling an ever-expanding date-phrase grammar now. */
    private fun extractDueDate(title: String): Pair<String, Long?> {
        val zone = ZoneId.systemDefault()
        val lower = title.lowercase(Locale.getDefault())
        val todayMillis = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val tomorrowMillis = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return when {
            // dropLast sized off the literal suffix's own length, not a hand-counted number -
            // a hand-counted " today"/" tomorrow" length is exactly the kind of off-by-one that
            // would silently mis-trim the title (found and fixed before this ever shipped).
            lower == "today" -> "" to todayMillis
            lower.endsWith(" today") -> title.dropLast(" today".length).trim() to todayMillis
            lower == "tomorrow" -> "" to tomorrowMillis
            lower.endsWith(" tomorrow") -> title.dropLast(" tomorrow".length).trim() to tomorrowMillis
            else -> title to null
        }
    }
}
