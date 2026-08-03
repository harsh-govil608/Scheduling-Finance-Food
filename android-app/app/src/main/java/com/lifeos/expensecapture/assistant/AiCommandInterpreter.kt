package com.lifeos.expensecapture.assistant

import com.google.gson.Gson
import com.lifeos.expensecapture.BuildConfig
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.logging.AppLogger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Real LLM-backed CommandInterpreter (2026-08), via OpenRouter (one API key, many models -
 * see CommandIntent.kt's kdoc for why this exists and what it replaces). Falls back to
 * [RuleBasedCommandInterpreter] whenever the AI path can't produce an answer - no key configured,
 * network failure, malformed response, or the model returning something that doesn't parse into a
 * CommandIntent - so the assistant never goes silent just because a network call failed.
 *
 * The model is asked to return one JSON object matching [AiIntentDto]'s shape rather than being
 * given function-calling tools, since OpenRouter's tool-calling support varies by underlying
 * model but every chat model can follow a "reply with only this JSON shape" instruction.
 */
class AiCommandInterpreter(
    private val fallback: CommandInterpreter = RuleBasedCommandInterpreter,
    private val apiKey: String = BuildConfig.OPENROUTER_API_KEY,
    private val model: String = "openai/gpt-oss-20b:free"
) : CommandInterpreter {

    private val api: OpenRouterApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }
    private val gson = Gson()

    override suspend fun interpret(text: String): CommandIntent {
        if (apiKey.isBlank()) return fallback.interpret(text)
        return try {
            val response = api.chatCompletions(
                authorization = "Bearer $apiKey",
                request = ChatCompletionRequest(
                    model = model,
                    messages = listOf(
                        ChatMessage(role = "system", content = SYSTEM_PROMPT),
                        ChatMessage(role = "user", content = text)
                    )
                )
            )
            val content = response.choices.firstOrNull()?.message?.content
                ?: return fallback.interpret(text)
            parseIntent(content) ?: fallback.interpret(text)
        } catch (e: Exception) {
            AppLogger.e("AiCommandInterpreter", "AI interpret failed for: $text - falling back to rules", e)
            fallback.interpret(text)
        }
    }

    /** Models sometimes wrap JSON in a markdown code fence despite the prompt asking for raw
     * JSON - extracting the first {...} block is more robust than requiring an exact match. */
    private fun parseIntent(content: String): CommandIntent? {
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd < jsonStart) return null
        val dto = try {
            gson.fromJson(content.substring(jsonStart, jsonEnd + 1), AiIntentDto::class.java)
        } catch (e: Exception) {
            return null
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
