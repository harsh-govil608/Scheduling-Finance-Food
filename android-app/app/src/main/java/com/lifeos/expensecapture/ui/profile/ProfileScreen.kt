package com.lifeos.expensecapture.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.export.CsvExporter
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayResult
import com.lifeos.expensecapture.splitpay.model.UserPayProfile
import com.lifeos.expensecapture.splitpay.ui.UpiPay
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Profile pillar landing surface - stats row + grouped settings sections (2026-08 reference
 * mockups, `ui2/` folder). "Premium Member" and separate "email" from the mockup are dropped:
 * this app has no accounts/payments, so a display name is the only real identity it has - see
 * ProfileViewModel's kdoc for why the stats row itself is all real counts instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    app: App,
    onOpenPermissions: () -> Unit,
    onOpenAutomationRules: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenPremium: () -> Unit,
    onSelectPillar: (Pillar) -> Unit,
    onDataDeleted: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { ProfileViewModel(context, app.database) }
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by app.billingRepository.isPremium.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPersonalInfo by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPaymentSettings by remember { mutableStateOf(false) }

    // Export Statement (2026-08, real user request: "move share button to profile and name it
    // export statement") - moved here verbatim from Finance Home's top bar, same CSV export/share
    // logic, just reached from Settings instead of a top-bar icon.
    val coroutineScope = rememberCoroutineScope()
    var showExportOptions by remember { mutableStateOf(false) }
    var showExportStartPicker by remember { mutableStateOf(false) }
    var showExportEndPicker by remember { mutableStateOf(false) }
    var exportRangeStartMillis by remember { mutableStateOf<Long?>(null) }

    suspend fun exportAndShare(transactions: List<TransactionEntity>) {
        val categories = app.database.categoryDao().observeAll().first()
        val uri = CsvExporter.exportTransactions(context, transactions) { categoryId ->
            categories.firstOrNull { it.id == categoryId }?.name ?: "Uncategorized"
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export transactions"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = { PillarBottomBar(current = Pillar.PROFILE, onSelect = onSelectPillar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val photoBitmap = remember(uiState.profilePhotoPath) {
                        uiState.profilePhotoPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showPersonalInfo = true },
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
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            uiState.displayName.ifBlank { "You" },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Version ${uiState.appVersionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        ProfileStat("${uiState.transactionCount}", "Transactions", Modifier.weight(1f))
                        ProfileStat("${uiState.goalCount}", "Goals", Modifier.weight(1f))
                        ProfileStat("${uiState.habitCount}", "Habits", Modifier.weight(1f))
                        ProfileStat("${uiState.healthScore}", "Score", Modifier.weight(1f))
                    }
                }
            }

            item {
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Filled.Star,
                        title = if (isPremium) "Premium" else "Upgrade to Premium",
                        subtitle = if (isPremium) "Thanks for your support" else "Unlimited AI questions, full Family Sharing",
                        onClick = onOpenPremium
                    )
                }
            }

            item { SectionLabel("Account") }
            item {
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Filled.Person,
                        title = "Personal Information",
                        subtitle = "Name and photo",
                        onClick = { showPersonalInfo = true }
                    )
                    SettingsRowDivider()
                    SettingsRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "Payment Settings",
                        subtitle = "Your UPI ID for Smart Split",
                        onClick = { showPaymentSettings = true }
                    )
                    SettingsRowDivider()
                    SettingsToggleRow(
                        icon = Icons.Filled.MonitorHeart,
                        title = "Pause automatic capture",
                        subtitle = "Temporarily stop reading new SMS",
                        checked = uiState.capturePaused,
                        onCheckedChange = viewModel::setCapturePaused
                    )
                }
            }

            item { SectionLabel("Family") }
            item {
                SettingsGroupCard {
                    SettingsRow(
                        icon = Icons.Filled.Groups,
                        title = "Family Sharing",
                        subtitle = "Shared tasks, calendar, expenses, documents, health, SOS",
                        onClick = onOpenFamily
                    )
                }
            }

            item { SectionLabel("Data & Security") }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Filled.IosShare, "Export Statement", subtitle = "CSV, by month/all time/custom range", onClick = { showExportOptions = true })
                    SettingsRowDivider()
                    SettingsRow(Icons.Filled.Backup, "Backup & Restore", onClick = onOpenBackupRestore)
                    SettingsRowDivider()
                    SettingsRow(Icons.Filled.Security, "Manage Permissions", onClick = onOpenPermissions)
                    SettingsRowDivider()
                    SettingsRow(Icons.Filled.Category, "Manage Categories", onClick = onOpenCategories)
                }
            }

            item { SectionLabel("Tools") }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Filled.Rule, "Automation Rules", onClick = onOpenAutomationRules)
                    SettingsRowDivider()
                    SettingsRow(Icons.Filled.Calculate, "Calculator", onClick = onOpenCalculator)
                    SettingsRowDivider()
                    SettingsRow(Icons.Filled.Assignment, "Diagnostics", onClick = onOpenDiagnostics)
                }
            }

            item { SectionLabel("Support") }
            item {
                SettingsGroupCard {
                    SettingsRow(Icons.Filled.Info, "About App", onClick = { showAbout = true })
                }
            }

            item { SectionLabel("Danger Zone") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDeleteConfirm = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text("Clear All My Data and Log Out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showPersonalInfo) {
        PersonalInfoDialog(
            displayName = uiState.displayName,
            hasPhoto = uiState.profilePhotoPath != null,
            onNameChange = viewModel::setDisplayName,
            onPhotoPicked = viewModel::setProfilePhoto,
            onRemovePhoto = viewModel::removeProfilePhoto,
            onDismiss = { showPersonalInfo = false }
        )
    }

    if (showPaymentSettings) {
        PaymentSettingsDialog(onDismiss = { showPaymentSettings = false })
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About") },
            text = { Text("Expense Capture\nVersion ${uiState.appVersionName}\n\nA local-only finance and productivity tracker - no accounts, no servers, everything stays on this device.") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close") } }
        )
    }

    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = { Text("Export Statement") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showExportOptions = false
                            coroutineScope.launch {
                                val zone = ZoneId.systemDefault()
                                val monthStart = LocalDate.now(zone).withDayOfMonth(1)
                                    .atStartOfDay(zone).toInstant().toEpochMilli()
                                val transactions = app.database.transactionDao().observeAll().first()
                                    .filter { it.date >= monthStart }
                                exportAndShare(transactions)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export this month (CSV)") }
                    TextButton(
                        onClick = {
                            showExportOptions = false
                            coroutineScope.launch {
                                val transactions = app.database.transactionDao().observeAll().first()
                                exportAndShare(transactions)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export all time (CSV)") }
                    TextButton(
                        onClick = {
                            showExportOptions = false
                            showExportStartPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export a custom range (CSV)…") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExportOptions = false }) { Text("Cancel") } }
        )
    }

    if (showExportStartPicker) {
        val startPickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showExportStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    exportRangeStartMillis = startPickerState.selectedDateMillis
                    showExportStartPicker = false
                    showExportEndPicker = true
                }) { Text("Next: end date") }
            },
            dismissButton = {
                TextButton(onClick = { showExportStartPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = startPickerState) }
    }

    if (showExportEndPicker) {
        val endPickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showExportEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = exportRangeStartMillis
                    val endMillis = endPickerState.selectedDateMillis
                    showExportEndPicker = false
                    if (startMillis != null && endMillis != null) {
                        // DatePickerState.selectedDateMillis is UTC midnight for the picked
                        // calendar date - recover that date, then build the actual filter range
                        // in the device's own zone so "end date" means through the end of that
                        // real day.
                        val zone = ZoneId.systemDefault()
                        val startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        val rangeStart = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
                        val rangeEndExclusive = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                        coroutineScope.launch {
                            val transactions = app.database.transactionDao().observeAll().first()
                                .filter { it.date in rangeStart until rangeEndExclusive }
                            exportAndShare(transactions)
                        }
                    }
                }) { Text("Export") }
            },
            dismissButton = {
                TextButton(onClick = { showExportEndPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = endPickerState) }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Clear all data and log out?") },
            text = {
                Text(
                    "This permanently erases every transaction, budget, subscription, bill, " +
                        "and rule stored on this device, signs you out of Family if you're " +
                        "signed in, and resets automatic SMS capture so it starts fresh from " +
                        "scratch next time you grant access. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData { onDataDeleted() }
                    showDeleteConfirm = false
                }) { Text("Clear everything", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PersonalInfoDialog(
    displayName: String,
    hasPhoto: Boolean,
    onNameChange: (String) -> Unit,
    onPhotoPicked: (android.net.Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(displayName) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onPhotoPicked(it) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personal Information") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it; onNameChange(it) },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.width(8.dp))
                Row {
                    TextButton(onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text(if (hasPhoto) "Change photo" else "Add photo") }
                    if (hasPhoto) {
                        TextButton(onClick = onRemovePhoto) { Text("Remove photo") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

/** Editable UPI ID (2026-08, real user request) - previously the only way to set it was a
 * one-time blocking gate inside SmartSplitCreateScreen, with no way to change it afterward.
 * Reads/writes the same Firestore users/{uid} doc Smart Split already uses, so a change here is
 * picked up there automatically, live - no separate sync needed. Smart Split's identity is
 * anonymous Firebase Auth (see SmartSplitsScreen's kdoc), so this dialog can't assume a user is
 * already signed in just because they came from Profile rather than Smart Split - it signs in
 * anonymously first if needed, mirroring SmartSplitsScreen.kt's own sign-in-on-open pattern. */
@Composable
private fun PaymentSettingsDialog(onDismiss: () -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val payRepository = remember { SplitPayRepository() }
    val coroutineScope = rememberCoroutineScope()

    var uid by remember { mutableStateOf(authRepository.currentUser?.uid ?: "") }
    var signingIn by remember { mutableStateOf(authRepository.currentUser == null) }

    LaunchedEffect(Unit) {
        if (authRepository.currentUser == null) {
            authRepository.signInAnonymously()
            uid = authRepository.currentUser?.uid ?: ""
        }
        signingIn = false
    }

    val payProfile by remember(uid) { payRepository.observePayProfile(uid) }.collectAsState(initial = null)
    var upiIdInput by remember(payProfile) { mutableStateOf(payProfile?.upiId ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Settings") },
        text = {
            Column {
                Text(
                    "Your UPI ID is what gets shared with people you split expenses with, so " +
                        "they can pay you back via Smart Split.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                if (signingIn) {
                    Text("Setting up…", style = MaterialTheme.typography.bodySmall)
                } else {
                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it; error = null },
                        label = { Text("UPI ID (e.g. yourname@okhdfcbank)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !signingIn && !saving,
                onClick = {
                    if (!UpiPay.looksLikeValidVpa(upiIdInput)) {
                        error = "That doesn't look like a valid UPI ID"
                        return@TextButton
                    }
                    saving = true
                    coroutineScope.launch {
                        val result = payRepository.upsertPayProfile(
                            UserPayProfile(
                                uid = uid,
                                displayName = payProfile?.displayName ?: "",
                                phoneNumber = payProfile?.phoneNumber,
                                upiId = upiIdInput.trim()
                            )
                        )
                        saving = false
                        when (result) {
                            is SplitPayResult.Success -> onDismiss()
                            is SplitPayResult.Failure -> error = result.message
                        }
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
