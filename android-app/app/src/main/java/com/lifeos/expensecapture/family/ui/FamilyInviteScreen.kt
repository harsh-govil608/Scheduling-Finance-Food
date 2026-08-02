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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.material3.OutlinedButton
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
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

/**
 * Invite via email or share link (2026-08 Family module PRD) - both resolve to the same
 * Invitation doc/code (see Invitation's kdoc); this screen just offers two different ways to get
 * that code in front of the invitee. "Email" launches the device's own mail app with a prefilled
 * subject/body rather than sending mail directly - real server-sent email needs a Cloud Function
 * (SMTP/SendGrid etc., a Node/TS deploy), out of reach for client-only Kotlin, flagged as a
 * fast-follow rather than silently faked.
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

    var email by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }
    var proposedRole by remember { mutableStateOf(FamilyRole.ADULT) }
    var latestInvitation by remember { mutableStateOf<Invitation?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun createAndAct(after: (Invitation) -> Unit) {
        val userId = authRepository.currentUser?.uid ?: return
        coroutineScope.launch {
            when (val result = familyRepository.createInvitation(familyId, email.trim().ifBlank { null }, proposedRole, userId)) {
                is FamilyResult.Success -> {
                    latestInvitation = result.value
                    error = null
                    after(result.value)
                }
                is FamilyResult.Failure -> error = result.message
            }
        }
    }

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
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email (optional - just a label on the invite)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                createAndAct { invitation ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join our family \"$familyName\" on Expense Capture. " +
                                                "Open the app, choose \"Join with an invite code\", and enter: ${invitation.code}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share invite"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier)
                            Spacer(Modifier.width(8.dp))
                            Text("Share invite link")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                createAndAct { invitation ->
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("mailto:")
                                        if (email.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
                                        putExtra(Intent.EXTRA_SUBJECT, "You're invited to join \"$familyName\"")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Open Expense Capture, choose \"Join with an invite code\", and enter: ${invitation.code}"
                                        )
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (e: android.content.ActivityNotFoundException) {
                                        // No email app installed - the code is still shown below to copy manually.
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Invite via email")
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
