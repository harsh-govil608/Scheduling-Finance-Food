package com.lifeos.expensecapture.ui.productivity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.EntryRow
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment

/**
 * "Home" pillar landing surface - Task Management (Doc 10) + Habits (Doc 13) + Daily Planning
 * (Doc 14, folded in - see ProductivityHomeViewModel). Deliberately structured like Finance's
 * HomeScreen (a snapshot/list per feature, then entry points) so the two pillars feel like one
 * app, not two bolted-together prototypes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityHomeScreen(
    app: App,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenShopping: () -> Unit,
    onOpenTimeline: () -> Unit,
    onSelectPillar: (Pillar) -> Unit
) {
    val viewModel = remember {
        ProductivityHomeViewModel(
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) },
        bottomBar = { PillarBottomBar(current = Pillar.HOME, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenTasks),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.ListAlt, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, size = 32.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Due today", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                        if (uiState.todayTasks.isEmpty()) {
                            Text(
                                if (uiState.totalOpenTasks == 0) "Nothing on your list - tap to add something." else "Nothing due today, ${uiState.totalOpenTasks} open in total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.todayTasks.take(5).forEach { task ->
                                Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (uiState.todayTasks.size > 5) {
                                Text(
                                    "+${uiState.todayTasks.size - 5} more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenHabits),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer, size = 32.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Habits for today", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                        if (uiState.totalHabits == 0) {
                            Text(
                                "No habits yet - tap to start one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (uiState.pendingHabitsToday.isEmpty()) {
                            Text(
                                "All ${uiState.totalHabits} checked off for today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.pendingHabitsToday.take(5).forEach { habit ->
                                Text("• ${habit.name}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Explore") }
            item {
                EntryRow(
                    Icons.Filled.Flag, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                    "Goals", "Longer-term targets you're working toward", onOpenGoals
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Folder, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                    "Projects", "Group related tasks together", onOpenProjects
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Timeline, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Timeline", "Today, across Finance and Home together", onOpenTimeline
                )
            }
            item {
                EntryRow(
                    Icons.Filled.StickyNote2, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                    "Notes", "Quick things worth writing down", onOpenNotes
                )
            }
            item {
                EntryRow(
                    Icons.Filled.MenuBook, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                    "Journal", "A daily entry, for yourself", onOpenJournal
                )
            }
            item {
                EntryRow(
                    Icons.Filled.ShoppingCart, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Shopping", "A simple list for what you need to buy", onOpenShopping
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Assessment, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.secondaryContainer,
                    "Review", "How the last week or month went, in real numbers", onOpenReview
                )
            }
        }
    }
}
