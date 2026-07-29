package com.lifeos.expensecapture.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.assistant.CommandExecutor
import com.lifeos.expensecapture.assistant.RuleBasedCommandInterpreter
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantMessage(val text: String, val isUser: Boolean)

/**
 * See CommandIntent.kt's kdoc for the two-layer design. This ViewModel only owns the
 * conversation history and wires interpret -> execute together - RuleBasedCommandInterpreter is
 * referenced directly for now rather than injected, matching this app's existing pattern of not
 * building a DI seam before there's a second real implementation to switch between (the same
 * discipline behind not adding a ViewModelProvider.Factory anywhere in this app yet). When an
 * LLM-backed interpreter exists, this becomes a constructor parameter then, not before.
 */
class AssistantViewModel(private val db: AppDatabase) : ViewModel() {

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
                executor.execute(RuleBasedCommandInterpreter.interpret(trimmed))
            } catch (e: Exception) {
                AppLogger.e("AssistantViewModel", "command execution failed for: $trimmed", e)
                "Something went wrong doing that, so nothing was changed."
            }
            _messages.value = _messages.value + AssistantMessage(response, isUser = false)
        }
    }
}
