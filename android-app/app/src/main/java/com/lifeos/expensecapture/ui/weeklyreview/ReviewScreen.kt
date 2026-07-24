package com.lifeos.expensecapture.ui.weeklyreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App

/** Weekly/Monthly Review, Phase 3 Docs 15/16 - see ReviewViewModel.kt for the real-numbers-only
 * scope (no AI-generated narrative). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        ReviewViewModel(
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.period == ReviewPeriod.WEEK,
                        onClick = { viewModel.selectPeriod(ReviewPeriod.WEEK) },
                        label = { Text("Last 7 days") }
                    )
                    FilterChip(
                        selected = uiState.period == ReviewPeriod.MONTH,
                        onClick = { viewModel.selectPeriod(ReviewPeriod.MONTH) },
                        label = { Text("Last 30 days") }
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tasks", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${uiState.tasksCompleted} completed, ${uiState.tasksCreated} added",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Habits", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                uiState.activeHabitCount == 0 -> "No habits tracked yet"
                                uiState.habitMaintenancePercent != null -> "Maintained ${uiState.habitMaintenancePercent}% of days across ${uiState.activeHabitCount} habit${if (uiState.activeHabitCount == 1) "" else "s"}"
                                else -> "Not enough history yet for this period"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
