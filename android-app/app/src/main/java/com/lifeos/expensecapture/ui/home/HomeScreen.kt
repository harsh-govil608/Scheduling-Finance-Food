package com.lifeos.expensecapture.ui.home

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
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.R
import com.lifeos.expensecapture.assistant.AiTextPolisher
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.ui.common.AccentInfoCard
import com.lifeos.expensecapture.ui.common.AiInsightCard
import com.lifeos.expensecapture.ui.common.CategoryVisuals
import com.lifeos.expensecapture.ui.common.EntryRow
import com.lifeos.expensecapture.ui.common.GreetingTitle
import com.lifeos.expensecapture.ui.common.HeroMoneyCard
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.ProfileAvatarButton
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.StatTile
import com.lifeos.expensecapture.ui.common.TransactionRow
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.common.rememberSpeaker
import com.lifeos.expensecapture.ui.ledger.ManualEntryDialog
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong
import com.lifeos.expensecapture.update.UpdateViewModel
import com.lifeos.expensecapture.util.Prefs
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import kotlinx.coroutines.launch
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
    onOpenDashboardCustomize: () -> Unit,
    onSelectPillar: (Pillar) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Quick Actions row (2026-08 reference mockups, `ui2/` folder) - reuses the same
    // ManualEntryDialog/TransactionRepository Ledger's own "+" button already opens, just
    // pre-selecting the Spent/Received chip per action, instead of duplicating that form.
    val transactionRepository = remember {
        TransactionRepository(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao(),
            merchantRuleDao = app.database.merchantRuleDao(),
            correctionDao = app.database.correctionDao()
        )
    }
    var manualEntryDirection by remember { mutableStateOf<TransactionDirection?>(null) }
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
    val goodMorningGreeting = stringResource(R.string.home_good_morning_greeting)
    val nothingNeedsAttention = stringResource(R.string.home_nothing_needs_attention)

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
                goodMorningGreeting,
                morningState.leadItem ?: nothingNeedsAttention,
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
                    // Search icon removed from here (real user request, 2026-08) - decluttering
                    // the top bar. Search itself is unaffected - still reachable from the "Search"
                    // Quick Action button below (see onOpenSearch's other call site).
                    IconButton(onClick = onOpenDashboardCustomize) {
                        Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.home_customize_dashboard))
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(badge = {
                            if (uiState.unreadNotifications > 0) Badge { Text("${uiState.unreadNotifications}") }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.home_notifications))
                        }
                    }
                    ProfileAvatarButton(photoPath = uiState.profilePhotoPath, onClick = onOpenProfile)
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
                        title = stringResource(R.string.home_update_available, update.versionName),
                        body = update.notes.takeIf { it.isNotBlank() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { updateViewModel.installUpdate() },
                                enabled = !updateState.downloading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(stringResource(if (updateState.downloading) R.string.home_downloading else R.string.home_download_install))
                            }
                            if (updateState.downloading) {
                                Spacer(Modifier.width(8.dp))
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            }
                            TextButton(
                                onClick = { updateViewModel.dismiss() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) { Text(stringResource(R.string.home_later)) }
                        }
                    }
                }
            }

            if (morningState.visible) {
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.WbSunny,
                        accentColor = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.home_good_morning_title),
                        body = morningState.leadItem ?: nothingNeedsAttention
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
                            ) { Text(stringResource(R.string.home_got_it)) }
                            TextButton(
                                onClick = {
                                    // Warmer framing (found via a real user report, 2026-07 -
                                    // "audio should come as welcoming and giving summaries"):
                                    // this used to speak the same three card sentences with no
                                    // greeting - nothing factual changes, just an opening line so
                                    // it starts like a greeting rather than launching straight
                                    // into information.
                                    val lines = listOfNotNull(
                                        goodMorningGreeting,
                                        morningState.leadItem ?: nothingNeedsAttention,
                                        morningState.homeLine,
                                        morningState.yesterdaySpendLine
                                    )
                                    speak(lines.joinToString(". "))
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.home_read_aloud))
                            }
                        }
                    }
                }
            }

            if (!uiState.isOnline) {
                item {
                    Text(
                        stringResource(R.string.home_offline_banner),
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
                        title = stringResource(R.string.home_sms_off_title),
                        body = stringResource(R.string.home_sms_off_body)
                    ) {
                        TextButton(
                            onClick = onOpenPermissionsReview,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text(stringResource(R.string.home_review_permissions)) }
                    }
                }
            }

            // Customizable Dashboard (2026-08, real user request) - the 6 genuinely optional
            // sections render in whatever order/visibility Prefs.getHomeSectionOrder returns
            // (defaults to this exact original order/visibility). "Needs attention" travels
            // together with Stats rather than being its own togglable entry - it's a system-
            // arbitrated slot (see HomeViewModel's kdoc on precedence), not user content, so it
            // stays wherever Stats ends up instead of being independently reorderable.
            Prefs.getHomeSectionOrder(context).forEach { section ->
                when (section) {
                    HomeSection.HERO -> item { HeroSection(uiState) }
                    HomeSection.STATS -> {
                        if (uiState.hasAnyData) item { StatsSection(uiState) }
                        uiState.attentionItem?.let { attention ->
                            item {
                                AccentInfoCard(
                                    icon = Icons.Filled.PriorityHigh,
                                    accentColor = Warning,
                                    title = stringResource(R.string.home_needs_attention),
                                    body = attentionItemText(attention)
                                )
                            }
                        }
                    }
                    HomeSection.INSIGHT -> uiState.spendingInsight?.let { insight ->
                        item { InsightSection(insight) }
                    }
                    HomeSection.QUICK_ACTIONS -> item {
                        QuickActionsSection(
                            onAddExpense = { manualEntryDirection = TransactionDirection.DEBIT },
                            onAddIncome = { manualEntryDirection = TransactionDirection.CREDIT },
                            onOpenSplitExpenses = onOpenSplitExpenses,
                            onOpenSearch = onOpenSearch,
                            onOpenImportStatement = onOpenImportStatement,
                            onOpenPayCycle = onOpenPayCycle,
                            onOpenNightSummary = onOpenNightSummary,
                            onOpenInvestments = onOpenInvestments
                        )
                    }
                    HomeSection.RECENT -> if (uiState.recentTransactions.isNotEmpty()) {
                        item { RecentTransactionsSection(uiState, onOpenLedger) }
                    }
                    HomeSection.EXPLORE -> item {
                        ExploreSection(onOpenLedger, onOpenBudgets, onOpenSubscriptions, onOpenBills, onOpenNeedsReview)
                    }
                }
            }
        }
    }

    manualEntryDirection?.let { direction ->
        ManualEntryDialog(
            categories = uiState.categories,
            initialDirection = direction,
            onConfirm = { amount, merchant, txnDirection, categoryId, date ->
                coroutineScope.launch {
                    transactionRepository.addManualTransaction(
                        amount = amount,
                        direction = txnDirection,
                        merchant = merchant,
                        categoryId = categoryId,
                        date = date
                    )
                }
                manualEntryDirection = null
            },
            onDismiss = { manualEntryDirection = null }
        )
    }
}

