package com.lifeos.expensecapture.ui.bills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.lifeos.expensecapture.finance.FinanceInsightsRepository.BillDisplayStatus
import com.lifeos.expensecapture.finance.FinanceInsightsRepository.BillWithComputedStatus
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.StatusChip
import com.lifeos.expensecapture.ui.common.SummaryStatCard
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.AmountLarge

/**
 * Bills PRD, Phase 3 Doc 22. Detection comes from RecurringPatternDetector (higher amount
 * variance = bill-like, per the PRD's own stated distinction from fixed-amount subscriptions).
 * Reminder DELIVERY (push notifications ahead of the due date) is explicitly NOT implemented -
 * no notification infrastructure exists yet in this pilot - so due/overdue status is only
 * visible when this screen is opened, not proactively delivered. See day-2.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        BillsViewModel(
            FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            )
        )
    }
    val bills by viewModel.bills.collectAsState()
    val aiReviewInProgress by viewModel.aiReviewInProgress.collectAsState()
    val aiReviewResultMessage by viewModel.aiReviewResultMessage.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // AI-augmented bill review (2026-08) - see AiFinanceAnalyst's kdoc. Reviews
                    // real transaction history for recurring charges the automatic detector's
                    // 20-40 day window misses (annual/quarterly bills); anything found lands in
                    // the "New" section below for the same confirm/dismiss review as any other
                    // detected bill, never auto-tracked.
                    if (aiReviewInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 12.dp))
                    } else {
                        IconButton(onClick = { viewModel.requestAiReview() }) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Ask AI to review for missed bills")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add bill manually")
            }
        }
    ) { padding ->
        if (bills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No recurring bills detected yet. Variable-amount recurring payments " +
                        "(utilities, statements, rent) show up here once a pattern repeats."
                )
            }
        } else {
            val trackedBills = bills.filter {
                it.displayStatus != BillDisplayStatus.CANCELLED && it.displayStatus != BillDisplayStatus.UNCONFIRMED
            }
            val totalTypical = trackedBills.sumOf { it.bill.typicalAmount }
            // Bug fix (real user report, 2026-08): dismissing a detected bill ("Not a bill") sets
            // it CANCELLED, which upsertBill's `existing.status != CANCELLED` guard relies on to
            // never re-detect that payee again. Hard-deleting the row (via the Delete button
            // below) throws that guard away - billDao.findByPayee then finds nothing, so the next
            // refreshRecurringDetection() (runs on every Home open) re-inserts it from scratch as
            // a brand new DETECTED_UNCONFIRMED row, i.e. the "deleted" bill comes back. A
            // manually-added bill has no such risk (upsertBill only ever acts on payees the SMS-
            // based RecurringPatternDetector actually finds), so those alone still show up
            // CANCELLED with a real Delete option; a dismissed detected bill just disappears
            // immediately - no separate delete step needed, and nothing to resurrect it.
            val visibleBills = bills.filter {
                it.displayStatus != BillDisplayStatus.CANCELLED || it.bill.isManuallyAdded
            }
            val overdueCount = bills.count { it.displayStatus == BillDisplayStatus.OVERDUE }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryStatCard(
                        icon = Icons.Filled.Payments,
                        label = "Tracked bills, typical total",
                        value = "₹${"%.2f".format(totalTypical)}",
                        caption = if (overdueCount > 0) {
                            "$overdueCount past due - worth a look"
                        } else {
                            "${trackedBills.size} bill${if (trackedBills.size == 1) "" else "s"} tracked"
                        }
                    )
                }
                items(visibleBills, key = { it.bill.id }) { item ->
                    BillCard(
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
        AddBillDialog(
            onConfirm = { payee, amount, dueDay ->
                viewModel.addManual(payee, amount, dueDay)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    aiReviewResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissAiReviewMessage() },
            title = { Text("AI bill review") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAiReviewMessage() }) { Text("OK") }
            }
        )
    }
}

/** Bills PRD, Doc 22 Feature Scope: manual bill add - "for bills with no reliable digital
 * trail (cash rent, informal loans)" that the SMS-based detector can never see. */
