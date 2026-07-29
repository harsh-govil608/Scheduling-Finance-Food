package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

/**
 * Natural-language automation (built via a real user request, 2026-07): "I speak the details in
 * human language and it updates everywhere, without me tapping Add/Edit buttons." Split into two
 * layers on purpose, per the user's own explicit sequencing ("AI keys I will integrate at last
 * once the structure is ready"):
 *
 * - CommandInterpreter (text -> CommandIntent): the "understanding" layer. RuleBasedCommandInterpreter
 *   is the only implementation today - regex/keyword matching, same shape as TransactionParser
 *   and TransactionSearch already use elsewhere in this app, deliberately no AI/ML. When a real
 *   LLM key is added later, a new interpreter implementing this same interface (or wrapping this
 *   one as a fallback) drops in without anything downstream changing.
 * - CommandExecutor (CommandIntent -> action): the "doing" layer. Calls the exact same
 *   repository/DAO methods every existing Add/Edit UI already calls - no new business logic,
 *   only routing. This is intentionally interpreter-agnostic, so whichever interpreter produced
 *   the intent, execution behaves identically.
 */
sealed class CommandIntent {
    data class AddTransaction(
        val amount: Double,
        val direction: TransactionDirection,
        val merchant: String,
        val categoryHint: String? = null
    ) : CommandIntent()

    data class AddTask(val title: String, val dueDate: Long? = null) : CommandIntent()

    data class AddHabit(val name: String) : CommandIntent()

    data class AddShoppingItem(val name: String, val quantity: String = "") : CommandIntent()

    data class SetBudget(val categoryHint: String?, val monthlyLimit: Double) : CommandIntent()

    /** The interpreter couldn't confidently map this to an action - same "don't silently drop
     * it" discipline ParseResult.Unparsed and TransactionSearch's no-match case already follow,
     * applied here to user commands instead of bank SMS. */
    data class Unrecognized(val rawText: String) : CommandIntent()
}

interface CommandInterpreter {
    fun interpret(text: String): CommandIntent
}
