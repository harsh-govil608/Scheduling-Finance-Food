package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.BuildConfig
import com.lifeos.expensecapture.logging.AppLogger
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Shared Gemini access point (2026-08, replaces the OpenRouter integration this app briefly had) -
 * every AI-touching feature in the app (the Assistant tab, insight-card polishing, categorization
 * suggestions, bill/budget review) goes through this one [generateText] call rather than each
 * standing up its own Retrofit client, so timeout/error handling only needs to be right once.
 *
 * Deliberately returns null on any failure (blank key, network error, blank/malformed response)
 * instead of throwing - every caller already has a deterministic fallback (rule-based intent
 * parsing, the plain-computed insight sentence, "no suggestion"), matching the same
 * try/fail-quietly shape the old OpenRouter-backed AiCommandInterpreter used, now shared instead
 * of duplicated per feature. This is also what keeps the app's "works fully offline" promise
 * intact - nothing here is on any critical path, only ever an optional enhancement layer.
 */
object GeminiClient {
    private const val MODEL = "gemini-2.5-flash"

    private val api: GeminiApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * @param jsonMode true when the caller needs to parse the response as JSON (structured
     * intents/category maps) - false for plain narration text.
     */
    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        jsonMode: Boolean = false,
        apiKey: String = BuildConfig.GEMINI_API_KEY
    ): String? {
        if (apiKey.isBlank()) return null
        return try {
            val response = api.generateContent(
                model = MODEL,
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(prompt)))),
                    systemInstruction = systemInstruction?.let { GeminiContent(parts = listOf(GeminiPart(it))) },
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = if (jsonMode) "application/json" else null
                    )
                )
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            AppLogger.e("GeminiClient", "Gemini call failed", e)
            null
        }
    }
}
