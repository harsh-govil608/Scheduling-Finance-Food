package com.lifeos.expensecapture.ui.budget

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.ui.theme.AmountBody
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong

/**
 * Budget Planner PRD, Phase 3 Doc 20. Supports category + overall budgets (goal-linked
 * deferred, per the PRD's own open question). Spend Prediction (Doc 21) is folded in here as
 * the "projected month-end" line on each card rather than a separate screen - see
 * docs/coders-documentation/day-2.md for that scope consolidation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        BudgetViewModel(
            insightsRepository = FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            ),
            categoriesFlow = app.database.categoryDao().observeAll()
        )
    }
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add budget")
            }
        }
    ) { padding ->
        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No budgets set yet. Tap + to set one - an overall limit or per category.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(budgets, key = { it.budget.id }) { progress ->
                    BudgetCard(progress, onDelete = { viewModel.deleteBudget(progress) })
                }
            }
        }
    }

    if (showAddDialog) {
        BudgetEditDialog(
            categories = categories,
            onSuggestedDefault = { categoryId -> viewModel.suggestedDefault(categoryId) },
            onConfirm = { categoryId, limit ->
                viewModel.setBudget(categoryId, limit)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun BudgetCard(progress: FinanceInsightsRepository.BudgetProgress, onDelete: () -> Unit) {
    val ratio = (progress.spentThisMonth / progress.budget.monthlyLimit).coerceIn(0.0, 1.0)
    // "Over budget" is informational, not a system failure - deliberately stays in the amber
    // family rather than escalating to MaterialTheme.colorScheme.error's alarm-red, per the
    // Design System PRD's "encourage, never guilt" rule for semantic color. See Color.kt.
    val barColor = when {
        progress.spentThisMonth > progress.budget.monthlyLimit -> WarningStrong
        ratio > 0.7 -> Warning
        else -> MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(progress.categoryName, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onDelete) { Text("Remove") }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "₹${"%.2f".format(progress.spentThisMonth)} of ₹${"%.2f".format(progress.budget.monthlyLimit)}",
                style = AmountBody
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = barColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                projectionText(progress),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun projectionText(progress: FinanceInsightsRepository.BudgetProgress): String {
    return when (progress.predictionConfidence) {
        FinanceInsightsRepository.PredictionConfidence.INSUFFICIENT_DATA ->
            "Not enough data yet this month for a month-end projection."
        FinanceInsightsRepository.PredictionConfidence.LOW ->
            "Rough projection (low confidence, early in the month): ₹${"%.2f".format(progress.projectedMonthEndSpend)} by month end."
        FinanceInsightsRepository.PredictionConfidence.MEDIUM ->
            "Projected by month end: ₹${"%.2f".format(progress.projectedMonthEndSpend)} (based on your pace so far, not a guarantee)."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEditDialog(
    categories: List<CategoryEntity>,
    onSuggestedDefault: suspend (Long?) -> Double,
    onConfirm: (categoryId: Long?, monthlyLimit: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) } // null = Overall
    var expanded by remember { mutableStateOf(false) }
    var limitText by remember { mutableStateOf("") }
    var suggested by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(selectedCategory) {
        suggested = onSuggestedDefault(selectedCategory?.id)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set a budget") },
        text = {
            Column {
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedCategory?.name ?: "Overall (all categories)")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Overall (all categories)") },
                            onClick = { selectedCategory = null; expanded = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly limit") },
                    modifier = Modifier.fillMaxWidth()
                )
                suggested?.let { value ->
                    if (value > 0) {
                        TextButton(onClick = { limitText = "%.2f".format(value) }) {
                            Text("Use suggested: ₹${"%.2f".format(value)} (avg. of your recent spend)")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val limit = limitText.toDoubleOrNull()
                if (limit != null && limit > 0) {
                    onConfirm(selectedCategory?.id, limit)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
