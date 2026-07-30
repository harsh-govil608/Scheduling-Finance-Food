package com.lifeos.expensecapture.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(app: App, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { PermissionsViewModel(context, app.database.consentDao()) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.rows) { row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = if (row.wasRevoked) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    } else {
                        CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(row.label, style = MaterialTheme.typography.bodyLarge)
                        Text(row.explanation, style = MaterialTheme.typography.bodySmall)
                        Text(
                            when {
                                row.wasRevoked -> "Turned off outside the app - some features are paused"
                                row.isGranted -> "Granted"
                                else -> "Not granted"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) { Text("Open app settings") }
                    }
                }
            }
        }
    }
}
