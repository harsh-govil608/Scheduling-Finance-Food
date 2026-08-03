package com.lifeos.expensecapture.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.assistant.AiCommandInterpreter
import com.lifeos.expensecapture.assistant.CommandExecutor
import com.lifeos.expensecapture.assistant.CommandInterpreter
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantMessage(val text: String, val isUser: Boolean)

/**
 * See CommandIntent.kt's kdoc for the two-layer design. This ViewModel only owns the
 * conversation history and wires interpret -> execute together. Defaults to AiCommandInterpreter,
 * which itself falls back to RuleBasedCommandInterpreter when no key is configured or the AI call
 * fails - so this class doesn't need to know which one actually answered.
 */
class AssistantViewModel(
    private val db: AppDatabase,
    private val interpreter: CommandInterpreter = AiCommandInterpreter()
) : ViewModel() {

    private val executor = CommandExecutor(db)

    private val _messages = MutableStateFlow(
        listOf(
            AssistantMessage(
                text = "Tell me what to do, in your own words - for example \"spent 200 on lunch\", " +
                    "\"add task call mom tomorrow\", \"add habit meditate\", \"add milk to shopping\", " +
                    "or \"set food budget to 5000\".",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        _messages.value = _messages.value + AssistantMessage(trimmed, isUser = true)

        viewModelScope.launch {
            val response = try {
                executor.execute(interpreter.interpret(trimmed))
            } catch (e: Exception) {
                AppLogger.e("AssistantViewModel", "command execution failed for: $trimmed", e)
                "Something went wrong doing that, so nothing was changed."
            }
            _messages.value = _messages.value + AssistantMessage(response, isUser = false)
        }
    }
}
