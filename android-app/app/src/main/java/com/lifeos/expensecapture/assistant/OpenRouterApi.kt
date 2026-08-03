package com.lifeos.expensecapture.assistant

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** OpenRouter's chat completions endpoint is OpenAI-compatible - one Retrofit interface works for
 * whichever underlying model MODEL_ID points at (see AiCommandInterpreter). */
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
    val temperature: Double = 0.1
)

data class ChatMessage(val role: String, val content: String)

data class ChatCompletionResponse(val choices: List<ChatChoice>)

data class ChatChoice(val message: ChatMessage)
