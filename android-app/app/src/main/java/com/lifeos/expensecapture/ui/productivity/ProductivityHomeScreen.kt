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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.assistant.AiTextPolisher
import com.lifeos.expensecapture.data.db.entity.TaskPriority
import com.lifeos.expensecapture.ui.common.ActionTile
import com.lifeos.expensecapture.ui.common.GreetingTitle
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.ProfileAvatarButton
import com.lifeos.expensecapture.ui.common.ProgressRing
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong

/**
 * "Home" pillar landing surface - Task Management (Doc 10) + Habits (Doc 13) + Daily Planning
 * (Doc 14, folded in - see ProductivityHomeViewModel). Redesigned 2026-08 to match the `ui3/`
 * reference mockups (Today Score card, AI Suggestions as a real tip list, priority chips on
 * Today's Focus, Explore as a grid) - see ProductivityHomeUiState's kdoc for what's real vs.
 * deliberately left out (no OCR "Scan Bill" quick action - not built, would be a non-functional
 * button; no Family-spend tile - a separate follow-up, not in this reference).
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
            item { TodayScoreCard(uiState.todayScore, onOpenReview) }

            if (uiState.tips.isNotEmpty()) {
                item { AiSuggestionsCard(uiState.tips) }
            }

            item {
                TodaysFocusCard(
                    uiState = uiState,
                    onOpenTasks = onOpenTasks,
                    onOpenHabits = onOpenHabits
                )
            }

            item { SectionLabel("Explore") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ExploreTile(
                            Icons.Filled.Timeline, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                            "Timeline", onOpenTimeline, Modifier.weight(1f)
                        )
                        ExploreTile(
                            Icons.Filled.Folder, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                            "Projects", onOpenProjects, Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ExploreTile(
                            Icons.Filled.MenuBook, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                            "Journal", onOpenJournal, Modifier.weight(1f)
                        )
                        ExploreTile(
                            Icons.Filled.ShoppingCart, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                            "Shopping", onOpenShopping, Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ExploreTile(
                            Icons.Filled.Assessment, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant,
                            "Review", onOpenReview, Modifier.weight(1f)
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

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
        }
    }
}

/**
 * "Today Score" card (2026-08, `ui3/` reference) - the average of ProductivityHomeViewModel's
 * real task/habit completion signals (see ProductivityHomeUiState.todayScore's kdoc), rendered as
 * a ring + status chip + a message tiered off the same score, not a separate judgement - same
 * "no fabricated number" discipline as FinancialHealthScoreCard in Analytics, just for the Home
 * pillar's own tasks/habits instead of finance. Null (nothing due/completed today, no habits at
 * all) shows an honest empty state rather than a fake 100%.
 */
