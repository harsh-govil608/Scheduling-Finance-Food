package com.lifeos.expensecapture.family.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.FamilyResult
import com.lifeos.expensecapture.family.model.FamilyRole
import com.lifeos.expensecapture.family.model.Invitation
import com.lifeos.expensecapture.splitpay.ui.normalizePhoneNumber
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.util.rememberContactPhonePicker
import kotlinx.coroutines.launch

/**
 * Invite by phone number (2026-08 real user request: "person1 types in person2 and person3
 * phone numbers and hits send") - matches sign-in itself moving to phone + OTP (see
 * FamilyAuthRepository's kdoc). Generates the same Invitation doc/code as before (see
 * Invitation's kdoc); "Send Invite" shares that code via WhatsApp/SMS/any share target rather
 * than a real push notification - see this screen's own note on why (same FCM Cloud Function gap
 * flagged throughout this module).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyInviteScreen(
    familyId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val familyRepository = remember { FamilyRepository() }
    val authRepository = remember { FamilyAuthRepository() }
    val family by familyRepository.observeFamily(familyId).collectAsState(initial = null)
    val familyName = family?.name ?: "your family"

    var phone by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var proposedRole by remember { mutableStateOf(FamilyRole.ADULT) }
    var latestInvitation by remember { mutableStateOf<Invitation?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickContact = rememberContactPhonePicker { _, pickedNumber -> phone = normalizePhoneNumber(pickedNumber) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite to $familyName") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Role for this invite", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            TextButton(onClick = { roleExpanded = true }) {
                                Text(proposedRole.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                            DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                FamilyRole.entries.filter { it != FamilyRole.OWNER }.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = { proposedRole = role; roleExpanded = false }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = normalizePhoneNumber(it) },
                                label = { Text("Phone number") },
                                prefix = { Text("+91 ") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = pickContact) {
                                Icon(Icons.Filled.ContactPhone, contentDescription = "Pick from contacts")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val userId = authRepository.currentUser?.uid ?: return@Button
                                if (phone.length != 10) {
                                    error = "Enter a valid 10-digit phone number"
                                    return@Button
                                }
                                coroutineScope.launch {
                                    when (val result = familyRepository.createInvitation(familyId, phone, proposedRole, userId)) {
                                        is FamilyResult.Success -> {
                                            latestInvitation = result.value
                                            error = null
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Join our family \"$familyName\" on Expense Capture. " +
                                                        "Open the app, sign in, choose \"Join with an invite code\", and enter: ${result.value.code}"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Send invite"))
                                        }
                                        is FamilyResult.Failure -> error = result.message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Send Invite")
                        }
                    }
                }
            }

            error?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            latestInvitation?.let { invitation ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Invite code", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invitation.code, style = MaterialTheme.typography.headlineSmall)
                            }
                            IconButton(onClick = { clipboard.setText(AnnotatedString(invitation.code)) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy code")
                            }
                        }
                    }
                }
            }
        }
    }
}
