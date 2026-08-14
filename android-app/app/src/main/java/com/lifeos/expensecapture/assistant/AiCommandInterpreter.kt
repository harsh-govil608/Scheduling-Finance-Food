package com.lifeos.expensecapture.assistant

import com.google.gson.Gson
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.logging.AppLogger
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Real LLM-backed CommandInterpreter (2026-08), via OpenRouter (see CommandIntent.kt's kdoc for
 * why this exists). Briefly tried a Gemini key instead - that key turned out to be invalid
 * (a Google OAuth-shaped credential, not a real API key), so this is back on OpenRouter with a
 * real one; [AiClient]'s provider-neutral name/signature means this class itself didn't need to
 * change either time. Falls back to [RuleBasedCommandInterpreter] whenever the AI path can't
 * produce an answer - no key configured, network failure, malformed response, or the model
 * returning something that doesn't parse into a CommandIntent - so the assistant never goes
 * silent just because a network call failed.
 *
 * The model is asked to return one JSON object matching [AiIntentDto]'s shape rather than being
 * given function-calling tools, since OpenRouter's tool-calling support varies by underlying
 * model but every chat model can follow a "reply with only this JSON shape" instruction -
 * [AiClient]'s jsonMode requests `response_format: json_object` and defensively strips to the
 * first {...} block regardless, since not every model honors that request.
 */
