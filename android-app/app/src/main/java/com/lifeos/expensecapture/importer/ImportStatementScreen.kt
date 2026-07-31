package com.lifeos.expensecapture.importer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

/**
 * Import Statement (real user review: "Just give us the option to upload credit card
 * statement") - see CsvParser's kdoc for why this is a generic column-mapping flow rather than
 * per-issuer templates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStatementScreen(app: App, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        ImportStatementViewModel(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao(),
            merchantRuleDao = app.database.merchantRuleDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (text != null) viewModel.loadCsv(text)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Statement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.importComplete) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Imported ${uiState.importedCount} transaction${if (uiState.importedCount == 1) "" else "s"}.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Anything that looked like a duplicate of something already in your Ledger was skipped automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Done") }
                }
            }
        } else if (uiState.headers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Pick a CSV export from your bank or card issuer's app/website. " +
                            "You'll match up its columns on the next screen - every issuer's " +
                            "format is different, so this works with whatever yours looks like.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { filePicker.launch("text/*") }) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose CSV file")
                    }
                    uiState.loadError?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ColumnMappingCard(uiState = uiState, viewModel = viewModel)
                }
                if (uiState.isMappingComplete) {
                    item { Text("Preview", style = MaterialTheme.typography.titleSmall) }
                    items(viewModel.previewRows()) { parsed ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (parsed == null) {
                                    Text("Couldn't read this row - will be skipped", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Column {
                                        Text(parsed.merchant, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            if (parsed.date == null) "Unrecognized date" else "Date OK",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "${if (parsed.direction == TransactionDirection.DEBIT) "-" else "+"}₹${"%.2f".format(parsed.amount ?: 0.0)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.import() },
                            enabled = !uiState.importing,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (uiState.importing) "Importing..." else "Import ${uiState.dataRows.size} rows") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnMappingCard(uiState: ImportStatementUiState, viewModel: ImportStatementViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Match your file's columns", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            ColumnPicker("Date column", uiState.headers, uiState.dateColumn) { viewModel.setDateColumn(it) }
            Spacer(Modifier.height(8.dp))
            ColumnPicker("Description column", uiState.headers, uiState.descriptionColumn) { viewModel.setDescriptionColumn(it) }
            Spacer(Modifier.height(8.dp))
            ColumnPicker("Amount column", uiState.headers, uiState.amountColumn) { viewModel.setAmountColumn(it) }
            Spacer(Modifier.height(12.dp))

            Text("Date format", style = MaterialTheme.typography.bodyMedium)
            var dateFormatExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { dateFormatExpanded = true }) { Text(uiState.dateFormat) }
                DropdownMenu(expanded = dateFormatExpanded, onDismissRequest = { dateFormatExpanded = false }) {
                    SUPPORTED_DATE_FORMATS.forEach { format ->
                        DropdownMenuItem(text = { Text(format) }, onClick = { viewModel.setDateFormat(format); dateFormatExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Positive amounts are money spent", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Turn off if your file does it the other way around",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = uiState.positiveMeansDebit, onCheckedChange = { viewModel.setPositiveMeansDebit(it) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnPicker(label: String, headers: List<String>, selected: Int?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selected?.let { headers.getOrNull(it) } ?: "Choose...")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                headers.forEachIndexed { index, header ->
                    DropdownMenuItem(text = { Text(header) }, onClick = { onSelect(index); expanded = false })
                }
            }
        }
    }
}
