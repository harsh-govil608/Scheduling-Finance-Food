package com.lifeos.expensecapture.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.export.CsvExporter
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.ui.theme.AmountLarge
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Finance Tracker (Home) PRD, Phase 3 Doc 17: the Finance pillar's landing surface. Composes a
 * net-position snapshot, the single "needs attention" slot, an offline indicator (Doc 46), a
 * permission-revocation banner (Doc 41), and entry points into every other Finance Suite
 * screen - explicitly NOT transaction-level detail, budget mechanics, or bill/subscription
 * detail, all owned by their own sibling screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    app: App,
    onOpenLedger: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenNeedsReview: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenInvestments: () -> Unit,
    onOpenNightSummary: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPermissionsReview: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember {
        HomeViewModel(
            context = context,
            transactionDao = app.database.transactionDao(),
            unparsedMessageDao = app.database.unparsedMessageDao(),
            notificationDao = app.database.notificationDao(),
            consentDao = app.database.consentDao(),
            insightsRepository = FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            )
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    val morningViewModel = remember {
        MorningBriefingViewModel(
            context = context,
            transactionDao = app.database.transactionDao(),
            insightsRepository = FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            )
        )
    }
    val morningState by morningViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance") },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val transactions = app.database.transactionDao().observeAll().first()
                            val categories = app.database.categoryDao().observeAll().first()
                            val uri = CsvExporter.exportTransactions(context, transactions) { categoryId ->
                                categories.firstOrNull { it.id == categoryId }?.name ?: "Uncategorized"
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Export transactions"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export your data")
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifications > 0) Badge { Text("${uiState.unreadNotifications}") }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile & settings")
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
            if (morningState.visible) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Good morning",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                morningState.leadItem ?: "Nothing needs your attention this morning.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                morningState.yesterdaySpendLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            TextButton(onClick = { morningViewModel.dismiss() }) { Text("Got it") }
                        }
                    }
                }
            }

            if (!uiState.isOnline) {
                item {
                    Text(
                        "Offline - everything here still works fully; nothing in this app needs a connection.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }
            }

            if (uiState.smsPermissionRevoked) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("SMS access was turned off", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Automatic capture is paused. You can still add expenses manually, " +
                                    "or re-enable it from Permissions.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = onOpenPermissionsReview) { Text("Review permissions") }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Spent this month", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "₹${"%.2f".format(uiState.spentThisMonth)}",
                            style = AmountLarge
                        )
                        if (!uiState.hasAnyData) {
                            Text(
                                "Nothing captured yet - grant SMS access or add a transaction " +
                                    "manually to get started.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            uiState.attentionItem?.let { item ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Needs attention", style = MaterialTheme.typography.bodySmall)
                            Text(attentionItemText(item), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { EntryPointCard("Ledger", "All captured and manual transactions", onOpenLedger) }
            item { EntryPointCard("Your day", "This morning through tonight, and what's coming up", onOpenNightSummary) }
            item { EntryPointCard("Budgets", "Set limits and see where you stand, with a month-end projection", onOpenBudgets) }
            item { EntryPointCard("Subscriptions", "Recurring charges detected from your transaction history", onOpenSubscriptions) }
            item { EntryPointCard("Bills", "Variable-amount recurring payments and due dates", onOpenBills) }
            item { EntryPointCard("Investments", "Manually tracked holdings, read-only", onOpenInvestments) }
            item { EntryPointCard("Needs Review", "Messages the parser couldn't confidently read", onOpenNeedsReview) }
        }
    }
}

private fun attentionItemText(item: AttentionItem): String = when (item) {
    is AttentionItem.OverdueBill -> "${item.payee} (~₹${"%.2f".format(item.amount)}) looks overdue"
    is AttentionItem.OverBudget -> "${item.categoryName} is ₹${"%.2f".format(item.overspendAmount)} over budget this month"
    is AttentionItem.UnparsedMessages -> "${item.count} message${if (item.count == 1) "" else "s"} couldn't be read automatically"
}

@Composable
private fun EntryPointCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
