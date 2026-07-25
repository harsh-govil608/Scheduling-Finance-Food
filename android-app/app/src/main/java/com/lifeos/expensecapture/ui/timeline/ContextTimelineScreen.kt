package com.lifeos.expensecapture.ui.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import com.lifeos.expensecapture.App
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Context Timeline PRD, Phase 3 Doc 32 - see ContextTimelineViewModel.kt for the scope cuts
 * (a plain chronological merge, no AI-driven "why this matters" inference). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextTimelineScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        ContextTimelineViewModel(
            transactionDao = app.database.transactionDao(),
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao()
        )
    }
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today, across everything") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing captured yet today - spend, tasks, and habits will show up here as they happen.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                    val pillarColor = if (entry.pillar == TimelinePillar.FINANCE) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(pillarColor)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            dateFormat.format(Date(entry.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(entry.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
