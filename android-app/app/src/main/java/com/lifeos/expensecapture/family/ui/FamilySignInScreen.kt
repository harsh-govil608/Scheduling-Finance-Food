package com.lifeos.expensecapture.family.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.OtpSendResult
import com.lifeos.expensecapture.splitpay.ui.normalizePhoneNumber
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

private enum class SignInStep { PHONE, OTP, NAME }

/**
 * Family module's identity gate (2026-08) - see FamilyAuthRepository's kdoc for why this is
 * phone number + OTP, and for why it exists only for this module. Not shown at app launch; only
 * reached when the founder (or a family member) opens the new Family pillar - everything else in
 * the app still needs no account at all.
 */
@Composable
fun FamilySignInScreen(viewModel: FamilyAppViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authRepository = remember { FamilyAuthRepository() }
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableStateOf(SignInStep.PHONE) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun sendOtp() {
        val normalized = normalizePhoneNumber(phone)
        val currentActivity = activity
        if (normalized.length != 10 || currentActivity == null) {
            error = "Enter a valid 10-digit phone number"
            return
        }
        sending = true
        error = null
        authRepository.sendOtp("+91$normalized", currentActivity) { result ->
            sending = false
            when (result) {
                is OtpSendResult.CodeSent -> {
                    verificationId = result.verificationId
                    step = SignInStep.OTP
                }
                is OtpSendResult.AutoVerified -> {
                    coroutineScope.launch {
                        authRepository.signInWithCredential(result.credential)
                        if (viewModel.needsDisplayName()) step = SignInStep.NAME
                    }
                }
                is OtpSendResult.Failed -> error = result.message
            }
        }
    }

    fun verifyOtp() {
        val id = verificationId ?: return
        if (otp.length != 6) {
            error = "Enter the 6-digit code"
            return
        }
        sending = true
        error = null
        coroutineScope.launch {
            val result = authRepository.verifyOtp(id, otp)
            sending = false
            if (result.success) {
                // Once signed in, FamilyAppViewModel's auth-state Flow flips isSignedIn=true and
                // FamilyEntryScreen swaps this whole composable out - the NAME branch is the one
                // case that still needs a step of its own before that handoff.
                if (viewModel.needsDisplayName()) step = SignInStep.NAME
            } else {
                error = result.errorMessage
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Welcome to", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
            "Family Hub",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            "Manage expenses, share moments and stay connected as a family.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 28.dp)
        )

        when (step) {
            SignInStep.PHONE -> {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number") },
                    prefix = { Text("+91 ") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
                Button(
                    onClick = { sendOtp() },
                    enabled = !sending,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Send OTP")
                }
                Text(
                    "By continuing, you agree to our Terms & Conditions and Privacy Policy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
                )
                Spacer(Modifier.height(24.dp))
                WhyFamiliesLoveItCard()
            }
            SignInStep.OTP -> {
                Text("Code sent to +91 ${normalizePhoneNumber(phone)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("6-digit code") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
                Button(
                    onClick = { verifyOtp() },
                    enabled = !sending,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Verify")
                }
                TextButton(onClick = { step = SignInStep.PHONE; otp = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Change number / resend")
                }
            }
            SignInStep.NAME -> {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Button(
                    onClick = { if (name.isNotBlank()) viewModel.setDisplayName(name.trim()) },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Continue") }
            }
        }
    }
}

/** Reference mockup's "Why Families Love It" card (2026-08, `ui3/`) - plain marketing copy about
 * real shipped capabilities (expense tracking, shared tasks/reminders via Tasks+Calendar, and
 * Firebase Auth-backed sign-in), not a features list promising anything unbuilt. */
@Composable
private fun WhyFamiliesLoveItCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Why Families Love It", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            WhyRow(Icons.Filled.TrendingUp, "Real-time expense tracking")
            Spacer(Modifier.height(10.dp))
            WhyRow(Icons.Filled.Notifications, "Shared tasks & reminders")
            Spacer(Modifier.height(10.dp))
            WhyRow(Icons.Filled.Lock, "Secure & private")
        }
    }
}

@Composable
private fun WhyRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
