package com.lifeos.expensecapture.ui.shopping

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App

/** Shopping PRD, Phase 3 Doc 36 - see ShoppingItemEntity.kt for scope cuts. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember { ShoppingViewModel(app.database.shoppingItemDao()) }
    val items by viewModel.items.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (suggestions.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions, key = { it.name }) { suggestion ->
                        // AI Transformation Plan F2: a soft suggestion from real buying cadence,
                        // not a prediction stated as fact - and never auto-added, tapping is what
                        // adds it (reuses the normal addItem path via acceptSuggestion).
                        AssistChip(
                            onClick = { viewModel.acceptSuggestion(suggestion) },
                            label = {
                                Text("${suggestion.name} · last bought ${suggestion.daysSinceLastBought}d ago")
                            }
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your list is empty. Tap + to add something.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleChecked(item) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = item.checked, onCheckedChange = { viewModel.toggleChecked(item) })
                            Text(
                                if (item.quantity.isBlank()) item.name else "${item.name} (${item.quantity})",
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                                color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.delete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete item")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        val voiceLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val heard = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!heard.isNullOrBlank()) name = heard
        }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the item")
                                }
                                try {
                                    voiceLauncher.launch(intent)
                                } catch (e: ActivityNotFoundException) {
                                    // No voice recognition app on this device - typing still works.
                                }
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Speak to add")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addItem(name, quantity); showAddDialog = false }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}
