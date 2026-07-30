package com.lifeos.expensecapture.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

/**
 * Habits PRD, Phase 3 Doc 13 - see HabitsViewModel.kt for the supportive-streak requirement this
 * implements. `streakLine` below is the concrete "never shame-coded" enforcement point: a lapsed
 * streak is never rendered as "broken" or "0-day streak," only as an invitation to resume.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        HabitsViewModel(
            habitDao = app.database.habitDao(),
            completionDao = app.database.habitCompletionDao()
        )
    }
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No habits yet. Tap + to start one - small and specific works best.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits, key = { it.habit.id }) { row ->
                    HabitCard(
                        row = row,
                        onToggle = { viewModel.toggleToday(row.habit, row.doneToday) },
                        onArchive = { viewModel.archive(row.habit) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onConfirm = { name -> viewModel.addHabit(name); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

private fun streakLine(row: HabitRow): String = when {
    row.currentStreak > 0 -> "${row.currentStreak} day${if (row.currentStreak == 1) "" else "s"} in a row"
    row.doneToday -> "First day - nice start"
    else -> "Ready when you are"
}

@Composable
private fun HabitCard(row: HabitRow, onToggle: () -> Unit, onArchive: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    if (row.doneToday) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (row.doneToday) "Mark not done today" else "Mark done today",
                    tint = if (row.doneToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(row.habit.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    streakLine(row),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onArchive) { Text("Archive") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New habit") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("e.g. Drink water, Stretch, Read") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
