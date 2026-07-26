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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.lifeos.expensecapture.ui.common.AccentInfoCard
import com.lifeos.expensecapture.ui.common.EntryRow
import com.lifeos.expensecapture.ui.common.HeroMoneyCard
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong
import com.lifeos.expensecapture.update.UpdateViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size

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
    onOpenPermissionsReview: () -> Unit,
    onSelectPillar: (Pillar) -> Unit
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
            ),
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao()
        )
    }
    val morningState by morningViewModel.uiState.collectAsState()

    val updateViewModel = remember { UpdateViewModel(context) }
    val updateState by updateViewModel.uiState.collectAsState()

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
        },
        bottomBar = { PillarBottomBar(current = Pillar.FINANCE, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            updateState.available?.let { update ->
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.Download,
                        accentColor = MaterialTheme.colorScheme.primary,
                        title = "Update available (v${update.versionName})",
                        body = update.notes.takeIf { it.isNotBlank() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { updateViewModel.installUpdate() },
                                enabled = !updateState.downloading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (updateState.downloading) "Downloading..." else "Download & install")
                            }
                            if (updateState.downloading) {
                                Spacer(Modifier.width(8.dp))
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            }
                            TextButton(
                                onClick = { updateViewModel.dismiss() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) { Text("Later") }
                        }
                    }
                }
            }

            if (morningState.visible) {
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.WbSunny,
                        accentColor = MaterialTheme.colorScheme.primary,
                        title = "Good morning",
                        body = morningState.leadItem ?: "Nothing needs your attention this morning."
                    ) {
                        morningState.homeLine?.let { line ->
                            Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                        }
                        Text(
                            morningState.yesterdaySpendLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { morningViewModel.dismiss() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("Got it") }
                    }
                }
            }

            if (!uiState.isOnline) {
                item {
                    Text(
                        "Offline - everything here still works fully; nothing in this app needs a connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
            }

            if (uiState.smsPermissionRevoked) {
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.Warning,
                        accentColor = WarningStrong,
                        title = "SMS access was turned off",
                        body = "Automatic capture is paused. You can still add expenses manually, or re-enable it from Permissions."
                    ) {
                        TextButton(
                            onClick = onOpenPermissionsReview,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("Review permissions") }
                    }
                }
            }

            item {
                HeroMoneyCard(
                    label = "Spent this month",
                    amount = uiState.spentThisMonth,
                    caption = if (!uiState.hasAnyData) {
                        "Nothing captured yet - grant SMS access or add a transaction manually to get started."
                    } else {
                        "Last 7 days"
                    },
                    trend = uiState.last7DaysSpend
                )
            }

            uiState.attentionItem?.let { attention ->
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.PriorityHigh,
                        accentColor = Warning,
                        title = "Needs attention",
                        body = attentionItemText(attention)
                    )
                }
            }

            item { SectionLabel("Explore") }
            item {
                EntryRow(
                    Icons.Filled.ReceiptLong, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                    "Ledger", "All captured and manual transactions", onOpenLedger
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Today, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Your day", "This morning through tonight, and what's coming up", onOpenNightSummary
                )
            }
            item {
                EntryRow(
                    Icons.Filled.PieChart, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                    "Budgets", "Set limits and see where you stand, with a month-end projection", onOpenBudgets
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Autorenew, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                    "Subscriptions", "Recurring charges detected from your transaction history", onOpenSubscriptions
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Payments, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                    "Bills", "Variable-amount recurring payments and due dates", onOpenBills
                )
            }
            item {
                EntryRow(
                    Icons.Filled.TrendingUp, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Investments", "Manually tracked holdings, read-only", onOpenInvestments
                )
            }
            item {
                EntryRow(
                    Icons.Filled.Inbox, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant,
                    "Needs Review", "Messages the parser couldn't confidently read", onOpenNeedsReview
                )
            }
        }
    }
}

private fun attentionItemText(item: AttentionItem): String = when (item) {
    is AttentionItem.OverdueBill -> "${item.payee} (~₹${"%.2f".format(item.amount)}) looks overdue"
    is AttentionItem.OverBudget -> "${item.categoryName} is ₹${"%.2f".format(item.overspendAmount)} over budget this month"
    is AttentionItem.UnparsedMessages -> "${item.count} message${if (item.count == 1) "" else "s"} couldn't be read automatically"
}
