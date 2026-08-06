package com.lifeos.expensecapture.family.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.WarningStrong

/**
 * Family "More" screen (2026-08, `ui3/` reference mockup) - the catch-all for every Family
 * destination that doesn't get its own bottom-nav tab (see FamilyPillarBottomBar's kdoc):
 * Members/Invite/Notifications plus the three shared modules without a tab of their own
 * (Documents/Health/Contacts). "Family Settings" and "Spending Limits" from the reference mockup
 * aren't built here - this app has no such features yet, and a row that opens nothing would be
 * worse than not having it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMoreScreen(
    onBack: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenHealth: () -> Unit,
    onOpenEmergencyContacts: () -> Unit,
    onSelectPillar: (FamilyPillar) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        bottomBar = { FamilyPillarBottomBar(current = FamilyPillar.MORE, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SosCard(onOpenSos) }

            item { SectionLabel("Family") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoreRow(Icons.Filled.Groups, "Manage Members", onOpenMembers)
                    MoreRow(Icons.Filled.PersonAdd, "Invite Members", onOpenInvite)
                    MoreRow(Icons.Filled.Notifications, "Notifications", onOpenNotifications)
                }
            }

            item { SectionLabel("Shared") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SharedModuleTile(Icons.Filled.Description, "Documents", onOpenDocuments, Modifier.weight(1f))
                    SharedModuleTile(Icons.Filled.MonitorHeart, "Health", onOpenHealth, Modifier.weight(1f))
                    SharedModuleTile(Icons.Filled.ContactPhone, "Contacts", onOpenEmergencyContacts, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SosCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarningStrong.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Emergency, contentDescription = null, tint = WarningStrong)
                Spacer(Modifier.width(8.dp))
                Text("SOS", style = MaterialTheme.typography.titleMedium, color = WarningStrong)
            }
            Text(
                "Share your live location with family instantly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = WarningStrong),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Send SOS") }
        }
    }
}

@Composable
private fun MoreRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SharedModuleTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
