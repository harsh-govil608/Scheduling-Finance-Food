package com.lifeos.expensecapture.family.ui.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.family.data.HealthRecordRepository
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.HealthRecord
import com.lifeos.expensecapture.family.model.PermissionType
import com.lifeos.expensecapture.family.ui.common.PermissionGate
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

/** Shared Health Records module (2026-08 Family module) - the one shared module that's
 * PermissionType.HEALTH-gated per record's owning member (see PermissionGate's kdoc); each row
 * checks the record owner's own PermissionSet before rendering, same discipline location sharing
 * on the Dashboard already applies. Otherwise follows TasksModuleScreen's pattern. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthModuleScreen(familyId: String, onBack: () -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val familyRepository = remember { FamilyRepository() }
    val repository = remember(familyId) { HealthRecordRepository(familyId = familyId) }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val currentUserName = authRepository.currentUser?.displayName ?: ""
    val coroutineScope = rememberCoroutineScope()

    // remember()'d keyed on familyId (2026-08-15 fix) - see TasksModuleScreen.kt's identical fix
    // for why an inline observeX().collectAsState() recreates the Firestore listener on every
    // recomposition instead of reusing one.
    val records by remember(familyId) { repository.observeAll() }.collectAsState(initial = emptyList())
    val members by remember(familyId) { familyRepository.observeMembers(familyId) }.collectAsState(initial = emptyList())
    val currentMember = members.firstOrNull { it.userId == currentUserId }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Records") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add record") }
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No shared health records yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    val owner = members.firstOrNull { it.userId == record.memberUserId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            if (currentMember != null && owner != null) {
                                // Real privacy gap fixed 2026-08-15: the "For {member}" label used
                                // to render unconditionally, outside the gate - so a hidden
                                // record's owner (and the fact they have one at all) was disclosed
                                // even when its title/notes were correctly hidden. PermissionGate's
                                // own fallback ("Hidden by this member's privacy settings")
                                // deliberately never says whose - moving this inside keeps that
                                // guarantee instead of undermining it right below.
                                PermissionGate(viewer = currentMember, target = owner, type = PermissionType.HEALTH) {
                                    Text(record.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(record.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "For ${owner.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHealthRecordDialog(
            members = members,
            onConfirm = { title, notes, member ->
                coroutineScope.launch {
                    repository.add(
                        HealthRecord(
                            familyId = familyId,
                            memberUserId = member.userId,
                            title = title,
                            notes = notes,
                            recordDate = System.currentTimeMillis(),
                            createdBy = currentUserId,
                            createdAt = System.currentTimeMillis()
                        ),
                        currentUserName
                    )
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddHealthRecordDialog(
    members: List<FamilyMember>,
    onConfirm: (title: String, notes: String, member: FamilyMember) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add health record") },
        text = {
            Column {
                TextButton(onClick = { expanded = true }) { Text(selectedMember?.displayName ?: "Select member") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    members.forEach { member ->
                        DropdownMenuItem(text = { Text(member.displayName) }, onClick = { selectedMember = member; expanded = false })
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val member = selectedMember
                if (title.isNotBlank() && member != null) onConfirm(title.trim(), notes.trim(), member)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
