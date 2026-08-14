package com.lifeos.expensecapture.voice

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.assistant.CommandExecutor
import com.lifeos.expensecapture.assistant.RuleBasedCommandInterpreter
import com.lifeos.expensecapture.ui.theme.ExpenseCaptureTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Voice quick-capture (2026-08, real user request: "AI voice assistant outside app for quick
 * records and summaries"), launched from the Quick Settings tile (VoiceCaptureTileService) so it
 * never requires fully opening the app - swipe down, tap the tile, speak. A translucent, no-
 * title-bar theme (Theme.VoiceCapture) makes this feel like a floating quick-action rather than
 * "the app," even though it's a real Activity.
 *
 * Deliberately reuses RuleBasedCommandInterpreter (not AiCommandInterpreter) - fully offline,
 * works with no connectivity, and a multi-second network AI round-trip doesn't fit a glanceable
 * quick-tile flow the way it fits the full Assistant chat screen. Same reasoning for the
 * interpret->execute pipeline itself: CommandExecutor.execute already implements every command
 * this app supports (add expense, log a habit, complete a task, etc.) - reusing it here means
 * zero duplicated parsing/logging logic, not a second implementation to keep in sync.
 *
 * Uses the raw android.speech.tts.TextToSpeech API directly rather than
 * ui/common/Speech.kt's rememberSpeaker() - that helper is tied to a Composable's composition
 * lifecycle (DisposableEffect), which fits a long-lived screen but not this short-lived,
 * finish-itself-in-a-few-seconds Activity.
 */
class VoiceCaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseCaptureTheme {
                VoiceCaptureContent(onFinished = { finish() })
            }
        }
    }
}

private enum class CaptureStatus { LISTENING, PROCESSING, DONE, NO_RECOGNIZER, NOTHING_HEARD }

@Composable
private fun VoiceCaptureContent(onFinished: () -> Unit) {
    val activity = LocalContext.current as Activity
    val app = activity.application as App
    val coroutineScope = rememberCoroutineScope()
    var status by remember { mutableStateOf(CaptureStatus.LISTENING) }
    var resultText by remember { mutableStateOf("") }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(activity) { }
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }

    fun speakThenFinish(text: String) {
        resultText = text
        status = CaptureStatus.DONE
        val engine = tts
        if (engine != null) {
            engine.language = Locale.getDefault()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_capture")
        }
        // Give the spoken confirmation a moment to actually be heard before the Activity closes -
        // no reliable "utterance finished" callback is wired here for what's meant to be a quick
        // 2-3 word confirmation, so a fixed delay is simpler and good enough for this glanceable
        // flow.
        coroutineScope.launch {
            delay(2500)
            onFinished()
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val heard = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (heard.isNullOrBlank()) {
            status = CaptureStatus.NOTHING_HEARD
            coroutineScope.launch {
                delay(1500)
                onFinished()
            }
            return@rememberLauncherForActivityResult
        }
        status = CaptureStatus.PROCESSING
        coroutineScope.launch {
            val executor = CommandExecutor(app.database, activity.applicationContext)
            val intent = RuleBasedCommandInterpreter.interpret(heard, emptyList())
            val response = executor.execute(intent)
            speakThenFinish(response)
        }
    }

    LaunchedEffect(Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "What did you spend, or what do you want to log?")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            status = CaptureStatus.NO_RECOGNIZER
            delay(1500)
            onFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                if (status == CaptureStatus.LISTENING || status == CaptureStatus.PROCESSING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text(
                            if (status == CaptureStatus.LISTENING) "Listening..." else "Got it, one sec...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Text(
                        when (status) {
                            CaptureStatus.DONE -> resultText
                            CaptureStatus.NO_RECOGNIZER -> "No voice recognizer available on this device"
                            CaptureStatus.NOTHING_HEARD -> "Didn't catch that"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
