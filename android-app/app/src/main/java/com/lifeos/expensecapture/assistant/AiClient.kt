package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.BuildConfig
import com.lifeos.expensecapture.logging.AppLogger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Shared LLM access point (2026-08) - every AI-touching feature in the app (the Assistant tab,
 * insight-card polishing, categorization suggestions, bill/budget review) goes through this one
 * [generateText] call rather than each standing up its own HTTP client, so provider/timeout/error
 * handling only needs to be right once. Provider-neutral name and call shape deliberately, since
 * this app has already switched providers twice in one week (OpenRouter -> a Gemini key that
 * turned out to be invalid -> back to OpenRouter, a real key this time) - keeping every other AI
 * feature's code (AiCommandInterpreter, AiTextPolisher, AiCategorySuggester, AiFinanceAnalyst)
 * pointed at a stable name/signature means the *next* provider swap, if any, is a one-file change
 * again instead of a repo-wide rename.
 *
 * Currently backed by OpenRouter (OpenAI-compatible chat completions). Deliberately returns null
 * on any failure (blank key, network error, blank/malformed response) instead of throwing - every
 * caller already has a deterministic fallback (rule-based intent parsing, the plain-computed
 * insight sentence, "no suggestion"), so this being unavailable never breaks the app's "works
 * fully offline" promise - AI is additive everywhere, never load-bearing.
 */
object AiClient {
    private const val MODEL = "openai/gpt-oss-20b:free"

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

    /**
     * @param jsonMode true when the caller needs to parse the response as JSON (structured
     * intents/category maps/arrays) - requests `response_format: json_object` (not every model
     * honors it) and, regardless, strips the response down to its first {...} or [...] block, the
     * same defensive scrape the original OpenRouter integration needed since models sometimes wrap
     * JSON in a markdown fence or add a stray sentence despite instructions.
     */
    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        jsonMode: Boolean = false,
        apiKey: String = BuildConfig.OPENROUTER_API_KEY
    ): String? {
        if (apiKey.isBlank()) return null
        return try {
            val messages = listOfNotNull(
                systemInstruction?.let { ChatMessage(role = "system", content = it) },
                ChatMessage(role = "user", content = prompt)
            )
            val response = api.chatCompletions(
                authorization = "Bearer $apiKey",
                request = ChatCompletionRequest(
                    model = MODEL,
                    messages = messages,
                    response_format = if (jsonMode) ResponseFormat() else null
                )
            )
            val content = response.choices.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotBlank() }
                ?: return null
            if (jsonMode) extractJsonBlock(content) else content
        } catch (e: Exception) {
            AppLogger.e("AiClient", "AI call failed", e)
            null
        }
    }

    /** Finds the first {...} or [...] block in the content, whichever starts first - handles a
     * JSON object (intents, category maps) or a JSON array (AiFinanceAnalyst's suggestion list)
     * without the caller needing to know which shape to expect ahead of time. */
    private fun extractJsonBlock(content: String): String {
        val objStart = content.indexOf('{')
        val arrStart = content.indexOf('[')
        val start = when {
            objStart == -1 -> arrStart
            arrStart == -1 -> objStart
            else -> minOf(objStart, arrStart)
        }
        if (start == -1) return content
        val isArray = content[start] == '['
        val end = if (isArray) content.lastIndexOf(']') else content.lastIndexOf('}')
        if (end == -1 || end < start) return content
        return content.substring(start, end + 1)
    }
}
