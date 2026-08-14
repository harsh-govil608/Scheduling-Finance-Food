package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

/**
 * Natural-language automation (built via a real user request, 2026-07): "I speak the details in
 * human language and it updates everywhere, without me tapping Add/Edit buttons." Split into two
 * layers on purpose, per the user's own explicit sequencing ("AI keys I will integrate at last
 * once the structure is ready"):
 *
 * - CommandInterpreter (text -> CommandIntent): the "understanding" layer. RuleBasedCommandInterpreter
 *   was the only implementation at first - regex/keyword matching, same shape as TransactionParser
 *   and TransactionSearch already use elsewhere in this app. AiCommandInterpreter (2026-08, via
 *   OpenRouter - briefly tried a Gemini key in between that turned out to be invalid) is the real
 *   LLM-backed implementation this was staged for, wrapping RuleBasedCommandInterpreter as its
 *   fallback for when no key is configured or the network call fails - nothing downstream changed
 *   to support either provider swap, per the original plan.
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

    /** Quick one-shot actions (2026-08, real user request - "I want automation such that I ask
     * chatbot to do everything for me") - each matches an existing open/pending item by a
     * substring of its title/name/payee/merchant (whatever the user actually said) rather than an
     * ID the model could never know, same "find by name, not by ID" shape TransactionSearch
     * already uses elsewhere in this app. Deliberately excludes multi-step flows (creating a
     * family, building a Smart Split with participants) - those need their existing guided UI,
     * not a single sentence. */
    data class CompleteTask(val titleMatch: String) : CommandIntent()
    data class DeleteTask(val titleMatch: String) : CommandIntent()
    data class CompleteHabit(val nameMatch: String) : CommandIntent()
    data class CheckShoppingItem(val nameMatch: String) : CommandIntent()
    data class ConfirmBill(val payeeMatch: String) : CommandIntent()
    data class DismissBill(val payeeMatch: String) : CommandIntent()
    data class ConfirmSubscription(val merchantMatch: String) : CommandIntent()
    data class DismissSubscription(val merchantMatch: String) : CommandIntent()
    data class RecategorizeTransaction(val merchantMatch: String, val categoryName: String) : CommandIntent()

    /** The interpreter couldn't confidently map this to an action - same "don't silently drop
     * it" discipline ParseResult.Unparsed and TransactionSearch's no-match case already follow,
     * applied here to user commands instead of bank SMS. */
    data class Unrecognized(val rawText: String) : CommandIntent()
}

/** One prior exchange in the Assistant conversation - threaded through so the AI-backed
 * interpreter/Q&A path can actually answer a follow-up ("what about last week" after asking about
 * this week's spend). Real user report, 2026-08: the assistant "forgets the context after 2
 * conversations" - it was never a model context-window limit, prior turns were simply never sent
 * to the LLM at all (see AiClient.generateText's kdoc). */
data class ConversationTurn(val userText: String, val assistantText: String)

/** Flattens turns into the alternating user/assistant [ChatMessage] list [AiClient.generateText]
 * expects as its `history` param. */
fun List<ConversationTurn>.toChatMessages(): List<ChatMessage> =
    flatMap { listOf(ChatMessage(role = "user", content = it.userText), ChatMessage(role = "assistant", content = it.assistantText)) }

interface CommandInterpreter {
    suspend fun interpret(text: String, history: List<ConversationTurn> = emptyList()): CommandIntent
}
