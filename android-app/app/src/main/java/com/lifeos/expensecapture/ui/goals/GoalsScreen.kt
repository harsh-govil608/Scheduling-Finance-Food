package com.lifeos.expensecapture.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Goals PRD, Phase 3 Doc 12 - see GoalEntity.kt for the scope cuts (no AI suggestions, no
 * cross-pillar progress inference, no habit linkage).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember { GoalsViewModel(app.database.goalDao()) }
    val goals by viewModel.goals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add goal")
            }
        }
    ) { padding ->
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No goals yet. Tap + to set one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onToggle = { viewModel.toggleCompleted(goal) },
                        onDelete = { viewModel.delete(goal) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onConfirm = { title, targetDate, targetAmount ->
                viewModel.addGoal(title, targetDate, targetAmount)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = goal.completed, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (goal.completed) TextDecoration.LineThrough else null
                )
                goal.targetDate?.let { target ->
                    Text(
                        "Target: ${dateFormat.format(Date(target))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                goal.targetAmount?.let { amount ->
                    Text(
                        "₹${"%,.0f".format(amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete goal")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onConfirm: (title: String, targetDate: Long?, targetAmount: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetInDays by remember { mutableStateOf<Int?>(null) }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are you working toward?") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Target amount, optional (e.g. house down payment)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    TextButton(onClick = { targetInDays = null }) { Text(if (targetInDays == null) "• No target date" else "No target date") }
                    TextButton(onClick = { targetInDays = 30 }) { Text(if (targetInDays == 30) "• 1 month" else "1 month") }
                    TextButton(onClick = { targetInDays = 90 }) { Text(if (targetInDays == 90) "• 3 months" else "3 months") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val target = targetInDays?.let { days -> System.currentTimeMillis() + days * 86_400_000L }
                onConfirm(title, target, amountText.toDoubleOrNull())
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
