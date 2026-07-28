package com.lifeos.expensecapture.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // weight() resolves per-receiver (RowScope/ColumnScope);
// importing it by name alone resolved to an internal symbol during the real build - see
// android-app/README.md "Known gaps" if this surfaces again after a Compose version bump.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.ui.common.CategoryVisuals
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.theme.AmountBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showManualEntry by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showManualEntry = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction manually")
            }
        }
    ) { padding ->
        if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No transactions yet. They'll appear here automatically once a bank/UPI " +
                        "SMS arrives, or add one manually with the + button.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        categoryName = uiState.categories.firstOrNull { it.id == transaction.categoryId }?.name
                            ?: "Uncategorized",
                        onClick = { selectedTransaction = transaction }
                    )
                    HorizontalDivider()
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
}

/**
 * Category icon badge added (found via a real user report, 2026-07 - "the UI is looking too
 * basic"): this row used to be plain text with no visual accent at all, unlike Home's polished
 * treatment - see CategoryVisuals' kdoc.
 */
@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    categoryName: String,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val (tint, container) = CategoryVisuals.colorPairFor(categoryName)
        IconBadge(
            icon = CategoryVisuals.iconFor(categoryName),
            tint = tint,
            containerColor = container,
            size = 40.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.merchantRaw, style = MaterialTheme.typography.bodyLarge)
            Text(
                "$categoryName · ${dateFormat.format(Date(transaction.date))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        val isCredit = transaction.direction == TransactionDirection.CREDIT
        val sign = if (isCredit) "+" else "-"
        Text(
            "$sign₹${"%.2f".format(transaction.amount)}",
            style = AmountBody,
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
