package com.lifeos.expensecapture.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // weight() resolves per-receiver (RowScope/ColumnScope);
// importing it by name alone resolved to an internal symbol during the real build - see
// android-app/README.md "Known gaps" if this surfaces again after a Compose version bump.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.ui.common.TransactionRow
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Redesigned 2026-08 to match the `ui2/` reference mockups' "Transactions" screen: a search bar,
 * All/Income/Expense filter chips, and date-grouped sections (Today/Yesterday/calendar date)
 * instead of one flat list - all real filtering over the same TransactionRepository data, no
 * separate search index. The mockup's "Filter" icon (beyond direction) isn't wired to anything
 * real in this app yet, so it's left out rather than added as a dead button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(app: App, onBack: () -> Unit) {
    val repository = remember {
        TransactionRepository(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao(),
            merchantRuleDao = app.database.merchantRuleDao(),
            correctionDao = app.database.correctionDao()
        )
    }
    val viewModel = remember { LedgerViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val suggestingInProgress by viewModel.suggestingInProgress.collectAsState()
    val suggestionError by viewModel.suggestionError.collectAsState()

    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showManualEntry by remember { mutableStateOf(false) }

    // AI-assisted categorization (2026-08) - see AiCategorySuggester's kdoc: a one-time cold-start
    // assist for merchants with no rule yet, never a recurring dependency (accepting a suggestion
    // seeds a real merchant_rule, same as a manual correction would). Only shown when there's
    // something to suggest for.
    val uncategorizedCount = remember(uiState.allTransactions, uiState.categories) {
        val uncategorizedId = uiState.categories.firstOrNull { it.name == "Uncategorized" }?.id
        uiState.allTransactions.count { it.categoryId == uncategorizedId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showManualEntry = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Transaction") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DirectionFilterChip("All", uiState.directionFilter == LedgerDirectionFilter.ALL) {
                    viewModel.setDirectionFilter(LedgerDirectionFilter.ALL)
                }
                DirectionFilterChip("Income", uiState.directionFilter == LedgerDirectionFilter.INCOME) {
                    viewModel.setDirectionFilter(LedgerDirectionFilter.INCOME)
                }
                DirectionFilterChip("Expense", uiState.directionFilter == LedgerDirectionFilter.EXPENSE) {
                    viewModel.setDirectionFilter(LedgerDirectionFilter.EXPENSE)
                }
            }

            if (uncategorizedCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$uncategorizedCount uncategorized",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (suggestingInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        TextButton(onClick = { viewModel.requestAiCategorySuggestions() }) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Suggest categories with AI")
                        }
                    }
                }
            }

            if (uiState.allTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No transactions yet. They'll appear here automatically once a bank/UPI " +
                            "SMS arrives, or add one manually with the button below.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions match this search or filter.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val grouped = groupByDateLabel(uiState.transactions)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (label, transactionsForLabel) ->
                        item {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(transactionsForLabel, key = { it.id }) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                categoryName = uiState.categories.firstOrNull { it.id == transaction.categoryId }?.name
                                    ?: "Uncategorized",
                                onClick = { selectedTransaction = transaction }
                            )
                            HorizontalDivider()
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        CategorizeSheet(
            transaction = transaction,
            categories = uiState.categories,
            onCategorySelected = { categoryId ->
                viewModel.recategorize(transaction, categoryId)
                selectedTransaction = null
            },
            onDelete = {
                viewModel.deleteTransaction(transaction)
                selectedTransaction = null
            },
            onDismiss = { selectedTransaction = null }
        )
    }

    if (showManualEntry) {
        ManualEntryDialog(
            categories = uiState.categories,
            onConfirm = { amount, merchant, direction, categoryId, date ->
                viewModel.addManual(amount, merchant, direction, categoryId, date)
                showManualEntry = false
            },
            onDismiss = { showManualEntry = false }
        )
    }

    if (suggestions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuggestions() },
            title = { Text("AI category suggestions") },
            text = {
                Column {
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    suggestion.transaction.merchantRaw,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "→ ${suggestion.suggestedCategoryName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.rejectSuggestion(suggestion) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = { viewModel.acceptSuggestion(suggestion) }) {
                                Icon(Icons.Filled.Check, contentDescription = "Accept", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { suggestions.forEach { viewModel.acceptSuggestion(it) } }) { Text("Accept all") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSuggestions() }) { Text("Dismiss") }
            }
        )
    }

    suggestionError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuggestions() },
            title = { Text("AI suggestions") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSuggestions() }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun DirectionFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Today/Yesterday/calendar-date section headers (2026-08 reference mockups, `ui2/` folder) -
 * transactions arrive newest-first already (TransactionRepository.observeLedger), so grouping
 * preserves that order rather than re-sorting. */
private fun groupByDateLabel(transactions: List<TransactionEntity>): List<Pair<String, List<TransactionEntity>>> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    val groups = LinkedHashMap<String, MutableList<TransactionEntity>>()
    transactions.forEach { transaction ->
        val date = Instant.ofEpochMilli(transaction.date).atZone(zone).toLocalDate()
        val label = when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> date.format(formatter)
        }
        groups.getOrPut(label) { mutableListOf() }.add(transaction)
    }
    return groups.map { it.key to it.value }
}
