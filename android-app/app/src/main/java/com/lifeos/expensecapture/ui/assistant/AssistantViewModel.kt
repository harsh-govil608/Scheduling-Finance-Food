package com.lifeos.expensecapture.ui.assistant

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.assistant.CommandExecutor
import com.lifeos.expensecapture.assistant.AiCommandInterpreter
import com.lifeos.expensecapture.assistant.CommandInterpreter
import com.lifeos.expensecapture.assistant.ConversationTurn
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantMessage(val text: String, val isUser: Boolean)

/**
 * See CommandIntent.kt's kdoc for the two-layer design. This ViewModel only owns the
 * conversation history and wires interpret -> execute together. Defaults to
 * AiCommandInterpreter, which itself falls back to RuleBasedCommandInterpreter when no key is
 * configured or the AI call fails - so this class doesn't need to know which one actually answered.
 */
class AssistantViewModel(
    private val db: AppDatabase,
    context: Context,
    private val interpreter: CommandInterpreter = AiCommandInterpreter()
) : ViewModel() {

    private val executor = CommandExecutor(db, context.applicationContext)

    private val _messages = MutableStateFlow(
        listOf(
            AssistantMessage(
                text = "Tell me what to do, in your own words - for example \"spent 200 on lunch\", " +
                    "\"add task call mom tomorrow\", \"add habit meditate\", \"add milk to shopping\", " +
                    "\"set food budget to 5000\", \"complete task call mom\", \"mark meditate done\", " +
                    "\"check off milk\", \"confirm bill electricity\", or \"recategorize swiggy as food\".",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    /** Real user request, 2026-08: no feedback at all while waiting for a response - a
     * FinanceQaEngine/AI round-trip can take a few real seconds, and with nothing on screen in
     * that gap it reads as the tap not having registered rather than "thinking." */
    private val _isResponding = MutableStateFlow(false)
    val isResponding: StateFlow<Boolean> = _isResponding.asStateFlow()

    /** Last few exchanges (oldest first), windowed rather than the full conversation so the
     * free-tier model's prompt stays small - see ConversationTurn's kdoc. Pairs up consecutive
     * user/assistant messages as they actually occurred; the initial static greeting isn't a
     * user turn, so it's naturally excluded. */
    private fun recentHistory(): List<ConversationTurn> {
        val turns = mutableListOf<ConversationTurn>()
        val messages = _messages.value
        var i = 0
        while (i < messages.size - 1) {
            val user = messages[i]
            val assistant = messages[i + 1]
            if (user.isUser && !assistant.isUser) {
                turns += ConversationTurn(user.text, assistant.text)
                i += 2
            } else {
                i += 1
            }
        }
        return turns.takeLast(MAX_HISTORY_TURNS)
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val history = recentHistory()
        _messages.value = _messages.value + AssistantMessage(trimmed, isUser = true)

        viewModelScope.launch {
            _isResponding.value = true
            val response = try {
                executor.execute(interpreter.interpret(trimmed, history), history)
            } catch (e: Exception) {
                AppLogger.e("AssistantViewModel", "command execution failed for: $trimmed", e)
                "Something went wrong doing that, so nothing was changed."
            } finally {
                _isResponding.value = false
            }
            _messages.value = _messages.value + AssistantMessage(response, isUser = false)
        }
    }

    private companion object {
        const val MAX_HISTORY_TURNS = 3
    }
}
