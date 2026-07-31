package com.lifeos.expensecapture.ui.splitexpenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.AmountBody
import com.lifeos.expensecapture.ui.theme.AmountHero

/**
 * Split Expense detail - the one screen where the device owner actually does anything ongoing
 * with this feature: tap a person off as paid once they've settled up. See
 * SplitExpensesViewModel.toggleSettled's kdoc for why this is manual, not automatic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpenseDetailScreen(app: App, expenseId: Long, onBack: () -> Unit) {
    val viewModel = remember {
        SplitExpensesViewModel(
            splitExpenseDao = app.database.splitExpenseDao(),
            splitParticipantDao = app.database.splitParticipantDao()
        )
    }
    val rows by viewModel.rows.collectAsState()
    val row = rows.firstOrNull { it.expense.id == expenseId }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(row?.expense?.description ?: "Split Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (row == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("This expense was deleted.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "Total you paid",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("₹${"%.2f".format(row.expense.totalAmount)}", style = AmountHero)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (row.allSettled) {
                                    "Everyone's paid you back"
                                } else {
                                    "₹${"%.2f".format(row.owedToYou)} still owed to you"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(row.participants, key = { it.id }) { participant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = participant.settled,
                                onCheckedChange = { viewModel.toggleSettled(participant) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    participant.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textDecoration = if (participant.settled) TextDecoration.LineThrough else null,
                                    color = if (participant.settled) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    if (participant.settled) "Paid" else "Not paid yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("₹${"%.2f".format(participant.shareAmount)}", style = AmountBody)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && row != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this split expense?") },
            text = { Text("This removes it and everyone's share. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(row)
                    showDeleteConfirm = false
                    onBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
