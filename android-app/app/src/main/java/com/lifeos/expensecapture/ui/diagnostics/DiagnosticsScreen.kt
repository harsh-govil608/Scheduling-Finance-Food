package com.lifeos.expensecapture.ui.diagnostics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pre-beta hardening (Priority 2): a plain read-only list of what CrashHandler/AppLogger have
 * captured, with a share-sheet export - the same "nothing leaves the device unless the user
 * explicitly shares it" pattern the CSV export already uses. Reachable from Profile, not a new
 * product feature - this is production-support tooling, not something aimed at day-to-day use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(app: App, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { DiagnosticsViewModel(app.database.crashLogDao()) }
    val entries by viewModel.entries.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = {
                            val uri = DiagnosticsExporter.export(context, entries)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share diagnostics"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share diagnostics")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing captured yet - this is where errors will show up if something goes wrong, kept only on this device.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.id }) { entry -> CrashLogRow(entry, dateFormat) }
                }
                TextButton(
                    onClick = { viewModel.clearAll() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Clear all") }
            }
        }
    }
}

@Composable
private fun CrashLogRow(entry: CrashLogEntity, dateFormat: SimpleDateFormat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                if (entry.fatal) "Fatal - ${entry.exceptionType}" else "Handled - ${entry.exceptionType}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (entry.fatal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${dateFormat.format(Date(entry.timestamp))} - v${entry.appVersionName}" +
                    (entry.source?.let { " - $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entry.message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
