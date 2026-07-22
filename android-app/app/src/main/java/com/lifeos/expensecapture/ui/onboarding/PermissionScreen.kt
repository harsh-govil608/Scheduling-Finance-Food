package com.lifeos.expensecapture.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import kotlinx.coroutines.launch

/**
 * Minimal, honest version of the Phase 3 Permissions & Consent PRD: explain in plain
 * language what is and isn't read/transmitted BEFORE asking for the permission
 * (architecture doc Section 9's non-negotiables).
 *
 * Also triggers the one-time SMS history scan (SmsHistoryScanner) once permission is granted -
 * without this, "automatic expense capture" would only ever see messages arriving after
 * install, not the transaction history already sitting in the user's inbox.
 */
@Composable
fun PermissionScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            isScanning = true
            scope.launch {
                SmsHistoryScanner.scanIfNeeded(context)
                onGranted()
            }
        }
    }

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (hasSmsPermission()) {
            isScanning = true
            SmsHistoryScanner.scanIfNeeded(context)
            onGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Scanning your existing messages for past transactions…")
        } else {
            Text("Automatic expense capture", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text(
                "This app reads bank and UPI transaction SMS on your phone to log expenses " +
                    "automatically. Only the amount, merchant, and date are ever stored or sent " +
                    "anywhere - the original message text never leaves your device. Other SMS " +
                    "(personal messages, OTPs) are ignored and never stored.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                )
            }) {
                Text("Grant SMS access")
            }
        }
    }
}