class AiCommandInterpreter(
    private val fallback: CommandInterpreter = RuleBasedCommandInterpreter
) : CommandInterpreter {

    private val gson = Gson()

    override suspend fun interpret(text: String, history: List<ConversationTurn>): CommandIntent {
        return try {
            val content = AiClient.generateText(
                prompt = text,
                systemInstruction = SYSTEM_PROMPT,
                jsonMode = true,
                history = history.toChatMessages()
            ) ?: return fallback.interpret(text, history)
            parseIntent(content) ?: fallback.interpret(text, history)
        } catch (e: Exception) {
            AppLogger.e("AiCommandInterpreter", "AI interpret failed for: $text - falling back to rules", e)
            fallback.interpret(text, history)
        }
    }

    private fun parseIntent(content: String): CommandIntent? {
        val dto = try {
            gson.fromJson(content, AiIntentDto::class.java)
        } catch (e: Exception) {
            null
        } ?: return null

        return when (dto.type?.trim()?.lowercase(Locale.ROOT)) {
            "addtransaction" -> {
                val amount = dto.amount ?: return null
                val direction = if (dto.direction?.trim()?.lowercase(Locale.ROOT) == "credit") {
                    TransactionDirection.CREDIT
                } else {
                    TransactionDirection.DEBIT
                }
                val merchant = dto.merchant?.trim().orEmpty()
                if (merchant.isBlank()) null else CommandIntent.AddTransaction(amount, direction, merchant, dto.categoryHint)
            }
            "addtask" -> {
                val title = dto.title?.trim().orEmpty()
                if (title.isBlank()) null else CommandIntent.AddTask(title, dueDateMillisFor(dto.dueDateHint))
            }
            "addhabit" -> dto.name?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.AddHabit(it) }
            "addshoppingitem" -> dto.name?.trim().takeUnless { it.isNullOrBlank() }
                ?.let { CommandIntent.AddShoppingItem(it, dto.quantity.orEmpty()) }
            "setbudget" -> dto.monthlyLimit?.let { CommandIntent.SetBudget(dto.categoryHint?.trim(), it) }
            "completetask" -> dto.title?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.CompleteTask(it) }
            "deletetask" -> dto.title?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.DeleteTask(it) }
            "completehabit" -> dto.name?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.CompleteHabit(it) }
            "checkshoppingitem" -> dto.name?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.CheckShoppingItem(it) }
            "confirmbill" -> dto.merchant?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.ConfirmBill(it) }
            "dismissbill" -> dto.merchant?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.DismissBill(it) }
            "confirmsubscription" -> dto.merchant?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.ConfirmSubscription(it) }
            "dismisssubscription" -> dto.merchant?.trim().takeUnless { it.isNullOrBlank() }?.let { CommandIntent.DismissSubscription(it) }
            "recategorizetransaction" -> {
                val merchant = dto.merchant?.trim().orEmpty()
                val category = dto.categoryHint?.trim().orEmpty()
                if (merchant.isBlank() || category.isBlank()) null else CommandIntent.RecategorizeTransaction(merchant, category)
            }
            "unrecognized" -> CommandIntent.Unrecognized(dto.rawText.orEmpty())
            else -> null
        }
    }

    private fun dueDateMillisFor(hint: String?): Long? {
        val zone = ZoneId.systemDefault()
        return when (hint?.trim()?.lowercase(Locale.ROOT)) {
            "today" -> LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            "tomorrow" -> LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            else -> null
        }
    }

    private data class AiIntentDto(
        val type: String? = null,
        val amount: Double? = null,
        val direction: String? = null,
        val merchant: String? = null,
        val categoryHint: String? = null,
        val title: String? = null,
        val dueDateHint: String? = null,
        val name: String? = null,
        val quantity: String? = null,
        val monthlyLimit: Double? = null,
        val rawText: String? = null
    )

    private companion object {
        val SYSTEM_PROMPT = """
            You turn one sentence from a personal finance app's user into exactly one JSON object -
            nothing else, no explanation, no markdown fence. Pick the closest matching shape:

            {"type":"AddTransaction","amount":200,"direction":"DEBIT","merchant":"lunch","categoryHint":null}
            {"type":"AddTask","title":"call mom","dueDateHint":"tomorrow"}
            {"type":"AddHabit","name":"meditate"}
            {"type":"AddShoppingItem","name":"milk","quantity":"2 liters"}
            {"type":"SetBudget","categoryHint":"food","monthlyLimit":5000}
            {"type":"CompleteTask","title":"call mom"}
            {"type":"DeleteTask","title":"call mom"}
            {"type":"CompleteHabit","name":"meditate"}
            {"type":"CheckShoppingItem","name":"milk"}
            {"type":"ConfirmBill","merchant":"electricity"}
            {"type":"DismissBill","merchant":"electricity"}
            {"type":"ConfirmSubscription","merchant":"netflix"}
            {"type":"DismissSubscription","merchant":"netflix"}
            {"type":"RecategorizeTransaction","merchant":"swiggy","categoryHint":"food"}
            {"type":"Unrecognized","rawText":"the original sentence"}

            Rules: direction is DEBIT for money spent/paid, CREDIT for money received/got.
            dueDateHint is "today", "tomorrow", or null - never a specific date. quantity is a
            free-text string like "2" or "1 kg", default "" if not mentioned. categoryHint is only
            set if the user named a category explicitly; otherwise null (except for
            RecategorizeTransaction, where it's the target category and required). "title"/"name"/
            "merchant" for the Complete/Delete/Check/Confirm/Dismiss/Recategorize types is just
            whatever short phrase identifies the existing item/bill/subscription/transaction the
            user is referring to (it gets matched by substring, not exact match) - never invent
            one, use exactly what the user said. Use "done"/"complete"/"finished" wording for
            CompleteTask/CompleteHabit, "not a bill"/"stop tracking" for DismissBill, "cancel"/
            "stop" for DismissSubscription. If the sentence isn't about logging or acting on one
            of these, use Unrecognized. Reply with the JSON object only.

            Critical: AddTransaction is ONLY for money that has ALREADY been spent or received -
            a completed, past-tense fact ("spent 200 on lunch", "got 500 from Sohom"). A sentence
            that asks for advice, discusses a hypothetical, or describes something the user is
            considering or planning - even if it names a specific amount and item - is NOT a
            transaction to log. Watch for "how can I", "should I", "can I afford", "what if",
            "I want to" / "I'm planning to" (without "did"/"bought"/"paid" alongside it), or any
            question mark - these mean the user wants analysis or an opinion, which is
            Unrecognized (it gets answered from their real data separately), never AddTransaction.
            Example: "I want to buy a robot worth 10000, how can I by seeing my financial
            situation" is a question asking whether/how to afford a ₹10000 purchase - it must be
            Unrecognized, not a transaction, even though it mentions "10000" and describes an
            item. Getting this wrong doesn't just answer badly - it writes a fake transaction into
            the user's real financial records, which is worse than not answering at all.
        """.trimIndent()
    }
}
