package com.lifeos.expensecapture.ui.productivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.assistant.AiTextPolisher
import com.lifeos.expensecapture.ui.common.AiInsightCard
import com.lifeos.expensecapture.ui.common.ActionTile
import com.lifeos.expensecapture.ui.common.EntryRow
import com.lifeos.expensecapture.ui.common.GreetingTitle
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.ProfileAvatarButton
import com.lifeos.expensecapture.ui.common.ProgressRing
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar

/**
 * "Home" pillar landing surface - Task Management (Doc 10) + Habits (Doc 13) + Daily Planning
 * (Doc 14, folded in - see ProductivityHomeViewModel). Deliberately structured like Finance's
 * HomeScreen (a snapshot/list per feature, then entry points) so the two pillars feel like one
 * app, not two bolted-together prototypes - now sharing the exact same greeting header, and the
 * same "Today's Focus"/dashboard-card visual language, after the 2026-07-31 design refresh (see
 * Color.kt's kdoc).
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
    onOpenLedger: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectPillar: (Pillar) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        ProductivityHomeViewModel(
            context = context,
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao(),
            projectDao = app.database.projectDao(),
            goalDao = app.database.goalDao(),
            transactionDao = app.database.transactionDao(),
            budgetDao = app.database.budgetDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { GreetingTitle(uiState.displayName) },
                actions = {
                    ProfileAvatarButton(photoPath = uiState.profilePhotoPath, onClick = onOpenProfile)
                }
            )
        },
        bottomBar = { PillarBottomBar(current = Pillar.HOME, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TodaysFocusCard(
                    uiState = uiState,
                    onOpenTasks = onOpenTasks,
                    onOpenHabits = onOpenHabits
                )
            }

            // Daily Summary (2026-08 reference mockups, `ui2/` folder) - folds the habit-streak
            // engagement hook (moved here from Finance's Home, see ProductivityHomeUiState's
            // kdoc) in alongside spend/tasks/budget status rather than as a separate card, same
            // grouping the mockup uses.
            item { SectionLabel("Daily Summary") }
            item { DailySummaryRow(uiState) }

            item { SectionLabel("Quick actions") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ActionTile(Icons.Filled.Receipt, "Add Expense", onOpenLedger, Modifier.weight(1f))
                        ActionTile(Icons.Filled.NoteAdd, "Create Note", onOpenNotes, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ActionTile(Icons.Filled.PlaylistAdd, "Add Habit", onOpenHabits, Modifier.weight(1f))
                        ActionTile(Icons.Filled.CheckBoxOutlineBlank, "Add Task", onOpenTasks, Modifier.weight(1f))
                    }
                }
            }

            uiState.insight?.let { insight ->
                item {
                    // AI-polished phrasing (2026-08) - ProductivityInsightEngine's deterministic
                    // sentence stays the source of truth; Gemini only warms up the phrasing, with
                    // the plain sentence as the immediate/fallback value. See AiTextPolisher's kdoc.
                    val polished by produceState(initialValue = insight, insight) {
                        value = AiTextPolisher.polish(insight)
                    }
                    AiInsightCard(title = "AI Suggestions", body = polished)
                }
            }

            if (uiState.projects.isNotEmpty()) {
                item { SectionLabel("Projects") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        uiState.projects.take(2).forEach { row ->
                            val progress = if (row.totalTaskCount > 0) {
                                (row.totalTaskCount - row.openTaskCount).toFloat() / row.totalTaskCount
                            } else {
                                0f
                            }
                            Card(
                                modifier = Modifier.weight(1f).clickable(onClick = onOpenProjects),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        row.project.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        // Real user request (2026-08): a single project left half the row empty -
                        // fills that space with a small Tasks-vs-Habits-completed chart (real
                        // per-day counts, Home-pillar data - not a repeat of Finance's own spend
                        // chart) instead of a bare Spacer, so the row doesn't look unfinished.
                        if (uiState.projects.size == 1) {
                            MiniProductivityChartCard(
                                tasksCompleted = uiState.tasksCompletedLast7Days,
                                habitsCompleted = uiState.habitsCompletedLast7Days,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (uiState.goals.isNotEmpty()) {
                item { SectionLabel("Goal progress") }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.clickable(onClick = onOpenGoals)
                    ) {
                        // Real completion only (0%/100%) - GoalEntity has no numeric progress
                        // field to derive a partial ring from, see ProductivityHomeUiState's kdoc.
                        uiState.goals.take(4).forEach { goal ->
                            val progress = if (goal.completed) 1f else 0f
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                ProgressRing(progress = progress, modifier = Modifier.size(64.dp)) {
                                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    goal.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(64.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Explore") }
            item {
                EntryRow(
                    Icons.Filled.Timeline, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Timeline", "Today, across Finance and Home together", onOpenTimeline
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
                    Icons.Filled.Assessment, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant,
                    "Review", "How the last week or month went, in real numbers", onOpenReview
                )
            }
        }
    }
}

/**
 * Daily Summary row (2026-08 reference mockups, `ui2/` folder) - four icon tiles: today's real
 * spend (cross-read from Finance's own transactions, same figure NightSummary/Home already show),
 * open tasks, habit streak, and whether every set budget is currently within its limit. "On
 * Track" is null (rendered as a dash) when no budgets are set at all, rather than a fabricated
 * "on track" with nothing real to check it against.
 */
