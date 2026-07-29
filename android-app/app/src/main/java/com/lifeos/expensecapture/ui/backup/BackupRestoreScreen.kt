package com.lifeos.expensecapture.ui.backup

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.export.BackupExporter
import com.lifeos.expensecapture.export.BackupImporter
import com.lifeos.expensecapture.export.RestoreResult
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Backup & Restore (built via a real user request, 2026-07 - "if my phone breaks tomorrow, I
 * lose everything" was the single blocker identified to this app being worth paying for). See
 * BackupExporter/BackupImporter's kdocs for the on-device-only design: no cloud sync, no
 * backend, no account - a file the user shares to wherever they trust (Drive, email, a USB
 * drive), which is the only thing that actually survives a lost or broken phone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(app: App, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBusy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isBusy = true
        coroutineScope.launch {
            val looksValid = withContext(Dispatchers.IO) {
                BackupImporter.looksLikeValidBackup(context, uri)
            }
            isBusy = false
            if (looksValid) {
                pendingRestoreUri = uri
                showRestoreConfirm = true
            } else {
                errorMessage = "That doesn't look like a backup made by this app - nothing was changed."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Everything on this device - every transaction, budget, task, and habit - lives " +
                    "only on this phone. Back up regularly so a lost or broken phone doesn't mean " +
                    "losing your history. Nothing here ever goes to a server - you choose where the " +
                    "backup file is saved or shared.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    isBusy = true
                    coroutineScope.launch {
                        try {
                            val uri = withContext(Dispatchers.IO) {
                                BackupExporter.exportDatabase(context, app.database)
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/octet-stream"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Save backup"))
                        } catch (e: Exception) {
                            AppLogger.e("BackupRestoreScreen", "backup export failed", e)
                            errorMessage = "Something went wrong creating the backup."
                        } finally {
                            isBusy = false
                        }
                    }
                },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Back up now") }

            OutlinedButton(
                onClick = {
                    errorMessage = null
                    restoreLauncher.launch(arrayOf("*/*"))
                },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Restore from backup") }

            if (isBusy) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Replace all current data?") },
            text = {
                Text(
                    "Restoring this backup permanently replaces every transaction, budget, task, " +
                        "and habit currently on this device with what's in the backup file. " +
                        "This cannot be undone - if you want to keep what's here now, back it up " +
                        "first. The app will restart to finish restoring."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingRestoreUri
                    showRestoreConfirm = false
                    if (uri != null) {
                        isBusy = true
                        coroutineScope.launch {
                            when (val result = withContext(Dispatchers.IO) {
                                BackupImporter.restoreDatabase(context, uri)
                            }) {
                                is RestoreResult.Success -> restartApp(context)
                                is RestoreResult.InvalidFile ->
                                    errorMessage = "That doesn't look like a backup made by this app - nothing was changed."
                                is RestoreResult.Failed -> errorMessage = result.message
                            }
                            isBusy = false
                        }
                    }
                }) { Text("Replace and restart", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/** Room's live AppDatabase singleton was already closed by BackupImporter before this runs -
 * a mid-life file swap under an open database connection isn't something any ViewModel/Flow in
 * this app is built to detect and re-subscribe to, so the whole process restarts fresh against
 * the restored file instead. */
private fun restartApp(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val restartIntent = Intent.makeRestartActivityTask(launchIntent?.component)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}
