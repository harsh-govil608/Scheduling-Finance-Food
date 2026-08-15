package com.lifeos.expensecapture.family.ui.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.SharedCalendarRepository
import com.lifeos.expensecapture.family.model.SharedCalendarEvent
import com.lifeos.expensecapture.family.ui.FamilyPillar
import com.lifeos.expensecapture.family.ui.FamilyPillarBottomBar
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

/** Shared Calendar module (2026-08 Family module) - follows TasksModuleScreen's pattern; the
 * date/time picker is deliberately a plain "days from now" quick-pick rather than a full
 * DatePickerDialog+TimePickerDialog pair, to keep this pass's six modules uniform in effort. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarModuleScreen(familyId: String, onBackToFinance: () -> Unit, onSelectPillar: (FamilyPillar) -> Unit = {}) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember(familyId) { SharedCalendarRepository(familyId = familyId) }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val currentUserName = authRepository.currentUser?.displayName ?: ""
    val coroutineScope = rememberCoroutineScope()

    // remember()'d keyed on familyId (2026-08-15 fix) - see TasksModuleScreen.kt's identical fix
    // for why an inline observeX().collectAsState() recreates the Firestore listener on every
    // recomposition instead of reusing one.
    val events by remember(familyId) { repository.observeAll() }.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = { IconButton(onClick = onBackToFinance) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Finance") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add event") }
        },
        bottomBar = { FamilyPillarBottomBar(current = FamilyPillar.CALENDAR, onSelect = onSelectPillar) }
    ) { padding ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No shared events yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(event.title, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                dateFormat.format(Date(event.startAt)) + (event.location?.let { " • $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCalendarEventDialog(
            onConfirm = { title, daysFromNow, location ->
                coroutineScope.launch {
                    repository.add(
                        SharedCalendarEvent(
                            familyId = familyId,
                            title = title,
                            startAt = System.currentTimeMillis() + daysFromNow * 86_400_000L,
                            location = location.ifBlank { null },
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
private fun AddCalendarEventDialog(
    onConfirm: (title: String, daysFromNow: Int, location: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("0") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add event") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                // Real gap found via review, 2026-08-15: daysText.toIntOrNull() ?: 0 used to
                // silently treat garbage input ("abc") as "today" and accept negative numbers as
                // a past date with no validation - the user would get a different event than what
                // they thought they typed, with no error shown. Now a numeric keyboard plus real
                // validation before Add is even enabled.
                val daysValue = daysText.toIntOrNull()
                val daysInvalid = daysValue == null || daysValue < 0
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Days from today") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = daysText.isNotBlank() && daysInvalid,
                    supportingText = { if (daysText.isNotBlank() && daysInvalid) Text("Enter 0 or a positive number of days") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            val daysValue = daysText.toIntOrNull()
            TextButton(
                onClick = { onConfirm(title.trim(), daysValue ?: 0, location) },
                enabled = title.isNotBlank() && daysValue != null && daysValue >= 0
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
