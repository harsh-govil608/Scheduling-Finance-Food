package com.lifeos.expensecapture.assistant

import com.google.gson.Gson
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.logging.AppLogger
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Real LLM-backed CommandInterpreter (2026-08), via Gemini - replaces the OpenRouter-backed
 * AiCommandInterpreter this app briefly had (see CommandIntent.kt's kdoc for why this exists and
 * what it replaces). Falls back to [RuleBasedCommandInterpreter] whenever the AI path can't
 * produce an answer - no key configured, network failure, malformed response, or the model
 * returning something that doesn't parse into a CommandIntent - so the assistant never goes
 * silent just because a network call failed.
 *
 * Unlike the old OpenRouter integration, this asks Gemini for `responseMimeType: application/json`
 * directly (via [GeminiClient]'s jsonMode) rather than scraping the first {...} block out of free
 * text - Gemini enforces valid JSON server-side, so malformed output here means the model
 * genuinely couldn't map the sentence, not a formatting slip.
 */
class GeminiCommandInterpreter(
    private val fallback: CommandInterpreter = RuleBasedCommandInterpreter
) : CommandInterpreter {

    private val gson = Gson()

    override suspend fun interpret(text: String): CommandIntent {
        return try {
            val content = GeminiClient.generateText(prompt = text, systemInstruction = SYSTEM_PROMPT, jsonMode = true)
                ?: return fallback.interpret(text)
            parseIntent(content) ?: fallback.interpret(text)
        } catch (e: Exception) {
            AppLogger.e("GeminiCommandInterpreter", "AI interpret failed for: $text - falling back to rules", e)
            fallback.interpret(text)
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
            {"type":"Unrecognized","rawText":"the original sentence"}

            Rules: direction is DEBIT for money spent/paid, CREDIT for money received/got.
            dueDateHint is "today", "tomorrow", or null - never a specific date. quantity is a
            free-text string like "2" or "1 kg", default "" if not mentioned. categoryHint is only
            set if the user named a category explicitly; otherwise null. If the sentence isn't
            about logging a transaction/task/habit/shopping item or setting a budget, use
            Unrecognized. Reply with the JSON object only.
        """.trimIndent()
    }
}
