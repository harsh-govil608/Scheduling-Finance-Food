package com.lifeos.expensecapture.family.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.assistant.AiTextPolisher
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.FamilyRole
import com.lifeos.expensecapture.family.model.MemberPresence
import com.lifeos.expensecapture.family.model.PresenceStatus
import com.lifeos.expensecapture.family.ui.FamilyPillar
import com.lifeos.expensecapture.family.ui.FamilyPillarBottomBar
import com.lifeos.expensecapture.ui.analytics.ChartLegendRow
import com.lifeos.expensecapture.ui.analytics.DonutChart
import com.lifeos.expensecapture.ui.analytics.DonutSlice
import com.lifeos.expensecapture.ui.common.AiInsightCard
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.WarningStrong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Family Dashboard (2026-08 Family module, restyled 2026-08 to match `ui3/` reference mockups) -
 * member status row with presence, a real "Total Family Spend Today" card (vs. yesterday, see
 * FamilyDashboardViewModel's kdoc), recent activity off the real event stream, an upcoming
 * preview, a deterministic AI insight, and a Quick Actions row into the modules people add things
 * to most (Expenses/Tasks/Calendar - all real destinations, not fabricated one-tap creators, since
 * this dashboard doesn't own an add-flow itself). The other three shared modules
 * (Documents/Health/Contacts) plus Members/Invite/SOS/Notifications live under More - see
 * FamilyPillarBottomBar's kdoc for why only three modules get their own tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(
    familyId: String,
    currentUserId: String,
    onOpenTasks: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenNotifications: () -> Unit,
    onBackToFinance: () -> Unit,
    onSelectPillar: (FamilyPillar) -> Unit
) {
    val viewModel = remember(familyId) { FamilyDashboardViewModel(familyId, currentUserId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.family?.name ?: "Family") },
                navigationIcon = {
                    IconButton(onClick = onBackToFinance) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Finance")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenInvite) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Invite members")
                    }
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = { FamilyPillarBottomBar(current = FamilyPillar.HOME, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { GreetingCard(uiState.currentMember?.displayName ?: "") }

            item {
                SosBanner(onClick = onOpenSos)
            }

            if (uiState.members.isNotEmpty()) {
                item { SectionLabel("Family Members") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.members, key = { it.userId }) { member ->
                            MemberStatusColumn(
                                member = member,
                                presence = uiState.presence.firstOrNull { it.userId == member.userId },
                                onClick = onOpenTasks
                            )
                        }
                    }
                }
            }

            if (uiState.totalFamilySpendToday > 0) {
                item { SectionLabel("Family Expense Tracker") }
                item {
                    FamilySpendTodayCard(
                        total = uiState.totalFamilySpendToday,
                        yesterday = uiState.totalFamilySpendYesterday,
                        byMember = uiState.spendByMemberToday
                    )
                }
            }

            uiState.insight?.let { insight ->
                item {
                    // AI-polished phrasing (2026-08) - buildInsight()'s deterministic sentence
                    // stays the source of truth; the AI call only warms up the phrasing, with the
                    // plain sentence as the immediate/fallback value. See AiTextPolisher's kdoc.
                    val polished by produceState(initialValue = insight, insight) {
                        value = AiTextPolisher.polish(insight)
                    }
                    AiInsightCard(title = "Family Insight", body = polished)
                }
            }

            if (uiState.upcomingTasks.isNotEmpty() || uiState.upcomingCalendarEvents.isNotEmpty()) {
                item { SectionLabel("Upcoming") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            uiState.upcomingTasks.take(3).forEach { task ->
                                Text("• ${task.title}", style = MaterialTheme.typography.bodyMedium)
                            }
                            uiState.upcomingCalendarEvents.take(3).forEach { event ->
                                Text("• ${event.title}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item { SectionLabel("Quick Actions") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionTile(Icons.Filled.AccountBalanceWallet, "Add Expense", onOpenExpenses, Modifier.weight(1f))
                    QuickActionTile(Icons.Filled.Checklist, "Add Task", onOpenTasks, Modifier.weight(1f))
                    QuickActionTile(Icons.Filled.CalendarMonth, "Add Event", onOpenCalendar, Modifier.weight(1f))
                }
            }

            if (uiState.recentEvents.isNotEmpty()) {
                item { SectionLabel("Recent Activity") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column {
                            uiState.recentEvents.take(10).forEach { event ->
                                ActivityRow(event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingCard(displayName: String) {
    val hour = remember { java.time.LocalTime.now().hour }
    val timeOfDay = when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    Column {
        Text("$timeOfDay, ${displayName.ifBlank { "there" }}", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Here's what's happening in your family.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Family Expense Tracker (2026-08 real user request: "Total Family Spend Today... a clean pie
 * chart breaking down the entire household's spending") - real data synced from each member's own
 * SMS-auto-capture, see FamilyDashboardViewModel's kdoc on the todayLedgerEntries flow this reads.
 * Reuses the same DonutChart/ChartLegendRow Analytics already built rather than a second chart
 * implementation - only the data source differs. The "vs yesterday" line only renders when
 * yesterday actually has spend to compare against - see totalFamilySpendYesterday's kdoc. */
@Composable
private fun FamilySpendTodayCard(total: Double, yesterday: Double?, byMember: List<FamilySpendSlice>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Total Family Spend Today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("₹${"%.2f".format(total)}", style = MaterialTheme.typography.headlineMedium)
                if (yesterday != null) {
                    val percentChange = ((total - yesterday) / yesterday) * 100
                    val lessOrMore = if (percentChange <= 0) "less" else "more"
                    val color = if (percentChange <= 0) MaterialTheme.colorScheme.primary else WarningStrong
                    Text(
                        "  ${"%.0f".format(kotlin.math.abs(percentChange))}% $lessOrMore than yesterday",
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            if (byMember.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                val colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.secondary,
                    com.lifeos.expensecapture.ui.theme.Warning,
                    WarningStrong,
                    MaterialTheme.colorScheme.error
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        slices = byMember.mapIndexed { index, slice -> DonutSlice(slice.memberName, slice.amount, colors[index % colors.size]) },
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        byMember.forEachIndexed { index, slice ->
                            ChartLegendRow(
                                color = colors[index % colors.size],
                                label = slice.memberName,
                                valueText = "₹${"%.0f".format(slice.amount)}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SosBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(WarningStrong.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Emergency, contentDescription = null, tint = WarningStrong)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("SOS", style = MaterialTheme.typography.titleMedium, color = WarningStrong)
            Text(
                "Share your live location with the family instantly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemberStatusColumn(member: FamilyMember, presence: MemberPresence?, onClick: () -> Unit) {
    val statusColor = when (presence?.status) {
        PresenceStatus.ONLINE -> MaterialTheme.colorScheme.primary
        PresenceStatus.AWAY -> com.lifeos.expensecapture.ui.theme.Warning
        else -> MaterialTheme.colorScheme.outline
    }
    Column(
        modifier = Modifier.clickable(onClick = onClick).width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Real user report, 2026-08: show whatever photo the member set in their own
                // Profile screen (see ProfileViewModel.setProfilePhoto's Family sync), falling
                // back to initials for members who haven't set one.
                if (member.photoUrl != null) {
                    AsyncImage(
                        model = member.photoUrl,
                        contentDescription = member.displayName,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                } else {
                    Text(member.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleMedium)
                }
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            // Owner badge (2026-08, `ui3/` reference shows a crown on the admin's avatar) - real
            // FamilyRole.OWNER field, not a fabricated "admin" concept layered on top.
            if (member.role == FamilyRole.OWNER) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Owner",
                    tint = com.lifeos.expensecapture.ui.theme.Warning,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            member.displayName.ifBlank { "Member" },
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(member.role.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickActionTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge(icon = icon, tint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val activityTimeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())

@Composable
private fun ActivityRow(event: FamilyEvent) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column {
            Text(activityText(event), style = MaterialTheme.typography.bodyMedium)
            Text(
                activityTimeFormat.format(Date(event.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Turns a FamilyEvent into a human-readable line - the same structured data a future
 * natural-language summary would read, just formatted with plain string templates here rather
 * than a model. */
private fun activityText(event: FamilyEvent): String {
    val name = event.actorName.ifBlank { "Someone" }
    return when (event.type) {
        com.lifeos.expensecapture.family.model.FamilyEventType.FAMILY_CREATED -> "$name created the family"
        com.lifeos.expensecapture.family.model.FamilyEventType.MEMBER_JOINED -> "$name joined the family"
        com.lifeos.expensecapture.family.model.FamilyEventType.MEMBER_ROLE_CHANGED -> "$name changed a member's role"
        com.lifeos.expensecapture.family.model.FamilyEventType.MEMBER_PERMISSIONS_CHANGED -> "$name updated permissions"
        com.lifeos.expensecapture.family.model.FamilyEventType.TASK_CREATED -> "$name added a task${event.payload["title"]?.let { ": $it" } ?: ""}"
        com.lifeos.expensecapture.family.model.FamilyEventType.TASK_COMPLETED -> "$name completed a task${event.payload["title"]?.let { ": $it" } ?: ""}"
        com.lifeos.expensecapture.family.model.FamilyEventType.CALENDAR_EVENT_CREATED -> "$name added an event${event.payload["title"]?.let { ": $it" } ?: ""}"
        com.lifeos.expensecapture.family.model.FamilyEventType.EXPENSE_ADDED -> "$name added an expense${event.payload["description"]?.let { ": $it" } ?: ""}"
        com.lifeos.expensecapture.family.model.FamilyEventType.DOCUMENT_ADDED -> "$name uploaded a document${event.payload["title"]?.let { ": $it" } ?: ""}"
        com.lifeos.expensecapture.family.model.FamilyEventType.HEALTH_RECORD_ADDED -> "$name added a health record"
        com.lifeos.expensecapture.family.model.FamilyEventType.EMERGENCY_CONTACT_ADDED -> "$name added an emergency contact"
        com.lifeos.expensecapture.family.model.FamilyEventType.SOS_TRIGGERED -> "$name triggered an SOS alert"
        com.lifeos.expensecapture.family.model.FamilyEventType.SOS_RESOLVED -> "$name resolved the SOS alert"
        com.lifeos.expensecapture.family.model.FamilyEventType.MEMBER_ARRIVED -> "$name arrived"
        com.lifeos.expensecapture.family.model.FamilyEventType.MEMBER_DEPARTED -> "$name departed"
        else -> "$name did something"
    }
}
