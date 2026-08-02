package com.lifeos.expensecapture.splitpay.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.model.ParticipantStatus
import com.lifeos.expensecapture.splitpay.model.SmartSplit
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

/**
 * Smart Split landing surface (2026-08) - "splits I created" (owed to me) and "splits I owe"
 * (someone else's, where I was matched as an app-user participant, see
 * SplitPayRepository.observeSplitsIOwe), one screen. Sits alongside the original local-only
 * Split Expenses list (SplitExpensesScreen) rather than replacing it - reached from there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSplitsScreen(onBack: () -> Unit, onCreate: () -> Unit, onOpenSplit: (String) -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }
    val uid = authRepository.currentUser?.uid ?: ""

    val mySplits by repository.observeMySplits(uid).collectAsState(initial = emptyList())
    val owedParticipations by repository.observeSplitsIOwe(uid).collectAsState(initial = emptyList())
    val pendingIOwe = owedParticipations.filter { it.status == ParticipantStatus.PENDING || it.status == ParticipantStatus.CLAIMED_PAID }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Split") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, contentDescription = "New smart split") }
        }
    ) { padding ->
        if (mySplits.isEmpty() && pendingIOwe.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No smart splits yet. Create one to auto-notify who owes you and let them " +
                        "pay you back via UPI - even if they don't have this app.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pendingIOwe.isNotEmpty()) {
                    item { com.lifeos.expensecapture.ui.common.SectionLabel("You owe") }
                    items(pendingIOwe, key = { "owe-${it.id}" }) { participant ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenSplit(participant.splitId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("₹${"%.2f".format(participant.shareAmount)}", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (participant.status == ParticipantStatus.CLAIMED_PAID) "Waiting for confirmation" else "Tap to settle up",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (mySplits.isNotEmpty()) {
                    item { com.lifeos.expensecapture.ui.common.SectionLabel("Owed to you") }
                    items(mySplits, key = { it.id }) { split ->
                        SmartSplitRow(split, onClick = { onOpenSplit(split.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartSplitRow(split: SmartSplit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(split.description, style = MaterialTheme.typography.bodyLarge)
            Text("₹${"%.2f".format(split.totalAmount)} total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
