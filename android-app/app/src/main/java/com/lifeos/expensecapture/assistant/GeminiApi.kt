package com.lifeos.expensecapture.assistant

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Google's Generative Language ("Gemini") REST API - `generateContent` takes the API key as a
 * query param rather than an Authorization header, and its request/response shape is
 * `contents`/`parts` rather than OpenAI's `messages`, which is why this is a separate interface
 * from the OpenRouter one it replaces rather than a reskin of it (see GeminiClient's kdoc). */
interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(val text: String)

data class GeminiGenerationConfig(
    val temperature: Double = 0.1,
    /** "application/json" when the caller needs a reliably-parseable structured response (see
     * GeminiCommandInterpreter/AiCategorySuggester) - Gemini enforces valid JSON output itself
     * rather than needing the brittle "find the first {...} block" scraping the old OpenRouter
     * integration needed. Left null for the plain-text narration callers (AiTextPolisher). */
    val responseMimeType: String? = null
)

data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList())

data class GeminiCandidate(val content: GeminiContent? = null)
