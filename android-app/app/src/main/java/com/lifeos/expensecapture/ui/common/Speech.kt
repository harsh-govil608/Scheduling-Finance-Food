package com.lifeos.expensecapture.ui.common

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * "Voice everywhere": read a briefing aloud instead of requiring the user to open the app and
 * read it - Android's on-device TextToSpeech engine, no network call, no new permission, the
 * same on-device-only spirit as everything else in this app. Tied to the composition's
 * lifecycle: the engine is created once per call site and torn down when it leaves composition,
 * so nothing leaks across screens.
 */
@Composable
fun rememberSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        lateinit var tts: TextToSpeech
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault()
            }
        }
        engine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    return { text -> engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lifeos_speech") }
}
