package com.lifeos.expensecapture.splitpay.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository
import com.lifeos.expensecapture.splitpay.data.SplitPayResult
import com.lifeos.expensecapture.splitpay.model.ParticipantStatus
import com.lifeos.expensecapture.splitpay.model.SmartSplit
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

/**
 * Smart Split landing surface (2026-08) - "splits I created" (owed to me) and "splits I owe"
 * (someone else's, where I was matched as an app-user participant, see
 * SplitPayRepository.observeSplitsIOwe), one screen. Sits alongside the original local-only
 * Split Expenses list (SplitExpensesScreen) rather than replacing it - reached from there.
 *
 * Bug fix (real user report, 2026-08 - tapping "Try Smart Split" or its + button while signed
 * out crashed the app): every downstream repository call here assumes a real signed-in uid -
 * SplitPayRepository.observePayProfile in particular calls Firestore's `.document(uid)`, which
 * throws immediately on an empty string.
 *
 * Identity (2026-08 revision, real user constraint - no budget for Firebase's Blaze plan yet):
 * anonymous Firebase Auth + a self-declared name/phone, NOT the Family module's phone+OTP sign
 * in. OTP requires Blaze (Google won't send SMS on the free Spark plan); Smart Split has none of
 * Family sharing's location/SOS features that actually need a verified number, so a free
 * anonymous uid plus a phone number the user just types in (matched against, not verified - see
 * SplitPayRepository.findUserByPhone) is enough. See FamilyAuthRepository.signInAnonymously's
 * kdoc for the trade-off this makes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSplitsScreen(onBack: () -> Unit, onCreate: () -> Unit, onOpenSplit: (String) -> Unit, onOpenHistory: () -> Unit) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }

    var signingIn by remember { mutableStateOf(authRepository.currentUser == null) }
    var signInError by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableStateOf(0) }
    LaunchedEffect(retryToken) {
        if (authRepository.currentUser == null) {
            val result = authRepository.signInAnonymously()
            signInError = if (result.success) null else result.errorMessage
        }
        signingIn = false
    }

    if (signingIn) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val uid = authRepository.currentUser?.uid ?: ""

    // Only reachable if signInAnonymously failed above - surfaces the real reason (e.g. the
    // Anonymous provider not being enabled yet in Firebase console) instead of silently leaving
    // uid blank, which used to make SmartSplitProfileSetupScreen's Continue button spin forever
    // with no feedback (syncPhoneAndName's blank-uid guard failed silently underneath it).
    if (uid.isBlank()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Couldn't set up Smart Split", style = MaterialTheme.typography.headlineSmall)
            Text(
                signInError ?: "Unknown error signing in",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            Button(onClick = { signingIn = true; signInError = null; retryToken++ }, modifier = Modifier.fillMaxWidth()) {
                Text("Retry")
            }
        }
        return
    }

    // remember()'d keyed on uid, not called inline - see this screen's kdoc addition on the
    // flicker bug this fixes: an inline `repository.observeX(uid).collectAsState(...)` call
    // creates a brand new Flow (and Firestore listener) on every recomposition, which resets
    // collectAsState back to its initial value each time and re-subscribes from scratch. Keying
    // on uid means the same Flow/listener survives recomposition and only restarts when uid
    // itself actually changes.
    val payProfile by remember(uid) { repository.observePayProfile(uid) }.collectAsState(initial = null)

    if (payProfile?.phoneNumber.isNullOrBlank()) {
        SmartSplitProfileSetupScreen { name, phone ->
            repository.syncPhoneAndName(uid, normalizePhoneNumber(phone), name)
        }
        return
    }

    val mySplits by remember(uid) { repository.observeMySplits(uid) }.collectAsState(initial = emptyList())
    val owedParticipations by remember(uid) { repository.observeSplitsIOwe(uid) }.collectAsState(initial = emptyList())
    val pendingIOwe = owedParticipations.filter { it.status == ParticipantStatus.PENDING || it.status == ParticipantStatus.CLAIMED_PAID }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Split") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = "Split history")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) { Icon(Icons.Default.Add, contentDescription = "New smart split") }
        }
    ) { padding ->
        if (mySplits.isEmpty() && pendingIOwe.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No smart splits yet. Create one to auto-notify who owes you and let them " +
                        "pay you back via UPI - even if they don't have this app.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pendingIOwe.isNotEmpty()) {
                    item { com.lifeos.expensecapture.ui.common.SectionLabel("You owe") }
                    items(pendingIOwe, key = { "owe-${it.id}" }) { participant ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenSplit(participant.splitId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("₹${"%.2f".format(participant.shareAmount)}", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (participant.status == ParticipantStatus.CLAIMED_PAID) "Waiting for confirmation" else "Tap to settle up",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (mySplits.isNotEmpty()) {
                    item { com.lifeos.expensecapture.ui.common.SectionLabel("Owed to you") }
                    items(mySplits, key = { it.id }) { split ->
                        SmartSplitRow(split, onClick = { onOpenSplit(split.id) })
                    }
                }
            }
        }
    }
}

/** No OTP - just a name and a phone number to match against, see this file's kdoc for why.
 * [onSave] is suspend and returns a result rather than being fire-and-forget, so a failure (e.g.
 * a Firestore security-rule rejection) un-sticks the button and shows why instead of spinning
 * forever - a real bug hit during testing before this was suspend/result-based. */
@Composable
private fun SmartSplitProfileSetupScreen(onSave: suspend (name: String, phone: String) -> SplitPayResult<Unit>) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set up Smart Split", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Just your name and number so people you split with can find you - no OTP, no " +
                "password, nothing to verify.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            prefix = { Text("+91 ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
        Button(
            onClick = {
                saving = true
                error = null
                scope.launch {
                    when (val result = onSave(name.trim(), phone.trim())) {
                        is SplitPayResult.Success -> {}
                        is SplitPayResult.Failure -> {
                            saving = false
                            error = result.message
                        }
                    }
                }
            },
            enabled = !saving && name.isNotBlank() && normalizePhoneNumber(phone).length == 10,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (saving) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Continue")
        }
    }
}

@Composable
private fun SmartSplitRow(split: SmartSplit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(split.description, style = MaterialTheme.typography.bodyLarge)
            Text("₹${"%.2f".format(split.totalAmount)} total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
