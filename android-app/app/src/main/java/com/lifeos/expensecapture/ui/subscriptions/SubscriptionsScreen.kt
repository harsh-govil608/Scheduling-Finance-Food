package com.lifeos.expensecapture.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.finance.FinanceInsightsRepository.SubscriptionDisplayStatus
import com.lifeos.expensecapture.finance.FinanceInsightsRepository.SubscriptionWithComputedStatus
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.StatusChip
import com.lifeos.expensecapture.ui.common.SummaryStatCard
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.AmountLarge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Subscription Manager PRD, Phase 3 Doc 19. Detection comes from RecurringPatternDetector
 * (low amount variance = subscription-like); this screen is purely the confirm/track/dismiss
 * UX on top of that. Cancellation nudges are intentionally non-judgmental in copy here per
 * the PRD's explicit "gentle nudge, not a guilt trip" requirement and the Guiding Principles'
 * anti-shaming rule - "possibly lapsed" rather than "you're wasting money on this."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        SubscriptionsViewModel(
            FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            )
        )
    }
    val subscriptions by viewModel.subscriptions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add subscription manually")
            }
        }
    ) { padding ->
        if (subscriptions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No recurring subscriptions detected yet. This builds up automatically as " +
                        "matching charges repeat over time."
                )
            }
        } else {
            // Monthly-equivalent total (annual/quarterly subscriptions normalized to a per-month
            // figure via cadenceDays) across everything actually being tracked - excludes
            // UNCONFIRMED (not yet confirmed as real) and CANCELLED (no longer active).
            val activeSubs = subscriptions.filter {
                it.displayStatus == SubscriptionDisplayStatus.TRACKED ||
                    it.displayStatus == SubscriptionDisplayStatus.RENEWAL_UPCOMING ||
                    it.displayStatus == SubscriptionDisplayStatus.POSSIBLY_LAPSED
            }
            val monthlyTotal = activeSubs.sumOf { it.subscription.amount * (30.0 / it.subscription.cadenceDays) }
            // Bug fix (mirrors Bills' visibleBills - see BillsScreen.kt's kdoc for the same
            // reasoning): a detected (non-manual) cancelled subscription is hidden entirely once
            // cancelled - dismissSubscription's CANCELLED status is what stops it being
            // re-detected, so hard-deleting it would just let it come back on the next
            // refreshRecurringDetection. A manually-added subscription has no such risk, so it
            // stays visible CANCELLED with a real Delete option.
            val visibleSubscriptions = subscriptions.filter {
                it.displayStatus != SubscriptionDisplayStatus.CANCELLED || it.subscription.isManuallyAdded
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryStatCard(
                        icon = Icons.Filled.Autorenew,
                        label = "Recurring spend, ~per month",
                        value = "₹${"%.2f".format(monthlyTotal)}",
                        caption = "${activeSubs.size} active subscription${if (activeSubs.size == 1) "" else "s"}"
                    )
                }
                items(visibleSubscriptions, key = { it.subscription.id }) { item ->
                    SubscriptionCard(
                        item = item,
                        onConfirm = { viewModel.confirm(item) },
                        onDismiss = { viewModel.dismiss(item) },
                        onDelete = { viewModel.delete(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onConfirm = { merchant, amount, cadenceDays ->
                viewModel.addManual(merchant, amount, cadenceDays)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

/** Subscription Manager PRD, Doc 19: manual-add flow for a subscription not yet auto-detected
 * (e.g. only one charge so far, or annually billed so it looks infrequent to the detector). */
@Composable
private fun AddSubscriptionDialog(
    onConfirm: (merchant: String, amount: Double, cadenceDays: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var cadenceText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a subscription") },
        text = {
            Column {
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Name (e.g. Netflix)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cadenceText,
                    onValueChange = { cadenceText = it },
                    label = { Text("Renews every N days (30 = monthly, 365 = yearly)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull()
                val cadence = cadenceText.toIntOrNull()
                if (merchant.isNotBlank() && amount != null && cadence != null && cadence > 0) {
                    onConfirm(merchant, amount, cadence)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SubscriptionCard(
    item: SubscriptionWithComputedStatus,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val sub = item.subscription
    val (chipLabel, chipColor) = when (item.displayStatus) {
        SubscriptionDisplayStatus.UNCONFIRMED -> "New" to MaterialTheme.colorScheme.tertiary
        SubscriptionDisplayStatus.TRACKED -> "Active" to MaterialTheme.colorScheme.primary
        SubscriptionDisplayStatus.RENEWAL_UPCOMING -> "Renewing soon" to MaterialTheme.colorScheme.secondary
        SubscriptionDisplayStatus.POSSIBLY_LAPSED -> "Possibly lapsed" to MaterialTheme.colorScheme.tertiary
        SubscriptionDisplayStatus.CANCELLED -> "Not tracked" to MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = Icons.Filled.Autorenew,
                    tint = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    size = 40.dp
                )
                Spacer(Modifier.width(12.dp))
                // weight(1f) + a single-line ellipsis (found by actually running the app: real
                // SMS-derived merchant strings can be long/messy - an earlier version without
                // this wrapped unpredictably and left a large blank gap before the amount, the
                // same class of bug HeroMoneyCard's kdoc above describes for unconstrained Row
                // content) - this keeps the amount on the same line, always visible.
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sub.merchantDisplay,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Every ${sub.cadenceDays} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("₹${"%.2f".format(sub.amount)}", style = AmountLarge)
            }
            Spacer(Modifier.height(12.dp))
            StatusChip(chipLabel, chipColor)
            Spacer(Modifier.height(6.dp))
            Text(
                statusText(item, dateFormat),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.priceDrift?.let { drift ->
                Text(
                    "The last charge was ₹${"%.2f".format(drift.latestAmount)} - it's usually " +
                        "around ₹${"%.2f".format(drift.priorAverageAmount)}, worth a look",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            when (item.displayStatus) {
                SubscriptionDisplayStatus.UNCONFIRMED -> {
                    Row {
                        TextButton(onClick = onConfirm) { Text("Yes, track this") }
                        TextButton(onClick = onDismiss) { Text("Not a subscription") }
                    }
                }
                // Real removal (mirrors BillCard - see BillDao.delete's kdoc): a cancelled
                // subscription previously had no action at all, so it just sat in the list
                // forever with no way to actually clear it out.
                SubscriptionDisplayStatus.CANCELLED -> {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Row {
                        TextButton(onClick = onDismiss) { Text("Stop tracking") }
                        TextButton(onClick = onDelete) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private fun statusText(item: SubscriptionWithComputedStatus, dateFormat: SimpleDateFormat): String {
    return when (item.displayStatus) {
        SubscriptionDisplayStatus.UNCONFIRMED -> "Looks like a subscription - is this right?"
        SubscriptionDisplayStatus.TRACKED -> "Next expected around ${dateFormat.format(Date(item.nextExpectedDate))}"
        SubscriptionDisplayStatus.RENEWAL_UPCOMING -> "Renewal expected around ${dateFormat.format(Date(item.nextExpectedDate))}"
        SubscriptionDisplayStatus.POSSIBLY_LAPSED -> "Hasn't renewed when expected - might be worth a look, no rush"
        SubscriptionDisplayStatus.CANCELLED -> "No longer tracked"
    }
}
