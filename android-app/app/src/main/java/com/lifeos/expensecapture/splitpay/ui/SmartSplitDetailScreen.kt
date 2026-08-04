package com.lifeos.expensecapture.splitpay.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.model.ParticipantStatus
import com.lifeos.expensecapture.splitpay.model.SmartSplitParticipant
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.AmountHero
import kotlinx.coroutines.launch

/** GitHub Pages base for the Track B web payment page (docs/pay/index.html in this repo) - update
 * if the repo/Pages path ever changes. Query params `s` (split id) and `p` (participant id) tell
 * the page which Firestore doc to read; see docs/pay/index.html's own comments. */
private const val PAY_WEB_BASE_URL = "https://harsh-govil608.github.io/Scheduling-Finance-Food/pay/"

/**
 * Smart Split detail (2026-08) - "Phase 2/3" of the feature request. Two different experiences
 * depending on who's looking:
 *  - The payer sees every participant's status, and a "Confirm received" action once someone
 *    (app-user or external) has marked themselves paid - Phase 3's closing loop.
 *  - A participant who IS a signed-in app user and owes a share sees their own row's "Settle Up"
 *    (real UPI intent, Track A). External participants never open this screen directly - they use
 *    the shared web link instead (Track B), which is its own static page, not this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSplitDetailScreen(splitId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    // remember()'d keyed on splitId - see the same fix in SmartSplitsScreen.kt (an inline
    // observeX().collectAsState() recreates the Firestore listener, and resets to its initial
    // value, on every recomposition rather than just when splitId changes).
    val split by remember(splitId) { repository.observeSplit(splitId) }.collectAsState(initial = null)
    val participants by remember(splitId) { repository.observeParticipants(splitId) }.collectAsState(initial = emptyList())
    val isPayer = split?.payerId == currentUserId
    val myParticipantRow = participants.firstOrNull { it.participantUserId == currentUserId }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val participant = myParticipantRow ?: return@rememberLauncherForActivityResult
        // UpiPay.isSuccessResult exists for a future stricter flow, but isn't trusted here:
        // launching the UPI app at all means the user attempted payment, so this always marks
        // PAID_VIA_UPI regardless of what (if anything) the UPI app's response contains -
        // matching this screen's own kdoc on why a manual "I've already paid" fallback exists too.
        coroutineScope.launch {
            repository.updateParticipantStatus(splitId, participant.id, ParticipantStatus.PAID_VIA_UPI)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(split?.description ?: "Smart Split") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        val currentSplit = split
        if (currentSplit == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Loading…")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${"%.2f".format(currentSplit.totalAmount)}", style = AmountHero)
                            Text(
                                if (isPayer) "Paid by you" else "Paid by ${currentSplit.payerName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(participants, key = { it.id }) { participant ->
                    ParticipantCard(
                        participant = participant,
                        isMine = participant.participantUserId == currentUserId,
                        isPayer = isPayer,
                        onSettleUp = {
                            val vpa = currentSplit.payerUpiId
                            if (vpa.isNotBlank()) {
                                upiLauncher.launch(UpiPay.payIntent(vpa, currentSplit.payerName, participant.shareAmount, currentSplit.description))
                            }
                        },
                        onMarkPaidManually = {
                            coroutineScope.launch {
                                repository.updateParticipantStatus(splitId, participant.id, ParticipantStatus.PAID_VIA_UPI)
                            }
                        },
                        onConfirmReceived = {
                            coroutineScope.launch {
                                repository.updateParticipantStatus(splitId, participant.id, ParticipantStatus.CONFIRMED)
                            }
                        },
                        onShareLink = {
                            val link = "$PAY_WEB_BASE_URL?s=$splitId&p=${participant.id}"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Hi ${participant.name}, you owe ₹${"%.2f".format(participant.shareAmount)} " +
                                        "for \"${currentSplit.description}\". Pay here: $link"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share payment link"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    participant: SmartSplitParticipant,
    isMine: Boolean,
    isPayer: Boolean,
    onSettleUp: () -> Unit,
    onMarkPaidManually: () -> Unit,
    onConfirmReceived: () -> Unit,
    onShareLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(participant.name, style = MaterialTheme.typography.bodyLarge)
                    Text(statusText(participant), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹${"%.2f".format(participant.shareAmount)}", style = MaterialTheme.typography.bodyLarge)
            }

            if (isMine && participant.status == ParticipantStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Row {
                    androidx.compose.material3.Button(onClick = onSettleUp) { Text("Settle Up via UPI") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onMarkPaidManually) { Text("I've already paid") }
                }
            }

            if (isPayer && participant.isExternal && participant.status == ParticipantStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onShareLink) { Text("Share payment link") }
            }

            if (isPayer && participant.status == ParticipantStatus.CLAIMED_PAID) {
                Spacer(Modifier.height(12.dp))
                Text("${participant.name} says they paid - did you receive it?", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Button(onClick = onConfirmReceived) { Text("Yes, Confirm") }
            }
        }
    }
}

private fun statusText(participant: SmartSplitParticipant): String = when (participant.status) {
    ParticipantStatus.PENDING -> if (participant.isExternal) "Doesn't have the app - share a link" else "Owes you - not paid yet"
    ParticipantStatus.PAID_VIA_UPI -> "Paid via UPI"
    ParticipantStatus.CLAIMED_PAID -> "Says they paid - awaiting your confirmation"
    ParticipantStatus.CONFIRMED -> "Settled"
}
