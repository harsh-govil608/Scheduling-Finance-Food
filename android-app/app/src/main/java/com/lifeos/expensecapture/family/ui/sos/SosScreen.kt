package com.lifeos.expensecapture.family.ui.sos

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.lifeos.expensecapture.family.model.GeoPoint
import com.lifeos.expensecapture.family.model.SOSAlert
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.theme.WarningStrong
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SOS trigger + active-alerts screen (2026-08 Family module PRD: "SOS workflow with live location
 * sharing and emergency notifications"). Location permission is requested only here, at the point
 * of use - never at app launch - matching this app's existing onboarding pattern (see
 * PermissionScreen). If permission is denied, SOS still fires with a null location rather than
 * being blocked entirely: an alert with no coordinates is still strictly better than no alert.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    familyId: String,
    userId: String,
    userName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember(familyId) { SosViewModel(familyId, userId, userName) }
    val uiState by viewModel.uiState.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    var pendingTrigger by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (pendingTrigger) {
            pendingTrigger = false
            captureLocationAndTrigger(context, viewModel)
        }
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS") },
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
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Tapping SOS shares your live location with every family member and " +
                            "notifies them immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (hasLocationPermission()) {
                                captureLocationAndTrigger(context, viewModel)
                            } else {
                                pendingTrigger = true
                                locationPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        },
                        enabled = !uiState.triggering,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningStrong),
                        modifier = Modifier.size(160.dp),
                        shape = CircleShape
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Emergency, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text(if (uiState.triggering) "Sending…" else "SOS", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (uiState.justTriggered) {
                        Spacer(Modifier.height(16.dp))
                        Text("Alert sent - your family has been notified.", color = MaterialTheme.colorScheme.primary)
                    }
                    uiState.lastError?.let {
                        Spacer(Modifier.height(16.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (activeAlerts.isNotEmpty()) {
                item {
                    Text("Active alerts", style = MaterialTheme.typography.titleMedium)
                }
                items(activeAlerts, key = { it.id }) { alert ->
                    ActiveAlertCard(alert = alert, onResolve = { viewModel.resolve(alert.id) })
                }
            }
        }
    }
}

private val alertTimeFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())

@Composable
private fun ActiveAlertCard(alert: SOSAlert, onResolve: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.triggeredByName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    alertTimeFormat.format(Date(alert.triggeredAt)) +
                        (alert.location?.let { " • ${"%.4f".format(it.latitude)}, ${"%.4f".format(it.longitude)}" } ?: " • location unavailable"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onResolve) { Text("Resolve") }
        }
    }
}

/** One-shot current-location fetch via the Fused Location Provider - not a continuous background
 * subscription (no wakelock/battery cost beyond this single call), matching "captured only when
 * the user actually taps SOS" from this file's own kdoc. Falls back to null on any failure
 * (permission race, provider unavailable, timeout) rather than blocking the alert. */
private fun captureLocationAndTrigger(context: android.content.Context, viewModel: SosViewModel) {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) {
        viewModel.triggerSos(null)
        return
    }
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                val point = location?.let { GeoPoint(it.latitude, it.longitude) }
                viewModel.triggerSos(point)
            }
            .addOnFailureListener {
                viewModel.triggerSos(null)
            }
    } catch (e: SecurityException) {
        viewModel.triggerSos(null)
    }
}
