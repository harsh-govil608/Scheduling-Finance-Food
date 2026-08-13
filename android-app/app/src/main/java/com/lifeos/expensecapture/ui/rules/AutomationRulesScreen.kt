package com.lifeos.expensecapture.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationRulesScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        AutomationRulesViewModel(app.database.merchantRuleDao(), app.database.categoryDao())
    }
    val rules by viewModel.rules.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automation Rules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create a rule")
            }
        }
    ) { padding ->
        if (rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No rules yet. Rules are created automatically when you recategorize a transaction, or you can write one yourself with the + button.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.rule.id }) { row ->
                    RuleCard(
                        row = row,
                        categories = categories,
                        onTogglePause = { viewModel.togglePause(row.rule) },
                        onChangeCategory = { categoryId -> viewModel.changeCategory(row.rule, categoryId) },
                        onDelete = { viewModel.delete(row.rule) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            categories = categories,
            onConfirm = { pattern, categoryId ->
                viewModel.createRule(pattern, categoryId)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun RuleCard(
    row: RuleRow,
    categories: List<CategoryEntity>,
    onTogglePause: () -> Unit,
    onChangeCategory: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Third origin (2026-08, real user request: predefined categorization rules) - a seeded
    // default is neither "created by you" nor "learned from a correction you made", so it needs
    // its own label rather than falling into the "Learned from a correction" branch, which would
    // otherwise mislabel it.
    val origin = when {
        row.rule.isManuallyAuthored -> "You created this"
        row.rule.isSeededDefault -> "Built-in default"
        else -> "Learned from a correction"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("\"${row.rule.merchantPattern}\" → always ${row.categoryName}", style = MaterialTheme.typography.bodyLarge)
            }
            Text(origin, style = MaterialTheme.typography.bodySmall)
            if (row.rule.isPaused) {
                Text("Paused - not currently applying", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                Box {
                    TextButton(onClick = { expanded = true }) { Text("Change category") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { onChangeCategory(category.id); expanded = false }
                            )
                        }
                    }
                }
                TextButton(onClick = onTogglePause) { Text(if (row.rule.isPaused) "Resume" else "Pause") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun AddRuleDialog(
    categories: List<CategoryEntity>,
    onConfirm: (pattern: String, categoryId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a rule") },
        text = {
            Column {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Merchant name contains") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedCategory?.name ?: "Choose a category")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val category = selectedCategory
                if (pattern.isNotBlank() && category != null) {
                    onConfirm(pattern, category.id)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
