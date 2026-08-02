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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.MemberPresence
import com.lifeos.expensecapture.family.model.PresenceStatus
import com.lifeos.expensecapture.ui.common.AiInsightCard
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.WarningStrong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Family Dashboard (2026-08 Family module) - member status grid with presence, recent activity
 * off the real event stream, an upcoming preview (tasks due + calendar events in the next 7
 * days), a deterministic AI insight, and entry points into the six shared modules plus SOS and
 * invites. See FamilyDashboardViewModel's kdoc for where each of those numbers comes from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(
    familyId: String,
    currentUserId: String,
    onOpenTasks: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenEmergencyContacts: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val viewModel = remember(familyId) { FamilyDashboardViewModel(familyId, currentUserId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.family?.name ?: "Family") },
                actions = {
                    IconButton(onClick = onOpenInvite) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Invite members")
                    }
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
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
                SosBanner(onClick = onOpenSos)
            }

            if (uiState.members.isNotEmpty()) {
                item { SectionLabel("Family") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.members, key = { it.userId }) { member ->
                            MemberStatusColumn(
                                member = member,
                                presence = uiState.presence.firstOrNull { it.userId == member.userId },
                                onClick = onOpenMembers
                            )
                        }
                    }
                }
            }

            uiState.insight?.let { insight ->
                item { AiInsightCard(title = "Family Insight", body = insight) }
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

            item { SectionLabel("Shared") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        SharedModuleTile(Icons.Filled.Checklist, "Tasks", onOpenTasks, Modifier.weight(1f))
                        SharedModuleTile(Icons.Filled.CalendarMonth, "Calendar", onOpenCalendar, Modifier.weight(1f))
                        SharedModuleTile(Icons.Filled.AccountBalanceWallet, "Expenses", onOpenExpenses, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        SharedModuleTile(Icons.Filled.Description, "Documents", onOpenDocuments, Modifier.weight(1f))
                        SharedModuleTile(Icons.Filled.MonitorHeart, "Health", onOpenHealth, Modifier.weight(1f))
                        SharedModuleTile(Icons.Filled.ContactPhone, "Contacts", onOpenEmergencyContacts, Modifier.weight(1f))
                    }
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
                Text(member.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleMedium)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(statusColor)
            )
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
private fun SharedModuleTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge(icon = icon, tint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
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