@Composable
private fun DailySummaryRow(uiState: ProductivityHomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        DailySummaryTile(
            icon = Icons.Filled.CurrencyRupee,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
            value = "₹${"%.0f".format(uiState.spentToday)}",
            label = "Spent Today",
            modifier = Modifier.weight(1f)
        )
        DailySummaryTile(
            icon = Icons.Filled.Assignment,
            iconTint = MaterialTheme.colorScheme.secondary,
            iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            value = "${uiState.totalOpenTasks}",
            label = "Tasks Left",
            modifier = Modifier.weight(1f)
        )
        DailySummaryTile(
            icon = Icons.Filled.LocalFireDepartment,
            iconTint = MaterialTheme.colorScheme.tertiary,
            iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            value = if (uiState.bestHabitStreak > 0) "${uiState.bestHabitStreak}-day" else "-",
            label = "Streak",
            modifier = Modifier.weight(1f)
        )
        DailySummaryTile(
            icon = Icons.Filled.CheckCircle,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
            value = when (uiState.allBudgetsOnTrack) {
                true -> "On Track"
                false -> "Over"
                null -> "-"
            },
            label = "Budgets",
            modifier = Modifier.weight(1f)
        )
    }
}

/** Fills the empty half of the Projects row when there's only one project (see the row's call
 * site kdoc) - a compact grouped-bar chart, real per-day Tasks-completed/Habits-completed counts
 * over the last 7 days (see ProductivityHomeUiState's kdoc), deliberately Home-pillar data rather
 * than a repeat of Finance's own spend chart elsewhere on this same screen. Plain Box bars instead
 * of a Canvas (like Sparkline/BarChart elsewhere) since two flat-colored bar pairs don't need one -
 * simpler to read at this card's small size too. */
@Composable
private fun MiniProductivityChartCard(
    tasksCompleted: List<Float>,
    habitsCompleted: List<Float>,
    modifier: Modifier = Modifier
) {
    val taskColor = MaterialTheme.colorScheme.primary
    val habitColor = MaterialTheme.colorScheme.tertiary
    val maxValue = (tasksCompleted + habitsCompleted).maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val totalThisWeek = (tasksCompleted.sum() + habitsCompleted.sum()).toInt()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "This week",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text("$totalThisWeek completed", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                tasksCompleted.indices.forEach { i ->
                    Row(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(((tasksCompleted[i] / maxValue).coerceIn(0f, 1f)).coerceAtLeast(0.04f))
                                .background(taskColor, RoundedCornerShape(2.dp))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(((habitsCompleted[i] / maxValue).coerceIn(0f, 1f)).coerceAtLeast(0.04f))
                                .background(habitColor, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniLegendDot(taskColor, "Tasks")
                MiniLegendDot(habitColor, "Habits")
            }
        }
    }
}

@Composable
private fun MiniLegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DailySummaryTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconBadge(icon = icon, tint = iconTint, containerColor = iconContainerColor, size = 32.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * The reference mockups' "Today's Focus" bordered card - Tasks Due and Habit Progress side by
 * side as two columns (2026-08 visual polish pass, matching the mockup's layout exactly instead
 * of the two sections being stacked one above the other), each with its own "View all" link
 * rather than a whole-card tap target (avoids nesting a clickable Row inside a clickable Card).
 * Habit Progress uses the same ring treatment as Goal Progress below (ProgressRing) instead of a
 * linear bar, matching the mockup's "1/1 done today" ring. Deliberately does NOT include a
 * "Schedule" section like the reference - TaskEntity has no time-of-day field, only a due *date*,
 * so a list of clock times would be fabricated, not real data (see ProductivityHomeUiState's
 * kdoc on the same principle for Goal Progress).
 */
@Composable
private fun TodaysFocusCard(
    uiState: ProductivityHomeUiState,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Today's Focus", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionLabel("Tasks due")
                    if (uiState.todayTasks.isEmpty()) {
                        Text(
                            if (uiState.totalOpenTasks == 0) "Nothing on your list." else "Nothing due today, ${uiState.totalOpenTasks} open in total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.todayTasks.take(5).forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(
                                    Icons.Filled.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(task.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (uiState.todayTasks.size > 5) {
                            Text(
                                "+${uiState.todayTasks.size - 5} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onOpenTasks, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                        Text("View all tasks")
                    }
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    SectionLabel("Habit progress")
                    if (uiState.totalHabits == 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No habits yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        ProgressRing(
                            progress = uiState.doneTodayHabitsCount.toFloat() / uiState.totalHabits,
                            modifier = Modifier.size(96.dp),
                            progressColor = accent
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${uiState.doneTodayHabitsCount}/${uiState.totalHabits}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "done today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onOpenHabits, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                        Text("View all habits")
                    }
                }
            }
        }
    }
}
