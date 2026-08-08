package com.lifeos.expensecapture.ui.diagnostics

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import com.lifeos.expensecapture.sms.SmsScanDiagnostics
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
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
    val viewModel = remember { DiagnosticsViewModel(context, app.database.crashLogDao()) }
    val entries by viewModel.entries.collectAsState()
    val smsScanState by viewModel.smsScanState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    // Bug fix (real user report, 2026-08): this used to wipe the whole crash/error log on a
    // single tap, the only destructive action in the app with no confirm dialog - every other
    // delete (Profile's "Delete all data", Budget/Bills/Ledger/Habits/Goals removes) gates behind
    // an AlertDialog first.
    var showClearConfirm by remember { mutableStateOf(false) }

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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SmsScanDiagnosticsCard(smsScanState, onRunScan = viewModel::runSmsScanDiagnostics) }

            if (entries.isEmpty()) {
                item {
                    Text(
                        "Nothing captured yet - this is where errors will show up if something goes wrong, kept only on this device.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(entries, key = { it.id }) { entry -> CrashLogRow(entry, dateFormat) }
                item {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Clear all") }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all diagnostics?") },
            text = { Text("This permanently deletes every error/crash record kept on this device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearConfirm = false
                }) { Text("Clear all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Friendlier by default (real founder request, 2026-08: "koi diagnostic click karega to usko
 * codes dikhega?" - the raw exception class name, e.g. "java.lang.NullPointerException", read as
 * unexplained "code" to anyone non-technical looking at this screen). The plain-language headline
 * is what shows by default; the exception type/message stay available, just tucked behind a
 * "Show technical details" toggle instead of always visible - still exactly what
 * DiagnosticsExporter's share output includes in full either way, this only changes the in-app
 * list's default view.
 */
@Composable
private fun CrashLogRow(entry: CrashLogEntity, dateFormat: SimpleDateFormat) {
    var showTechnical by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                if (entry.fatal) "The app closed unexpectedly" else "A minor issue was handled automatically",
                style = MaterialTheme.typography.bodyLarge,
                color = if (entry.fatal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${dateFormat.format(Date(entry.timestamp))} - v${entry.appVersionName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { showTechnical = !showTechnical },
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(if (showTechnical) "Hide technical details" else "Show technical details")
            }
            if (showTechnical) {
                Text(
                    entry.exceptionType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                entry.source?.let {
                    Text("Source: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * SMS capture audit (2026-08, real founder request): a read-only, on-demand breakdown of what a
 * full inbox scan would actually do - see SmsDiagnosticsScanner's kdoc. This is the tool for
 * answering "why is my ledger missing transactions I can see in my SMS app" without pulling the
 * raw database file, and for judging whether the "silently skipped" count for a given sender is
 * worth adding to TransactionParser's bank-recognition allowlist.
 */
@Composable
private fun SmsScanDiagnosticsCard(state: SmsScanState, onRunScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("SMS Scan Diagnostics", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Runs a read-only pass over your full SMS inbox - nothing is added, changed, or sent anywhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            when (state) {
                is SmsScanState.Idle -> {
                    Button(onClick = onRunScan) { Text("Run SMS scan diagnostics") }
                }
                is SmsScanState.Running -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Scanning your inbox…")
                    }
                }
                is SmsScanState.Failed -> {
                    Text(
                        "Scan failed - see the error log below for details.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onRunScan) { Text("Try again") }
                }
                is SmsScanState.PermissionMissing -> {
                    Text(
                        "SMS permission isn't granted, so this can't read your inbox. Grant access from Manage Permissions first.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is SmsScanState.Result -> {
                    SmsScanResultRows(state.diagnostics)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onRunScan) { Text("Run again") }
                }
            }
        }
    }
}

@Composable
private fun SmsScanResultRows(d: SmsScanDiagnostics) {
    Column {
        SmsScanResultRow("Total SMS found", d.totalSmsFound)
        SmsScanResultRow("Financial SMS detected", d.financialCandidates)
        SmsScanResultRow("Successfully parsed", d.parsed)
        SmsScanResultRow("Already captured (duplicates)", d.duplicatesAlreadyCaptured)
        SmsScanResultRow("Needs Review (parse failed, recognized sender)", d.needsReview)
        SmsScanResultRow("  - of which eligible for AI fallback", d.wouldTryAiFallback)
        SmsScanResultRow("Ignored (OTP/verification/promo)", d.ignoredOtpOrPromo)
        SmsScanResultRow("Silently skipped (unrecognized sender)", d.silentlySkipped)
        if (d.silentlySkippedSenders.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Skipped senders: ${d.silentlySkippedSenders.take(10).joinToString(", ")}" +
                    if (d.silentlySkippedSenders.size > 10) ", …" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SmsScanResultRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$value", style = MaterialTheme.typography.bodyMedium)
    }
}
