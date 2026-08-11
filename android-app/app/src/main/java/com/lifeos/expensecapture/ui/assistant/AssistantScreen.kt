package com.lifeos.expensecapture.ui.assistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // weight() resolves per-receiver (RowScope/ColumnScope);
// importing it by name alone resolved to an internal symbol during the real build - see
// android-app/README.md "Known gaps" if this surfaces again after a Compose version bump.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar

/**
 * Natural-language automation UI (built via a real user request, 2026-07): "I speak the details
 * and it updates everywhere, without me tapping Add/Edit buttons." A chat transcript rather than
 * a form - the input is free text (typed or spoken via the existing RecognizerIntent pattern
 * already used in Search/Tasks/Shopping), each turn shows what was understood and what changed.
 * See CommandIntent.kt for the interpret -> execute split behind this screen.
 *
 * Promoted from a Home-pillar FAB to its own "AI" bottom-nav pillar (2026-08 reference mockups,
 * `ui2/` folder) - still the exact same rule-based CommandIntent engine, a navigation change only,
 * not a new model. The chat input row now lives in the content column, above PillarBottomBar,
 * rather than in Scaffold's bottomBar slot - that slot is the nav bar now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(app: App, onSelectPillar: (Pillar) -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { AssistantViewModel(app.database, context) }
    val messages by viewModel.messages.collectAsState()
    val isResponding by viewModel.isResponding.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!text.isNullOrBlank()) {
            viewModel.send(text)
        }
    }

    LaunchedEffect(messages.size, isResponding) {
        val lastIndex = messages.size - 1 + if (isResponding) 1 else 0
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Assistant") }) },
        bottomBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tell me what to do…") }
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell the assistant what to do")
                        }
                        try {
                            voiceLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            // No voice recognition app available on this device - typing still
                            // works, so this is a silent no-op rather than a crash.
                        }
                    }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Speak a command")
                    }
                    IconButton(onClick = {
                        if (input.isNotBlank()) {
                            viewModel.send(input)
                            input = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
                PillarBottomBar(current = Pillar.AI, onSelect = onSelectPillar)
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
            if (isResponding) {
                item { TypingIndicatorBubble() }
            }
        }
    }
}

/** Real user request, 2026-08: some indication the assistant is actually working on a response,
 * not just a blank gap between sending and the reply appearing - same bubble shape/alignment as a
 * real AI message so it reads as "the AI is about to say something" rather than a generic spinner. */
@Composable
private fun TypingIndicatorBubble() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun MessageBubble(message: AssistantMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(containerColor)
                .padding(12.dp)
        ) {
            Text(message.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
