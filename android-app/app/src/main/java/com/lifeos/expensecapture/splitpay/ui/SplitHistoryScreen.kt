package com.lifeos.expensecapture.splitpay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.model.SplitHistoryEntry
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Deleted-split history (2026-08, real user request: "add history to store records of it and
 * automatically delete after 1 month" - see SplitHistoryEntry's kdoc). Read-only - there's nothing
 * to act on here, just a record of what a deleted split was. Triggers
 * [SplitPayRepository.pruneOldSplitHistory] on load so entries older than 30 days are gone by the
 * time this list is shown, rather than needing a background job this app has no infrastructure for. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitHistoryScreen(onBack: () -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }
    val uid = authRepository.currentUser?.uid ?: ""

    LaunchedEffect(uid) {
        repository.pruneOldSplitHistory(uid)
    }

    val history by remember(uid) { repository.observeSplitHistory(uid) }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No deleted splits yet. Splits you delete show up here for 30 days, then they're " +
                        "cleared out automatically.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { entry ->
                    SplitHistoryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun SplitHistoryRow(entry: SplitHistoryEntry) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(entry.description.ifBlank { "Split" }, style = MaterialTheme.typography.bodyLarge)
            Text(
                "₹${"%.2f".format(entry.totalAmount)} - with ${entry.participantNames}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Deleted ${dateFormat.format(Date(entry.deletedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
