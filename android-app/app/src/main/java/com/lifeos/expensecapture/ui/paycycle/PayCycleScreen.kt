package com.lifeos.expensecapture.ui.paycycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.StatTile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pay Cycle - see PayCycleViewModel's kdoc for the "Salary/Income" category anchor this whole
 * screen depends on. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayCycleScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        PayCycleViewModel(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Cycle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!uiState.hasSalaryData) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "This looks at your spending between salary credits instead of calendar " +
                        "months - useful since salary doesn't always land on the same date. " +
                        "It needs at least one transaction categorized as \"Salary/Income\" to " +
                        "know where a cycle starts - recategorize your salary credit in the " +
                        "Ledger to \"Salary/Income\" to turn this on.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionLabel("Current cycle - since ${dateFormat.format(Date(uiState.currentCycleStart!!))}")
                }
                item {
                    CycleStatGrid(income = uiState.currentCycleIncome, expenses = uiState.currentCycleExpenses)
                }
                if (uiState.lastCycleStart != null && uiState.lastCycleEnd != null) {
                    item {
                        SectionLabel(
                            "Last full cycle - ${dateFormat.format(Date(uiState.lastCycleStart!!))} to " +
                                dateFormat.format(Date(uiState.lastCycleEnd!!))
                        )
                    }
                    item {
                        CycleStatGrid(income = uiState.lastCycleIncome, expenses = uiState.lastCycleExpenses)
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleStatGrid(income: Double, expenses: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                icon = Icons.Filled.TrendingUp,
                iconTint = MaterialTheme.colorScheme.primary,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                label = "Income",
                value = "₹${"%.2f".format(income)}",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Filled.TrendingDown,
                iconTint = MaterialTheme.colorScheme.error,
                iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                label = "Expenses",
                value = "₹${"%.2f".format(expenses)}",
                modifier = Modifier.weight(1f)
            )
        }
        StatTile(
            icon = Icons.Filled.Savings,
            iconTint = MaterialTheme.colorScheme.tertiary,
            iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            label = "Saved this cycle",
            value = "₹${"%.2f".format(income - expenses)}",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
