package com.lifeos.expensecapture.splitpay.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayResult
import com.lifeos.expensecapture.splitpay.model.ParticipantStatus
import com.lifeos.expensecapture.splitpay.model.SmartSplit
import com.lifeos.expensecapture.splitpay.model.SmartSplitParticipant
import com.lifeos.expensecapture.splitpay.model.UserPayProfile
import com.lifeos.expensecapture.util.rememberContactPhonePicker
import kotlinx.coroutines.launch

/** Strips spaces, dashes, and a leading +91/91 so "9876543210", "+91 98765 43210", and
 * "91-98765-43210" all match the same stored value - the one normalization rule this feature
 * applies before saving or looking up a phone number. */
fun normalizePhoneNumber(raw: String): String {
    val digitsOnly = raw.filter { it.isDigit() }
    return when {
        digitsOnly.length == 12 && digitsOnly.startsWith("91") -> digitsOnly.substring(2)
        digitsOnly.length == 10 -> digitsOnly
        else -> digitsOnly
    }
}

private class ParticipantEntry(name: String = "", phone: String = "", share: String = "") {
    var name by mutableStateOf(name)
    var phone by mutableStateOf(phone)
    var share by mutableStateOf(share)
    var matchedUserId by mutableStateOf<String?>(null)
    var checked by mutableStateOf(false)
}

/**
 * Smart Split's creation flow (2026-08) - "Phase 1" of the feature request: total, who paid
 * (always the signed-in user for this pass - splitting on someone else's behalf is a fast-follow,
 * not in the original request), and each participant either matched to a real app user by phone
 * (Track A) or left as an external contact (Track B). Gated behind a one-time "set your UPI ID"
 * prompt - every settle-up on the other end needs it, so it can't be skipped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSplitCreateScreen(onBack: () -> Unit, onCreated: (String) -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }
    val uid = authRepository.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    val payProfile by repository.observePayProfile(uid).collectAsState(initial = null)
    // Not authRepository.currentUser?.displayName - Smart Split's identity is anonymous auth
    // (see SmartSplitsScreen's kdoc), which never populates that field. The name lives in this
    // Firestore profile instead (set once during SmartSplitProfileSetupScreen).
    val displayName = payProfile?.displayName ?: ""
    var upiIdInput by remember { mutableStateOf("") }
    var savingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(payProfile) {
        payProfile?.upiId?.let { if (upiIdInput.isBlank()) upiIdInput = it }
    }

    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf(ParticipantEntry(), ParticipantEntry()) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Split") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (payProfile?.upiId.isNullOrBlank()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("Set your UPI ID first", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This is what gets pre-filled into everyone else's payment app when they settle up with you - " +
                        "it's never shown publicly, only used inside payment links.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    label = { Text("Your UPI ID (e.g. yourname@okhdfcbank)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (!UpiPay.looksLikeValidVpa(upiIdInput)) {
                            error = "That doesn't look like a valid UPI ID"
                            return@Button
                        }
                        savingProfile = true
                        coroutineScope.launch {
                            repository.upsertPayProfile(
                                UserPayProfile(uid = uid, displayName = displayName, phoneNumber = payProfile?.phoneNumber, upiId = upiIdInput.trim())
                            )
                            savingProfile = false
                        }
                    },
                    enabled = !savingProfile,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save UPI ID") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("What was it? (e.g. Dinner)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Total amount you paid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Split with", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = {
                            val total = amountText.toDoubleOrNull()
                            val count = participants.size + 1 // + the payer themselves
                            if (total != null && count > 0) {
                                val each = total / count
                                participants.forEach { it.share = "%.2f".format(each) }
                            }
                        }) { Text("Split equally (incl. you)") }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(participants.size) { index ->
                        val entry = participants[index]
                        val pickContact = rememberContactPhonePicker { pickedName, pickedNumber ->
                            entry.phone = normalizePhoneNumber(pickedNumber)
                            if (entry.name.isBlank()) entry.name = pickedName
                            entry.checked = false
                            entry.matchedUserId = null
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = entry.name,
                                    onValueChange = { entry.name = it },
                                    label = { Text("Name") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = entry.share,
                                    onValueChange = { entry.share = it },
                                    label = { Text("Share") },
                                    modifier = Modifier.weight(0.7f)
                                )
                                IconButton(onClick = { participants.removeAt(index) }, enabled = participants.size > 1) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove person")
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = entry.phone,
                                    onValueChange = { entry.phone = it; entry.checked = false; entry.matchedUserId = null },
                                    label = { Text("WhatsApp/phone number") },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = pickContact) {
                                    Icon(Icons.Filled.ContactPhone, contentDescription = "Pick from contacts")
                                }
                                Spacer(Modifier.width(8.dp))
                                if (entry.checked && entry.matchedUserId != null) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Has the app", tint = MaterialTheme.colorScheme.primary)
                                    Text("Has the app", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                                } else {
                                    TextButton(onClick = {
                                        val normalized = normalizePhoneNumber(entry.phone)
                                        if (normalized.isNotBlank()) {
                                            coroutineScope.launch {
                                                val match = repository.findUserByPhone(normalized)
                                                entry.matchedUserId = match?.uid
                                                entry.checked = true
                                            }
                                        }
                                    }) { Text("Check") }
                                }
                            }
                        }
                    }
                    item {
                        OutlinedButton(onClick = { participants.add(ParticipantEntry()) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add person")
                        }
                    }
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }

                Button(
                    onClick = {
                        val total = amountText.toDoubleOrNull()
                        val valid = participants.filter { it.name.isNotBlank() && it.phone.isNotBlank() }
                            .mapNotNull { entry -> entry.share.toDoubleOrNull()?.let { entry to it } }
                        if (description.isBlank() || total == null || total <= 0 || valid.isEmpty()) {
                            error = "Fill in the amount, description, and at least one person's share"
                            return@Button
                        }
                        coroutineScope.launch {
                            val split = SmartSplit(
                                description = description.trim(),
                                totalAmount = total,
                                payerId = uid,
                                payerName = displayName,
                                payerUpiId = payProfile?.upiId ?: "",
                                date = System.currentTimeMillis(),
                                createdAt = System.currentTimeMillis()
                            )
                            val participantModels = valid.map { (entry, share) ->
                                SmartSplitParticipant(
                                    name = entry.name.trim(),
                                    phoneNumber = normalizePhoneNumber(entry.phone),
                                    participantUserId = entry.matchedUserId,
                                    isExternal = entry.matchedUserId == null,
                                    shareAmount = share,
                                    status = ParticipantStatus.PENDING
                                )
                            }
                            when (val result = repository.createSplit(split, participantModels)) {
                                is SplitPayResult.Success -> onCreated(result.value)
                                is SplitPayResult.Failure -> error = result.message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Create & Notify") }
            }
        }
    }
}
