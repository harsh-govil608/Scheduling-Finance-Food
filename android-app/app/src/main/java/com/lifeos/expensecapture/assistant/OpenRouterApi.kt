package com.lifeos.expensecapture.assistant

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** OpenRouter's chat completions endpoint is OpenAI-compatible - one Retrofit interface works for
 * whichever underlying model [AiClient] points at. */
interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.1,
    /** Only set when the caller needs reliably-parseable JSON back (structured intents/category
     * maps) - not every model on OpenRouter honors this, which is why AiClient also strips down
     * to the first {...}/[...] block regardless, rather than trusting this alone. */
    val response_format: ResponseFormat? = null
)

data class ResponseFormat(val type: String = "json_object")

data class ChatMessage(val role: String, val content: String)

data class ChatCompletionResponse(val choices: List<ChatChoice> = emptyList())

data class ChatChoice(val message: ChatMessage)