@Composable
private fun AddBillDialog(
    onConfirm: (payee: String, typicalAmount: Double, dueDayOfMonth: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var payee by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDayText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a bill") },
        text = {
            Column {
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee (e.g. Rent, Electricity)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Typical amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it },
                    label = { Text("Usual due day of month (1-31)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull()
                val dueDay = dueDayText.toIntOrNull()
                if (payee.isNotBlank() && amount != null && dueDay != null && dueDay in 1..31) {
                    onConfirm(payee, amount, dueDay)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BillCard(
    item: BillWithComputedStatus,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val bill = item.bill
    var showDismissConfirm by remember { mutableStateOf(false) }
    val containerColor = when (item.displayStatus) {
        BillDisplayStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
        BillDisplayStatus.DUE_TODAY -> MaterialTheme.colorScheme.secondaryContainer
        else -> cardSurfaceColor()
    }

    val (chipLabel, chipColor) = when (item.displayStatus) {
        BillDisplayStatus.UNCONFIRMED -> "New" to MaterialTheme.colorScheme.tertiary
        BillDisplayStatus.UPCOMING -> "Upcoming" to MaterialTheme.colorScheme.onSurfaceVariant
        BillDisplayStatus.DUE_TODAY -> "Due today" to MaterialTheme.colorScheme.secondary
        BillDisplayStatus.OVERDUE -> "Overdue" to MaterialTheme.colorScheme.error
        BillDisplayStatus.PAID_THIS_CYCLE -> "Paid" to MaterialTheme.colorScheme.primary
        BillDisplayStatus.CANCELLED -> "Not tracked" to MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = Icons.Filled.Payments,
                    tint = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    size = 40.dp
                )
                Spacer(Modifier.width(12.dp))
                // weight(1f) + ellipsis - see SubscriptionCard's kdoc for the same fix and why
                // (found by actually running this against real device data: a long payee name
                // wrapped unpredictably and left a blank gap before the amount).
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        bill.payeeDisplay,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Usually around day ${bill.dueDayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("₹${"%.2f".format(bill.typicalAmount)}", style = AmountLarge)
            }
            Spacer(Modifier.height(12.dp))
            StatusChip(chipLabel, chipColor)
            if (item.displayStatus == BillDisplayStatus.UNCONFIRMED || item.displayStatus == BillDisplayStatus.OVERDUE) {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (item.displayStatus == BillDisplayStatus.UNCONFIRMED) {
                        "Looks like a recurring bill - is this right?"
                    } else {
                        "Past its usual due date - worth checking"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (item.displayStatus) {
                BillDisplayStatus.UNCONFIRMED -> {
                    Row {
                        TextButton(onClick = onConfirm) { Text("Yes, track this") }
                        TextButton(onClick = { showDismissConfirm = true }) { Text("Not a bill") }
                    }
                }
                // Real removal (found via a real user review - see BillDao.delete's kdoc):
                // a cancelled bill previously had no action at all, so it just sat in the
                // list forever with no way to actually clear it out.
                BillDisplayStatus.CANCELLED -> {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Row {
                        TextButton(onClick = { showDismissConfirm = true }) { Text("Stop tracking") }
                        TextButton(onClick = onDelete) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Real user request (2026-08): "Not a bill" and "Stop tracking" both immediately hide the
    // bill (see visibleBills in BillsScreen's kdoc addition above) - a stray tap had no undo, so
    // both now confirm first instead of firing on a single tap.
    if (showDismissConfirm) {
        AlertDialog(
            onDismissRequest = { showDismissConfirm = false },
            title = { Text("Are you sure?") },
            text = {
                Text(
                    if (item.displayStatus == BillDisplayStatus.UNCONFIRMED) {
                        "Mark \"${bill.payeeDisplay}\" as not a bill? It won't be tracked as a recurring bill."
                    } else {
                        "Stop tracking \"${bill.payeeDisplay}\"? It'll no longer count toward your tracked bills."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDismissConfirm = false; onDismiss() }) { Text("Yes, I'm sure") }
            },
            dismissButton = {
                TextButton(onClick = { showDismissConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
