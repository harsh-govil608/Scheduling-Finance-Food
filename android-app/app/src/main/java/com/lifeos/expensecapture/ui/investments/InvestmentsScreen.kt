package com.lifeos.expensecapture.ui.investments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import com.lifeos.expensecapture.data.db.entity.InvestmentType
import com.lifeos.expensecapture.finance.AmfiNavRepository
import com.lifeos.expensecapture.finance.AmfiScheme
import com.lifeos.expensecapture.ui.common.IconBadge
import com.lifeos.expensecapture.ui.common.SummaryStatCard
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

/**
 * Investments (Future) PRD, Phase 3 Doc 23, extended 2026-08 with mutual-fund NAV sync (real
 * user request - see InvestmentEntity's kdoc). A MANUAL holding is still exactly what the PRD
 * originally scoped: read-only, user-typed value, no sync. A MUTUAL_FUND holding is searched by
 * scheme name against AMFI's free public NAV data and kept current by InvestmentSyncTracker's
 * periodic sync - its currentValue is never hand-typed. Stocks still aren't supported (no free
 * public price source exists) - only "Name + value" manual entry covers that case, same as before.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember { InvestmentsViewModel(app.database.investmentDao()) }
    val investments by viewModel.investments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val total = investments.sumOf { it.currentValue }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add holding")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SummaryStatCard(
                icon = Icons.Filled.TrendingUp,
                label = "Total",
                value = "₹${"%.2f".format(total)}",
                caption = if (investments.isNotEmpty()) {
                    "${investments.size} holding${if (investments.size == 1) "" else "s"}"
                } else {
                    null
                },
                modifier = Modifier.padding(16.dp)
            )
            if (investments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No holdings added yet. Add a mutual fund to track it against real NAV data, or anything else (stocks, FDs) manually.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(investments, key = { it.id }) { investment ->
                        InvestmentCard(investment = investment, onDelete = { viewModel.delete(investment) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHoldingDialog(
            onAddManual = { name, value -> viewModel.addManual(name, value); showAddDialog = false },
            onAddMutualFund = { scheme, units -> viewModel.addMutualFund(scheme, units); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun InvestmentCard(investment: InvestmentEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Filled.TrendingUp,
                tint = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                size = 36.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    investment.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (investment.type == InvestmentType.MUTUAL_FUND) {
                    Text(
                        "${"%.3f".format(investment.units ?: 0.0)} units - NAV ${investment.lastNavUpdatedAt?.let { relativeTime(it) } ?: "not yet synced"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("₹${"%.2f".format(investment.currentValue)}", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onDelete) { Text("Remove") }
        }
    }
}

private fun relativeTime(epochMillis: Long): String {
    val days = (System.currentTimeMillis() - epochMillis) / 86_400_000L
    return when {
        days <= 0L -> "today"
        days == 1L -> "1 day ago"
        else -> "$days days ago"
    }
}

private enum class HoldingMode { MANUAL, MUTUAL_FUND }

@Composable
private fun AddHoldingDialog(
    onAddManual: (name: String, currentValue: Double) -> Unit,
    onAddMutualFund: (scheme: AmfiScheme, units: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(HoldingMode.MUTUAL_FUND) }

    // Manual mode state
    var name by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }

    // Mutual fund mode state - schemes fetched once when the dialog opens, filtered client-side
    // as the user types. AMFI's file has 10,000+ schemes, so only searching (not browsing) makes
    // sense, and matches are capped to keep the dialog's list short and fast to render.
    var schemes by remember { mutableStateOf<List<AmfiScheme>>(emptyList()) }
    var loadingSchemes by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedScheme by remember { mutableStateOf<AmfiScheme?>(null) }
    var unitsText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val result = AmfiNavRepository.fetchAll()
        schemes = result
        loadFailed = result.isEmpty()
        loadingSchemes = false
    }

    val matches = remember(query, schemes) {
        if (query.length < 3) emptyList() else schemes.filter { it.schemeName.contains(query, ignoreCase = true) }.take(20)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a holding") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { mode = HoldingMode.MUTUAL_FUND }) {
                        Text(
                            "Mutual Fund",
                            color = if (mode == HoldingMode.MUTUAL_FUND) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { mode = HoldingMode.MANUAL }) {
                        Text(
                            "Manual (stock, FD, etc.)",
                            color = if (mode == HoldingMode.MANUAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

                if (mode == HoldingMode.MANUAL) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (e.g. Stock, FD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        label = { Text("Current value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    when {
                        loadingSchemes -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                            Text("Loading fund list...")
                        }
                        loadFailed -> Text(
                            "Couldn't reach AMFI's fund list right now - check your connection and try again, or add this manually instead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        selectedScheme != null -> {
                            Text(selectedScheme!!.schemeName, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { selectedScheme = null }) { Text("Change fund") }
                            OutlinedTextField(
                                value = unitsText,
                                onValueChange = { unitsText = it },
                                label = { Text("Units held") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "Latest NAV: ₹${"%.4f".format(selectedScheme!!.nav)} (as of ${selectedScheme!!.date})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("Search fund name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
                                items(matches) { scheme ->
                                    Text(
                                        scheme.schemeName,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedScheme = scheme }
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (mode == HoldingMode.MANUAL) {
                    val value = valueText.toDoubleOrNull()
                    if (name.isNotBlank() && value != null) onAddManual(name, value)
                } else {
                    val units = unitsText.toDoubleOrNull()
                    val scheme = selectedScheme
                    if (scheme != null && units != null && units > 0.0) onAddMutualFund(scheme, units)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
