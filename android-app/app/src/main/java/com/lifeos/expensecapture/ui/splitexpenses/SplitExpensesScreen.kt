package com.lifeos.expensecapture.ui.splitexpenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.StatusChip
import com.lifeos.expensecapture.ui.common.SummaryStatCard
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.AmountLarge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Split Expenses landing screen - see SplitExpenseEntity's kdoc for the single-device scope
 * this whole feature operates in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitExpensesScreen(app: App, onBack: () -> Unit, onAddExpense: () -> Unit, onOpenDetail: (Long) -> Unit) {
    val viewModel = remember {
        SplitExpensesViewModel(
            splitExpenseDao = app.database.splitExpenseDao(),
            splitParticipantDao = app.database.splitParticipantDao()
        )
    }
    val rows by viewModel.rows.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) {
                Icon(Icons.Default.Add, contentDescription = "Add split expense")
            }
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No split expenses yet. Log something you paid for a group here, and " +
                        "track who's paid you back."
                )
            }
        } else {
            val totalOwed = rows.sumOf { it.owedToYou }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryStatCard(
                        icon = Icons.Filled.Groups,
                        label = "Total owed to you",
                        value = "₹${"%.2f".format(totalOwed)}",
                        caption = "${rows.size} expense${if (rows.size == 1) "" else "s"} logged"
                    )
                }
                items(rows, key = { it.expense.id }) { row ->
                    SplitExpenseRowCard(row = row, onClick = { onOpenDetail(row.expense.id) })
                }
            }
        }
    }
}

@Composable
private fun SplitExpenseRowCard(row: SplitExpenseRow, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        row.expense.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${dateFormat.format(Date(row.expense.date))} · ${row.participants.size} " +
                            "${if (row.participants.size == 1) "person" else "people"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("₹${"%.2f".format(row.expense.totalAmount)}", style = AmountLarge)
            }
            Spacer(Modifier.height(10.dp))
            if (row.allSettled) {
                StatusChip("Settled", MaterialTheme.colorScheme.primary)
            } else {
                StatusChip("₹${"%.2f".format(row.owedToYou)} owed", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
