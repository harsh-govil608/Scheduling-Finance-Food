package com.lifeos.expensecapture.ui.profile

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    app: App,
    onBack: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAutomationRules: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onDataDeleted: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { ProfileViewModel(context, app.database) }
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            val photoPickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri -> uri?.let { viewModel.setProfilePhoto(it) } }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val photoBitmap = remember(uiState.profilePhotoPath) {
                    uiState.profilePhotoPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    TextButton(onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text(if (uiState.profilePhotoPath != null) "Change photo" else "Add photo") }
                    if (uiState.profilePhotoPath != null) {
                        TextButton(onClick = { viewModel.removeProfilePhoto() }) { Text("Remove photo") }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::setDisplayName,
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pause automatic capture", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Temporarily stop reading new SMS without revoking the permission itself.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = uiState.capturePaused, onCheckedChange = viewModel::setCapturePaused)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(onClick = onOpenPermissions) { Text("Manage permissions") }
            TextButton(onClick = onOpenAutomationRules) { Text("Automation rules") }
            TextButton(onClick = onOpenCategories) { Text("Manage categories") }
            TextButton(onClick = onOpenCalculator) { Text("Calculator") }
            TextButton(onClick = onOpenDiagnostics) { Text("Diagnostics") }
            TextButton(onClick = onOpenBackupRestore) { Text("Backup & Restore") }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            TextButton(onClick = { showDeleteConfirm = true }) {
                Text("Delete all my data", color = MaterialTheme.colorScheme.error)
            }

            // Real user report, 2026-08: an update banner never showed on a tester's phone, and
            // there was no way to even ask "what version are you actually on" - the app never
            // displayed its own version anywhere. A one-line read-only label fixes that; it's
            // also the fastest sanity check that the in-app updater (UpdateChecker) actually
            // landed a given release.
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Version ${uiState.appVersionName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete all data?") },
            text = {
                Text(
                    "This permanently erases every transaction, budget, subscription, bill, " +
                        "and rule stored on this device. There's no server-side account to " +
                        "delete separately - this is everything. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData { onDataDeleted() }
                    showDeleteConfirm = false
                }) { Text("Delete everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