@Composable
private fun HeroSection(uiState: HomeUiState) {
    HeroMoneyCard(
        label = stringResource(R.string.home_spent_this_month),
        amount = uiState.spentThisMonth,
        caption = if (!uiState.hasAnyData) {
            stringResource(R.string.home_nothing_captured_yet)
        } else {
            stringResource(R.string.home_last_7_days)
        },
        trend = uiState.last7DaysSpend,
        secondaryLabel = if (uiState.hasAnyData) stringResource(R.string.home_today_label) else null,
        secondaryAmount = if (uiState.hasAnyData) uiState.spentToday else null,
        trendThreshold = uiState.dailySpendThreshold
    )
}

// Stat-tile grid (reference mockups' Income/Expenses/Savings/Investments 2x2 grid, see `ui/`
// folder) - real numbers from HomeViewModel, not placeholders. Caller gates this on
// uiState.hasAnyData, same gate HeroMoneyCard's caption already uses, so a fresh install doesn't
// show four ₹0.00 tiles before there's anything to show at all.
@Composable
private fun StatsSection(uiState: HomeUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                icon = Icons.Filled.TrendingUp,
                iconTint = MaterialTheme.colorScheme.primary,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                label = stringResource(R.string.home_stat_income),
                value = "₹${"%.2f".format(uiState.incomeThisMonth)}",
                deltaText = uiState.incomeDeltaPercent?.let { "${if (it >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(it))}%" },
                deltaPositive = (uiState.incomeDeltaPercent ?: 0f) >= 0f,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Filled.TrendingDown,
                iconTint = MaterialTheme.colorScheme.error,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                label = stringResource(R.string.home_stat_expenses),
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
                label = stringResource(R.string.home_stat_savings),
                value = "₹${"%.2f".format(uiState.savingsThisMonth)}",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Filled.AccountBalanceWallet,
                iconTint = MaterialTheme.colorScheme.secondary,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                label = stringResource(R.string.home_stat_investments),
                value = "₹${"%.2f".format(uiState.investmentsTotal)}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InsightSection(insight: com.lifeos.expensecapture.finance.SpendingInsightEngine.SpendingInsight) {
    // AI-polished phrasing (2026-08, real user request) - the deterministic sentence is still the
    // only source of truth (spendingInsightText), this just asks AiClient to warm up the
    // phrasing. Shows the plain sentence immediately, swapped in-place if/when the polished
    // version arrives; falls back to the plain sentence unchanged on any failure - see
    // AiTextPolisher's kdoc.
    val factual = remember(insight) { spendingInsightText(insight) }
    val polished by produceState(initialValue = factual, factual) {
        value = AiTextPolisher.polish(factual)
    }
    AiInsightCard(title = stringResource(R.string.home_what_changed), body = polished)
}

