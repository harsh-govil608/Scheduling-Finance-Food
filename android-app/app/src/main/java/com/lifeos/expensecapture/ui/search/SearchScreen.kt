package com.lifeos.expensecapture.ui.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.ui.common.CategoryVisuals
import com.lifeos.expensecapture.ui.common.IconBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Search PRD, Phase 3 Doc 05, plus voice-to-search from the Voice Assistant PRD (Doc 06).
 * Voice scope is deliberately narrow: only search is voice-enabled, not pillar actions
 * (logging, task capture) - Doc 06 explicitly names Search as a shared-contract sibling, and
 * building full multi-pillar voice actions with confidence-gated no-screen confirmation is a
 * much larger surface than a single-device Finance pilot needs today. Uses Android's built-in
 * recognizer activity (shows its own transcription UI as the "confirm what was heard" step)
 * rather than a custom SpeechRecognizer integration - see day-2.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember { SearchViewModel(app.database.transactionDao(), app.database.categoryDao()) }
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!text.isNullOrBlank()) {
            viewModel.onQueryChange(text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Try \"zomato over 200\" or \"this month\"") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Search transactions")
                        }
                        try {
                            voiceLauncher.launch(intent)
                        } catch (e: ActivityNotFoundException) {
                            // No voice recognition app available on this device - text search
                            // still works, so this is a silent no-op rather than a crash.
                        }
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Search by voice")
                    }
                }
            )

            if (query.isBlank()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Search your transactions by merchant, category, amount (\"over 500\", " +
                            "\"under 100\"), or time (\"this week\", \"last month\", \"july\")."
                    )
                }
            } else if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No matching transactions.")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(results, key = { it.id }) { transaction ->
                        SearchResultRow(
                            transaction,
                            categoryName = categories.firstOrNull { it.id == transaction.categoryId }?.name
                                ?: "Uncategorized"
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** Category icon badge added (found via a real user report, 2026-07 - "the UI is looking too
 * basic") - same treatment as Ledger's TransactionRow, see CategoryVisuals' kdoc. */
@Composable
private fun SearchResultRow(transaction: TransactionEntity, categoryName: String) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (tint, container) = CategoryVisuals.colorPairFor(categoryName)
        IconBadge(icon = CategoryVisuals.iconFor(categoryName), tint = tint, containerColor = container, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.merchantRaw, style = MaterialTheme.typography.bodyLarge)
            val sign = if (transaction.direction == TransactionDirection.DEBIT) "-" else "+"
            Text(
                "$sign₹${"%.2f".format(transaction.amount)} · $categoryName · ${dateFormat.format(Date(transaction.date))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
