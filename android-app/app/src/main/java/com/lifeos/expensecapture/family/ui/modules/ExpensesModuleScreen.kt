package com.lifeos.expensecapture.family.ui.modules

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyLedgerRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.SharedExpenseRepository
import com.lifeos.expensecapture.family.model.FamilyLedgerEntry
import com.lifeos.expensecapture.family.model.SharedExpense
import com.lifeos.expensecapture.family.ui.FamilyPillar
import com.lifeos.expensecapture.family.ui.FamilyPillarBottomBar
import com.lifeos.expensecapture.ui.analytics.BarChart
import com.lifeos.expensecapture.ui.analytics.ChartLegendRow
import com.lifeos.expensecapture.ui.analytics.DonutChart
import com.lifeos.expensecapture.ui.analytics.DonutSlice
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private enum class ExpensesTab { OVERVIEW, TRANSACTIONS }

/** Shared Expenses module (2026-08 Family module, restyled 2026-08 to match `ui3/` reference's
 * Overview/Transactions tabs). Overview reads the real family ledger (see FamilyLedgerRepository's
 * kdoc - SMS-auto-synced across every member's phone), grouped by category and by week for this
 * month; Transactions is the pre-existing manually-added SharedExpense list unchanged - see
 * SharedExpense's kdoc for why that's a deliberately separate, smaller "who paid this shared cost"
 * ledger rather than the same data as Overview's auto-synced spend. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesModuleScreen(familyId: String, onBackToFinance: () -> Unit, onSelectPillar: (FamilyPillar) -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = { IconButton(onClick = onBackToFinance) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Finance") } }
            )
        },
        bottomBar = { FamilyPillarBottomBar(current = FamilyPillar.EXPENSES, onSelect = onSelectPillar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Transactions") })
            }
            when (ExpensesTab.entries[selectedTab]) {
                ExpensesTab.OVERVIEW -> ExpensesOverviewTab(familyId)
                ExpensesTab.TRANSACTIONS -> ExpensesTransactionsTab(familyId)
            }
        }
    }
}

@Composable
private fun ExpensesOverviewTab(familyId: String) {
    val ledgerRepository = remember { FamilyLedgerRepository() }
    val monthStartMillis = remember {
        LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val entries by remember(familyId) { ledgerRepository.observeEntries(familyId, monthStartMillis) }
        .collectAsState(initial = emptyList())
    val debits = entries.filter { it.direction == "DEBIT" }

    if (debits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                "No family spend synced yet this month - this fills in as members' phones auto-capture transactions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = debits.sumOf { it.amount }
    val byCategory = debits.groupBy { it.categoryName.ifBlank { "Uncategorized" } }
        .mapValues { it.value.sumOf { entry -> entry.amount } }
        .entries.sortedByDescending { it.value }
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        Warning,
        WarningStrong,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.secondary
    )
    val weeklyTotals = weeklyTotals(debits)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Total Spent (This Month)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${"%.2f".format(total)}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DonutChart(
                            slices = byCategory.mapIndexed { index, entry -> DonutSlice(entry.key, entry.value, colors[index % colors.size]) },
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            byCategory.forEachIndexed { index, entry ->
                                ChartLegendRow(
                                    color = colors[index % colors.size],
                                    label = entry.key,
                                    valueText = "₹${"%.0f".format(entry.value)}"
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Weekly Trend", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    BarChart(
                        labels = weeklyTotals.map { it.first },
                        series = listOf("This month" to weeklyTotals.map { it.second }),
                        colors = listOf(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

/** Buckets this month's debit entries into calendar weeks (Week 1 = days 1-7, etc.) - a plain,
 * honest simplification of the reference's daily trend chart; BarChart's label/bar count are 1:1
 * (see AnalyticsCharts.kt), so 30 individual day labels would overflow the row that renders them. */
private fun weeklyTotals(debits: List<FamilyLedgerEntry>): List<Pair<String, Float>> {
    val zone = ZoneId.systemDefault()
    val byWeek = debits.groupBy { entry ->
        val day = java.time.Instant.ofEpochMilli(entry.date).atZone(zone).toLocalDate().dayOfMonth
        (day - 1) / 7
    }
    val weekCount = (byWeek.keys.maxOrNull() ?: 0) + 1
    return (0 until weekCount).map { week ->
        "Week ${week + 1}" to (byWeek[week]?.sumOf { it.amount } ?: 0.0).toFloat()
    }
}

@Composable
private fun ExpensesTransactionsTab(familyId: String) {
    val authRepository = remember { FamilyAuthRepository() }
    val familyRepository = remember { FamilyRepository() }
    val repository = remember(familyId) { SharedExpenseRepository(familyId = familyId) }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val currentUserName = authRepository.currentUser?.displayName ?: ""
    val coroutineScope = rememberCoroutineScope()

    // remember()'d keyed on familyId (2026-08-15 fix) - see TasksModuleScreen.kt's identical fix
    // for why an inline observeX().collectAsState() recreates the Firestore listener on every
    // recomposition instead of reusing one.
    val expenses by remember(familyId) { repository.observeAll() }.collectAsState(initial = emptyList())
    val members by remember(familyId) { familyRepository.observeMembers(familyId) }.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No shared expenses yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses, key = { it.id }) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Paid by ${members.firstOrNull { it.userId == expense.paidByUserId }?.displayName ?: "someone"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("₹${"%.2f".format(expense.amount)}", style = MaterialTheme.typography.bodyLarge)
                                // Wires up SharedExpenseRepository.delete (2026-08, real user
                                // request: "small improvements" - the repository method already
                                // existed, fully implemented, but nothing in this screen ever
                                // called it).
                                IconButton(onClick = { coroutineScope.launch { repository.delete(expense.id) } }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete expense",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Add expense") }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onConfirm = { description, amount ->
                coroutineScope.launch {
                    repository.add(
                        SharedExpense(
                            familyId = familyId,
                            description = description,
                            amount = amount,
                            paidByUserId = currentUserId,
                            date = System.currentTimeMillis(),
                            createdAt = System.currentTimeMillis()
                        ),
                        currentUserName
                    )
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddExpenseDialog(onConfirm: (description: String, amount: Double) -> Unit, onDismiss: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (description.isNotBlank() && amount != null) onConfirm(description.trim(), amount)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
