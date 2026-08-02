package com.lifeos.expensecapture.family.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyNotificationRepository
import com.lifeos.expensecapture.family.model.FamilyNotification
import com.lifeos.expensecapture.family.model.FamilyNotificationType
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.WarningStrong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Family notification center (2026-08 Family module PRD: "reminders, arrivals, departures,
 * medicine alerts, and bill due alerts"), plus SOS. Cross-device counterpart to the existing
 * local NotificationCenterScreen (see FamilyNotificationRepository's kdoc for the delivery
 * caveat - real-time only while the app is open, full push needs an FCM Cloud Function).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyNotificationCenterScreen(familyId: String, onBack: () -> Unit) {
    val repository = remember(familyId) { FamilyNotificationRepository() }
    val notifications by repository.observeAll(familyId).collectAsState(initial = emptyList())

    LaunchedEffect(notifications) {
        notifications.filter { !it.read }.forEach { repository.markRead(familyId, it.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Nothing yet - reminders, arrivals, medicine and bill alerts, and SOS notifications land here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationRow(notification)
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())

@Composable
private fun NotificationRow(notification: FamilyNotification) {
    val (icon, tint) = iconFor(notification.type)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(notification.title, style = MaterialTheme.typography.bodyLarge)
                Text(notification.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(timeFormat.format(Date(notification.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun iconFor(type: FamilyNotificationType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    FamilyNotificationType.REMINDER -> Icons.Filled.Event to MaterialTheme.colorScheme.primary
    FamilyNotificationType.ARRIVAL -> Icons.Filled.Login to MaterialTheme.colorScheme.primary
    FamilyNotificationType.DEPARTURE -> Icons.Filled.Logout to MaterialTheme.colorScheme.secondary
    FamilyNotificationType.MEDICINE -> Icons.Filled.Medication to MaterialTheme.colorScheme.tertiary
    FamilyNotificationType.BILL_DUE -> Icons.Filled.Payments to com.lifeos.expensecapture.ui.theme.Warning
    FamilyNotificationType.SOS -> Icons.Filled.Emergency to WarningStrong
}
