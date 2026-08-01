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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.export.CsvExporter
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.ui.common.AccentInfoCard
import com.lifeos.expensecapture.ui.common.AiInsightCard
import com.lifeos.expensecapture.ui.common.CategoryVisuals
import com.lifeos.expensecapture.ui.common.EntryRow
import com.lifeos.expensecapture.ui.common.GreetingTitle
import com.lifeos.expensecapture.ui.common.HeroMoneyCard
import com.lifeos.expensecapture.ui.common.ProfileAvatarButton
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.StatTile
import com.lifeos.expensecapture.ui.common.TransactionRow
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.common.rememberSpeaker
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong
import com.lifeos.expensecapture.update.UpdateViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
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
    onOpenSplitExpenses: () -> Unit,
    onOpenImportStatement: () -> Unit,
    onOpenPayCycle: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenInvestments: () -> Unit,
    onOpenNightSummary: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPermissionsReview: () -> Unit,
    onOpenAssistant: () -> Unit,
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
            categoryDao = app.database.categoryDao(),
            goalDao = app.database.goalDao(),
            investmentDao = app.database.investmentDao(),
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
    val speak = rememberSpeaker()

    // Proactive audio welcome (found via a real user request, 2026-07 - "at the very beginning
    // audio should come to welcome the guest and summarize as proactive step"): reuses the
    // Morning Briefing's own "first open of the day" gate (visible only flips true once per
    // calendar day, see MorningBriefingViewModel's kdoc) rather than a new mechanism. Keyed on
    // morningState.visible so this LaunchedEffect body only re-runs when that value actually
    // changes (Compose won't rerun on every recomposition while it stays true) - the
    // alreadySpokenToday()/markSpokenToday() pair on top of that guards the case a fresh app
    // launch recreates this composable while the same calendar day's card is still unspoken.
    LaunchedEffect(morningState.visible) {
        if (morningState.visible && !morningViewModel.alreadySpokenToday()) {
            val lines = listOfNotNull(
                "Good morning!",
                morningState.leadItem ?: "Nothing needs your attention this morning.",
                morningState.homeLine,
                morningState.yesterdaySpendLine
            )
            speak(lines.joinToString(". "))
            morningViewModel.markSpokenToday()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { GreetingTitle(uiState.displayName) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    // Export range choice (found via a real user report, 2026-07 - "csv file not
                    // arranged, options needed"): this used to export every transaction ever
                    // captured with no way to scope it. Quick presets stay for the common cases,
                    // plus a genuine custom range (a second real user report: "give user
                    // flexibility to decide time range" - a fixed two-option choice still wasn't
                    // enough) via two sequential date pickers, same DatePickerDialog pattern
                    // already proven in ManualEntryDialog.
                    var showExportMenu by remember { mutableStateOf(false) }
                    var showExportStartPicker by remember { mutableStateOf(false) }
                    var showExportEndPicker by remember { mutableStateOf(false) }
                    var exportRangeStartMillis by remember { mutableStateOf<Long?>(null) }

                    suspend fun exportAndShare(transactions: List<TransactionEntity>) {
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

                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Export your data")
                    }
                    DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Export this month (CSV)") },
                            onClick = {
                                showExportMenu = false
                                coroutineScope.launch {
                                    val zone = ZoneId.systemDefault()
                                    val monthStart = LocalDate.now(zone).withDayOfMonth(1)
                                        .atStartOfDay(zone).toInstant().toEpochMilli()
                                    val transactions = app.database.transactionDao().observeAll().first()
                                        .filter { it.date >= monthStart }
                                    exportAndShare(transactions)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export all time (CSV)") },
                            onClick = {
                                showExportMenu = false
                                coroutineScope.launch {
                                    val transactions = app.database.transactionDao().observeAll().first()
                                    exportAndShare(transactions)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export a custom range (CSV)…") },
                            onClick = {
                                showExportMenu = false
                                showExportStartPicker = true
                            }
                        )
                    }

                    if (showExportStartPicker) {
                        val startPickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showExportStartPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    exportRangeStartMillis = startPickerState.selectedDateMillis
                                    showExportStartPicker = false
                                    showExportEndPicker = true
                                }) { Text("Next: end date") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExportStartPicker = false }) { Text("Cancel") }
                            }
                        ) { DatePicker(state = startPickerState) }
                    }

                    if (showExportEndPicker) {
                        val endPickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showExportEndPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val startMillis = exportRangeStartMillis
                                    val endMillis = endPickerState.selectedDateMillis
                                    showExportEndPicker = false
                                    if (startMillis != null && endMillis != null) {
                                        // DatePickerState.selectedDateMillis is UTC midnight for
                                        // the picked calendar date - recover that date, then
                                        // build the actual filter range in the device's own zone
                                        // so "end date" means through the end of that real day.
                                        val zone = ZoneId.systemDefault()
                                        val startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneOffset.UTC).toLocalDate()
                                        val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneOffset.UTC).toLocalDate()
                                        val rangeStart = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
                                        val rangeEndExclusive = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                                        coroutineScope.launch {
                                            val transactions = app.database.transactionDao().observeAll().first()
                                                .filter { it.date in rangeStart until rangeEndExclusive }
                                            exportAndShare(transactions)
                                        }
                                    }
                                }) { Text("Export") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExportEndPicker = false }) { Text("Cancel") }
                            }
                        ) { DatePicker(state = endPickerState) }
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifications > 0) Badge { Text("${uiState.unreadNotifications}") }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    ProfileAvatarButton(photoPath = uiState.profilePhotoPath, onClick = onOpenProfile)
                }
            )
        },
        bottomBar = { PillarBottomBar(current = Pillar.FINANCE, onSelect = onSelectPillar) },
        // Assistant entry point (built via a real user request, 2026-07): a FAB, not an
        // EntryRow buried in the Explore list - the whole point is fewer manual taps, so it
        // needs to be the most reachable thing on the screen, not the least.
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAssistant) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Open assistant")
            }
        }
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { morningViewModel.dismiss() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) { Text("Got it") }
                            TextButton(
                                onClick = {
                                    // Warmer framing (found via a real user report, 2026-07 -
                                    // "audio should come as welcoming and giving summaries"):
                                    // this used to speak the same three card sentences with no
                                    // greeting - nothing factual changes, just an opening line so
                                    // it starts like a greeting rather than launching straight
                                    // into information.
                                    val lines = listOfNotNull(
                                        "Good morning!",
                                        morningState.leadItem ?: "Nothing needs your attention this morning.",
                                        morningState.homeLine,
                                        morningState.yesterdaySpendLine
                                    )
                                    speak(lines.joinToString(". "))
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Read aloud")
                            }
                        }
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
                    trend = uiState.last7DaysSpend,
                    secondaryLabel = if (uiState.hasAnyData) "Today" else null,
                    secondaryAmount = if (uiState.hasAnyData) uiState.spentToday else null,
                    trendThreshold = uiState.dailySpendThreshold
                )
            }

            // Stat-tile grid (reference mockups' Income/Expenses/Savings/Investments 2x2 grid,
            // see `ui/` folder) - real numbers from HomeViewModel, not placeholders. Only shown
            // once there's at least one transaction, same gate HeroMoneyCard's caption already
            // uses, so a fresh install doesn't show four ₹0.00 tiles before there's anything to
            // show at all.
            if (uiState.hasAnyData) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatTile(
                                icon = Icons.Filled.TrendingUp,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                label = "Income",
                                value = "₹${"%.2f".format(uiState.incomeThisMonth)}",
                                deltaText = uiState.incomeDeltaPercent?.let { "${if (it >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(it))}%" },
                                deltaPositive = (uiState.incomeDeltaPercent ?: 0f) >= 0f,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                icon = Icons.Filled.TrendingDown,
                                iconTint = MaterialTheme.colorScheme.error,
                                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                                label = "Expenses",
                                value = "₹${"%.2f".format(uiState.spentThisMonth)}",
                                // Rising spend is the "bad" direction here, opposite of income's polarity.
                                deltaText = uiState.expensesDeltaPercent?.let { "${if (it >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(it))}%" },
                                deltaPositive = (uiState.expensesDeltaPercent ?: 0f) < 0f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatTile(
                                icon = Icons.Filled.Savings,
                                iconTint = MaterialTheme.colorScheme.tertiary,
                                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                label = "Savings",
                                value = "₹${"%.2f".format(uiState.savingsThisMonth)}",
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                icon = Icons.Filled.AccountBalanceWallet,
                                iconTint = MaterialTheme.colorScheme.secondary,
                                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                label = "Investments",
                                value = "₹${"%.2f".format(uiState.investmentsTotal)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            uiState.spendingInsight?.let { insight ->
                item {
                    AiInsightCard(
                        title = "What changed",
                        body = spendingInsightText(insight)
                    )
                }
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

            // Recent Transactions preview (reference mockups' "Recent Active Flow"/"Recent
            // Transactions") - the newest few real rows, same TransactionRow Ledger uses (moved
            // to ui/common so both share one implementation). "View All" opens the full Ledger
            // rather than duplicating its filtering/search here.
            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel("Recent Transactions")
                        TextButton(onClick = onOpenLedger) { Text("View All") }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column {
                            uiState.recentTransactions.forEachIndexed { index, transaction ->
                                TransactionRow(
                                    transaction = transaction,
                                    categoryName = uiState.categories.firstOrNull { it.id == transaction.categoryId }?.name
                                        ?: "Uncategorized",
                                    onClick = onOpenLedger
                                )
                                if (index != uiState.recentTransactions.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
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
            item {
                EntryRow(
                    Icons.Filled.Groups, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
                    "Split Expenses", "Log what you paid for a group, track who's paid you back", onOpenSplitExpenses
                )
            }
            item {
                EntryRow(
                    Icons.Filled.UploadFile, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                    "Import Statement", "Add past transactions from a bank or card CSV export", onOpenImportStatement
                )
            }
            item {
                EntryRow(
                    Icons.Filled.AccountBalanceWallet, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer,
                    "Pay Cycle", "Income and spending between salary credits, not calendar months", onOpenPayCycle
                )
            }
        }
    }
}

private fun spendingInsightText(insight: com.lifeos.expensecapture.finance.SpendingInsightEngine.SpendingInsight): String {
    val driverText = if (insight.topMerchants.isNotEmpty()) {
        " mostly ${insight.topMerchants.joinToString(" and ")}"
    } else {
        ""
    }
    val base = "Spending on ${insight.categoryName} is up ${insight.increasePercent.toInt()}% this month" +
        " (~₹${"%.0f".format(insight.increaseAmount)} more) -$driverText." +
        " Matching last month's pace here would free up ~₹${"%.0f".format(insight.monthlySavingsIfMatchLastMonth)} this month."

    val goalLine = insight.goalAcceleration?.let { acceleration ->
        val monthsText = if (acceleration.monthsSooner >= 1.0) {
            "${acceleration.monthsSooner.toInt()} month${if (acceleration.monthsSooner.toInt() == 1) "" else "s"}"
        } else {
            "a few weeks"
        }
        " At your current saving pace, that alone could get you to \"${acceleration.goalTitle}\" roughly $monthsText sooner."
    } ?: ""

    return base + goalLine
}

private fun attentionItemText(item: AttentionItem): String = when (item) {
    is AttentionItem.OverdueBill -> "${item.payee} (~₹${"%.2f".format(item.amount)}) looks overdue"
    is AttentionItem.CashFlowRisk ->
        "₹${"%.2f".format(item.upcomingTotal)} in bills and subscriptions due in the next ${item.windowDays} days - " +
            "current budget pace leaves ₹${"%.2f".format(item.availableHeadroom)}"
    is AttentionItem.OverBudget -> "${item.categoryName} is ₹${"%.2f".format(item.overspendAmount)} over budget this month"
    is AttentionItem.UnparsedMessages -> "${item.count} message${if (item.count == 1) "" else "s"} couldn't be read automatically"
}
