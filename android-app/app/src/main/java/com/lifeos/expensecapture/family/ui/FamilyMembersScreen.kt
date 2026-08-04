package com.lifeos.expensecapture.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.FamilyRole
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

/**
 * Role + permission management (2026-08 Family module PRD: "role-based access" and "permission
 * system controlling visibility of location, documents, health data, and expenses"). Only Owner/
 * Parent can change another member's role or permissions - see hasManagementRights below; every
 * member can always see this list (who's in the family, what role they hold) even without
 * management rights, they just can't edit anyone else's settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(familyId: String, onBack: () -> Unit) {
    val familyRepository = remember { FamilyRepository() }
    val authRepository = remember { FamilyAuthRepository() }
    val currentUserId = authRepository.currentUser?.uid
    val members by familyRepository.observeMembers(familyId).collectAsState(initial = emptyList())
    val currentMember = members.firstOrNull { it.userId == currentUserId }
    val hasManagementRights = currentMember?.role == FamilyRole.OWNER || currentMember?.role == FamilyRole.PARENT

    var editingMember by remember { mutableStateOf<FamilyMember?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Owner-only, not "hasManagementRights" - a Parent can manage other members
                    // but shouldn't be able to delete the whole family (see FamilyRole's kdoc:
                    // "Owner ... can never be removed except by deleting the family outright").
                    if (currentMember?.role == FamilyRole.OWNER) {
                        IconButton(onClick = { showDeleteConfirm = true }, enabled = !isDeleting) {
                            Icon(
                                Icons.Filled.DeleteForever,
                                contentDescription = "Delete family",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            items(members, key = { it.userId }) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.displayName.ifBlank { "Member" }, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (hasManagementRights && member.role != FamilyRole.OWNER) {
                            TextButton(onClick = { editingMember = member }) { Text("Manage") }
                        }
                    }
                }
            }
        }
    }

    editingMember?.let { member ->
        ManageMemberDialog(
            familyId = familyId,
            member = member,
            actorId = currentUserId ?: "",
            actorName = currentMember?.displayName ?: "",
            onDismiss = { editingMember = null }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirm = false },
            title = { Text("Delete this family?") },
            text = {
                Text(
                    "This permanently removes the family for every member - all shared tasks, " +
                        "calendar events, expenses, documents, health records, and emergency " +
                        "contacts. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        coroutineScope.launch {
                            familyRepository.deleteFamily(familyId)
                            isDeleting = false
                            showDeleteConfirm = false
                            onBack()
                        }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }, enabled = !isDeleting) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ManageMemberDialog(
    familyId: String,
    member: FamilyMember,
    actorId: String,
    actorName: String,
    onDismiss: () -> Unit
) {
    val familyRepository = remember { FamilyRepository() }
    val coroutineScope = rememberCoroutineScope()
    var role by remember { mutableStateOf(member.role) }
    var permissions by remember { mutableStateOf(member.permissions) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(member.displayName.ifBlank { "Member" }) },
        text = {
            Column {
                Text("Role", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { roleExpanded = true }) {
                    Text(role.name.lowercase().replaceFirstChar { it.uppercase() })
                }
                DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                    FamilyRole.entries.filter { it != FamilyRole.OWNER }.forEach { candidate ->
                        DropdownMenuItem(
                            text = { Text(candidate.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { role = candidate; roleExpanded = false }
                        )
                    }
                }

                Text("Visible to other members", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp))
                PermissionToggleRow("Location", permissions.locationVisible) { permissions = permissions.copy(locationVisible = it) }
                PermissionToggleRow("Documents", permissions.documentsVisible) { permissions = permissions.copy(documentsVisible = it) }
                PermissionToggleRow("Health data", permissions.healthVisible) { permissions = permissions.copy(healthVisible = it) }
                PermissionToggleRow("Expenses", permissions.expensesVisible) { permissions = permissions.copy(expensesVisible = it) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                coroutineScope.launch {
                    if (role != member.role) {
                        familyRepository.updateMemberRole(familyId, member.userId, role, actorId, actorName)
                    }
                    if (permissions != member.permissions) {
                        familyRepository.updateMemberPermissions(familyId, member.userId, permissions, actorId, actorName)
                    }
                }
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PermissionToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
