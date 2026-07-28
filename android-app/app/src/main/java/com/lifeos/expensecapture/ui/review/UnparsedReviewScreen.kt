package com.lifeos.expensecapture.ui.review

import androidx.compose.foundation.layout.* // weight() resolves per-receiver (RowScope/ColumnScope);
// importing it by name alone resolved to an internal symbol during the real build - see
// android-app/README.md "Known gaps" if this surfaces again after a Compose version bump.
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.ledger.ManualEntryDialog

/**
 * Finance Suite gap-fix, not a PRD of its own: surfaces SMS the parser couldn't turn into a
 * transaction (previously silently discarded - see docs/coders-documentation/day-1.md Section
 * 7 and day-2.md for the fix). Reuses ManualEntryDialog so converting one into a real
 * transaction doesn't need a second entry form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnparsedReviewScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        UnparsedReviewViewModel(
            unparsedMessageDao = app.database.unparsedMessageDao(),
            transactionRepository = TransactionRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                merchantRuleDao = app.database.merchantRuleDao(),
                correctionDao = app.database.correctionDao()
            )
        )
    }
    val unresolved by viewModel.unresolved.collectAsState()
    var categories by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        app.database.categoryDao().observeAll().collect { categories = it }
    }
    var selectedMessage by remember { mutableStateOf<UnparsedMessageEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Needs Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (unresolved.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nothing needs review right now. Messages the parser can't confidently " +
                        "read from will show up here instead of silently disappearing.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(unresolved, key = { it.id }) { message ->
                    // Icon badge added (found via a real user report, 2026-07 - "the UI is
                    // looking too basic"): this row was plain text with no visual accent at all.
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        IconBadge(
                            icon = Icons.AutoMirrored.Filled.Message,
                            tint = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            size = 40.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(message.sender, style = MaterialTheme.typography.bodySmall)
                            Text(message.body, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { selectedMessage = message }) { Text("Convert") }
                                TextButton(onClick = { viewModel.dismiss(message) }) { Text("Dismiss") }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    selectedMessage?.let { message ->
        ManualEntryDialog(
            categories = categories,
            initialDateMillis = message.receivedAt,
            onConfirm = { amount, merchant, direction, categoryId, date ->
                viewModel.convertToTransaction(message, amount, merchant, direction, categoryId, date)
                selectedMessage = null
            },
            onDismiss = { selectedMessage = null }
        )
    }
}
