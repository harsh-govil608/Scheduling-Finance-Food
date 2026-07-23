package com.lifeos.expensecapture.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
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
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllRead() }) { Text("Mark all read") }
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
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationEntity, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val background = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(notification.title, style = MaterialTheme.typography.bodyLarge)
        Text(notification.body, style = MaterialTheme.typography.bodyMedium)
        Text(dateFormat.format(Date(notification.createdAt)), style = MaterialTheme.typography.bodySmall)
    }
}