@Composable
private fun TodayScoreCard(score: Int?, onViewInsights: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Today Score", style = MaterialTheme.typography.titleMedium)
                }
                if (score != null) {
                    val (statusText, statusColor) = when {
                        score >= 70 -> "On Track" to accent
                        score >= 40 -> "Catching Up" to Warning
                        else -> "Behind" to WarningStrong
                    }
                    StatusChipLabel(statusText, statusColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            if (score == null) {
                Text(
                    "Nothing due or completed yet today - once you've got a task, habit, or two, " +
                        "this fills in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val ringColor = when {
                    score >= 70 -> accent
                    score >= 40 -> Warning
                    else -> WarningStrong
                }
                val message = when {
                    score >= 70 -> "Great job! You're doing well across your tasks and habits."
                    score >= 40 -> "Making progress - a few more today would help."
                    else -> "Today's tasks and habits could use some attention."
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(progress = score / 100f, modifier = Modifier.size(88.dp), progressColor = ringColor) {
                        Text("$score%", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onViewInsights, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Text("View Insights")
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusChipLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * "AI Suggestions" card (2026-08, `ui3/` reference - redesigned from a single AiInsightCard
 * paragraph into a short tappable-looking tip list). Each tip's deterministic sentence (see
 * ProductivityHomeUiState.tips' kdoc: up to 3 independently real, already-computed signals, never
 * one AI call inventing several) is now individually AI-polished via AiTextPolisher, the same
 * "real facts, AI only rewords" pattern already used elsewhere (Budget, Finance Home, Family
 * Dashboard) - see AiTextPolisher's kdoc. Real bug fix (founder question, 2026-08: "is AI
 * connected with Home tab in AI suggestions?"): this card carried a "Live" badge and an "AI
 * Suggestions" title over 100% deterministic text with no AI call anywhere in its pipeline -
 * honest per the kdoc a developer would read, but misleading to a user who has no way to tell
 * that from the UI alone.
 */
@Composable
private fun AiSuggestionsCard(tips: List<HomeTip>) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Suggestions", style = MaterialTheme.typography.titleMedium)
                StatusChipLabel("Live", accent)
            }
            tips.forEach { tip ->
                val (icon, tint, container) = when (tip.kind) {
                    HomeTipKind.BUDGET -> Triple(Icons.Filled.AccountBalanceWallet, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    HomeTipKind.TASK -> Triple(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
                    HomeTipKind.INSIGHT -> Triple(Icons.Filled.AutoAwesome, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBadge(icon = icon, tint = tint, containerColor = container, size = 32.dp)
                    Spacer(Modifier.width(12.dp))
                    val polished by produceState(initialValue = tip.text, tip.text) {
                        value = AiTextPolisher.polish(tip.text)
                    }
                    Text(polished, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
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
private fun MiniLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * "Today's Focus" card (2026-08, `ui3/` reference) - a task list with a priority chip per task
 * (TaskEntity.priority, real and already tracked, just not previously shown on Home) and a
 * "N tasks planned" count that matches the list below it exactly (both read off the same
 * uiState.todayTasks). The floating add button and "View all tasks" both open the same Tasks
 * screen - there's no separate inline add-task flow on Home itself.
 *
 * Habits list re-added below the tasks (2026-08 - founder feedback: the habit ring this card used
 * to show was dropped on the assumption Today Score's aggregate covered it, but that hid which
 * *specific* habits are still open, which the old ring/list did show). Read-only like the task
 * rows above - tapping a row or "View all habits" opens the real Habits screen, same "no inline
 * completion on Home" pattern as tasks.
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Today's Focus", style = MaterialTheme.typography.titleMedium)
                if (uiState.todayTasks.isNotEmpty()) {
                    Text(
                        "${uiState.todayTasks.size} task${if (uiState.todayTasks.size == 1) "" else "s"} planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (uiState.todayTasks.isEmpty()) {
                Text(
                    if (uiState.totalOpenTasks == 0) "Nothing on your list." else "Nothing due today, ${uiState.totalOpenTasks} open in total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.todayTasks.take(5).forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        PriorityChip(task.priority)
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

            if (uiState.totalHabits > 0) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Habits", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${uiState.doneTodayHabitsCount}/${uiState.totalHabits} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (uiState.pendingHabitsToday.isEmpty()) {
                    Text(
                        "All habits done for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.pendingHabitsToday.take(5).forEach { habit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenHabits).padding(vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Filled.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                habit.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (uiState.pendingHabitsToday.size > 5) {
                        Text(
                            "+${uiState.pendingHabitsToday.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onOpenTasks, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Text("View all tasks")
                }
                IconButton(
                    onClick = onOpenTasks,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(accent)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

/** Same color mapping TaskListScreen's own priorityColor already uses, kept as a small local
 * duplicate rather than a shared extraction - one three-branch `when` isn't worth a new shared
 * file for two call sites. */
@Composable
private fun PriorityChip(priority: TaskPriority) {
    val color = when (priority) {
        TaskPriority.LOW -> Color(0xFF6B8F71)
        TaskPriority.MEDIUM -> Warning
        TaskPriority.HIGH -> WarningStrong
    }
    StatusChipLabel(priority.name.lowercase().replaceFirstChar { it.uppercase() }, color)
}

@Composable
private fun ExploreTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    container: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            IconBadge(icon = icon, tint = tint, containerColor = container, size = 36.dp)
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