@Composable
private fun QuickActionsSection(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onOpenSplitExpenses: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenImportStatement: () -> Unit,
    onOpenPayCycle: () -> Unit,
    onOpenNightSummary: () -> Unit,
    onOpenInvestments: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(stringResource(R.string.home_quick_actions))
        // 2x4 grid, not a scroll row (real user feedback, 2026-08: a horizontally scrolling row
        // hid the extra four actions instead of making them easier to reach) - all eight visible
        // at once, two rows of four.
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionButton(
                    icon = Icons.Filled.Add,
                    label = stringResource(R.string.home_qa_add_expense),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onAddExpense
                )
                QuickActionButton(
                    icon = Icons.Filled.ArrowDownward,
                    label = stringResource(R.string.home_qa_add_income),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onAddIncome
                )
                QuickActionButton(
                    icon = Icons.Filled.Groups,
                    label = stringResource(R.string.home_qa_split_expenses),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onClick = onOpenSplitExpenses
                )
                QuickActionButton(
                    icon = Icons.Filled.Search,
                    label = stringResource(R.string.home_qa_search),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenSearch
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionButton(
                    icon = Icons.Filled.UploadFile,
                    label = stringResource(R.string.home_qa_import_statement),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onOpenImportStatement
                )
                QuickActionButton(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = stringResource(R.string.home_qa_pay_cycle),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onClick = onOpenPayCycle
                )
                QuickActionButton(
                    icon = Icons.Filled.Today,
                    label = stringResource(R.string.home_qa_your_day),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    onClick = onOpenNightSummary
                )
                QuickActionButton(
                    icon = Icons.Filled.TrendingUp,
                    label = stringResource(R.string.home_stat_investments),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenInvestments
                )
            }
        }
    }
}

// Recent Transactions preview (reference mockups' "Recent Active Flow"/"Recent Transactions") -
// the newest few real rows, same TransactionRow Ledger uses (moved to ui/common so both share one
// implementation). "View All" opens the full Ledger rather than duplicating its filtering/search
// here. Caller gates this on uiState.recentTransactions being non-empty.
@Composable
private fun RecentTransactionsSection(uiState: HomeUiState, onOpenLedger: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringResource(R.string.home_recent_transactions))
            TextButton(onClick = onOpenLedger) { Text(stringResource(R.string.home_view_all)) }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
        ) {
            Column {
                val uncategorizedLabel = stringResource(R.string.home_uncategorized)
                uiState.recentTransactions.forEachIndexed { index, transaction ->
                    TransactionRow(
                        transaction = transaction,
                        categoryName = uiState.categories.firstOrNull { it.id == transaction.categoryId }?.name
                            ?: uncategorizedLabel,
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

@Composable
private fun ExploreSection(
    onOpenLedger: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenNeedsReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(stringResource(R.string.home_explore))
        EntryRow(
            Icons.Filled.ReceiptLong, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
            stringResource(R.string.home_entry_ledger_title), stringResource(R.string.home_entry_ledger_body), onOpenLedger
        )
        EntryRow(
            Icons.Filled.PieChart, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
            stringResource(R.string.home_entry_budgets_title), stringResource(R.string.home_entry_budgets_body), onOpenBudgets
        )
        EntryRow(
            Icons.Filled.Autorenew, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
            stringResource(R.string.home_entry_subscriptions_title), stringResource(R.string.home_entry_subscriptions_body), onOpenSubscriptions
        )
        EntryRow(
            Icons.Filled.Payments, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer,
            stringResource(R.string.home_entry_bills_title), stringResource(R.string.home_entry_bills_body), onOpenBills
        )
        EntryRow(
            Icons.Filled.Inbox, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceVariant,
            stringResource(R.string.home_entry_needs_review_title), stringResource(R.string.home_entry_needs_review_body), onOpenNeedsReview
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(icon = icon, tint = iconTint, containerColor = containerColor)
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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
