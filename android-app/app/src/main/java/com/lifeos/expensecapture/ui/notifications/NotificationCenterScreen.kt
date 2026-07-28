package com.lifeos.expensecapture.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // weight() resolves per-receiver (RowScope/ColumnScope);
// importing it by name alone resolved to an internal symbol during the real build - see
// android-app/README.md "Known gaps" if this surfaces again after a Compose version bump.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import com.lifeos.expensecapture.ui.common.CategoryVisuals
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.NotificationVisuals
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Notification Center PRD, Phase 3 Doc 03: the durable, reviewable inbox. Single-device scope
 * only (no cross-device read-state sync, no arbitration-fed digests) - see day-2.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(app: App, onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val viewModel = remember { NotificationCenterViewModel(app.database.notificationDao()) }
    val notifications by viewModel.notifications.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (notifications.any { !it.isRead }) {
                                DropdownMenuItem(
                                    text = { Text("Mark all read") },
                                    onClick = { viewModel.markAllRead(); menuExpanded = false }
                                )
                            }
                            // Bug fix (found via a real user report, 2026-07): there used to be no
                            // way to clear the inbox at all, so it only ever grew. Soft-dismisses
                            // everything currently visible - see NotificationEntity.isDismissed's
                            // kdoc for why this doesn't touch the underlying rows' cooldown tracking.
                            DropdownMenuItem(
                                text = { Text("Clear all") },
                                onClick = { viewModel.clearAll(); menuExpanded = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing yet - bill/subscription/budget alerts and your daily recap will show up here.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = {
                            viewModel.markRead(notification)
                            onNavigateTo(notification.deepLinkRoute)
                        },
                        onDismiss = { viewModel.dismiss(notification) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationEntity, onClick: () -> Unit, onDismiss: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val background = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon badge added (found via a real user report, 2026-07 - "the UI is looking too
        // basic"): this row had zero visual differentiation between notification types beyond
        // a read/unread tint - see NotificationVisuals' kdoc.
        val (tint, container) = CategoryVisuals.colorPairFor(notification.type.name)
        IconBadge(icon = NotificationVisuals.iconFor(notification.type), tint = tint, containerColor = container, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.bodyLarge)
            Text(notification.body, style = MaterialTheme.typography.bodyMedium)
            Text(dateFormat.format(Date(notification.createdAt)), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = "Remove notification")
        }
    }
}
