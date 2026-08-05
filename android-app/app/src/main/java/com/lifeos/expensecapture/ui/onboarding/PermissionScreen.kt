package com.lifeos.expensecapture.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.assistant.AiTextPolisher
import com.lifeos.expensecapture.data.db.entity.ConsentEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.notifications.NotificationCheckWorker
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class OnboardingStep { WELCOME, SMS_PERMISSION, SCANNING, NOTIFICATION_PERMISSION, FIRST_VALUE }

const val CONSENT_SMS = "SMS"
const val CONSENT_NOTIFICATIONS = "NOTIFICATIONS"

/**
 * AI Transformation Plan P1 (personalized first-scan summary): the completion screen used to say
 * "you're all set" regardless of what the scan actually found - the single most fragile retention
 * moment in the app got the least personalized message anywhere in it. Built entirely from
 * numbers the repositories already compute; no model needed.
 */
private data class ScanSummary(
    val transactionCount: Int,
    val recurringCount: Int,
    val topCategoryName: String?,
    val topCategoryAmount: Double
)

private suspend fun buildScanSummary(app: App): ScanSummary {
    val transactions = app.database.transactionDao().getSince(0L)
    val categories = app.database.categoryDao().observeAll().first()
    val topEntry = transactions
        .filter { it.direction == TransactionDirection.DEBIT }
        .groupBy { it.categoryId }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .entries
        .maxByOrNull { it.value }

    val recurringCount = app.database.subscriptionDao().observeAll().first().size +
        app.database.billDao().observeAll().first().size

    return ScanSummary(
        transactionCount = transactions.size,
        recurringCount = recurringCount,
        topCategoryName = topEntry?.let { entry ->
            categories.firstOrNull { it.id == entry.key }?.name ?: "Uncategorized"
        },
        topCategoryAmount = topEntry?.value ?: 0.0
    )
}

private fun firstValueMessage(hasSmsPermission: Boolean, summary: ScanSummary?): String {
    if (!hasSmsPermission) {
        return "You can add expenses manually any time from the + button, and turn on " +
            "automatic capture later from Settings."
    }
    if (summary == null || summary.transactionCount == 0) {
        // Honest, non-deflating framing for a genuinely quiet scan - never a hollow
        // "you're all set" when nothing was actually found yet.
        return "Found 0 so far - more will show up automatically as new messages arrive, " +
            "and anything already on your phone keeps getting picked up in the background."
    }
    return buildString {
        append("Found ${summary.transactionCount} transaction${if (summary.transactionCount == 1) "" else "s"} so far.")
        if (summary.topCategoryName != null) {
            append(" Your biggest category so far: ${summary.topCategoryName} (₹${"%.2f".format(summary.topCategoryAmount)}).")
        }
        if (summary.recurringCount > 0) {
            append(
                " ${summary.recurringCount} look${if (summary.recurringCount == 1) "s" else ""} like " +
                    "a recurring bill or subscription - review ${if (summary.recurringCount == 1) "it" else "them"} anytime from Finance."
            )
        }
    }
}

/**
 * Onboarding PRD, Phase 3 Doc 40 + Permissions & Consent PRD, Phase 3 Doc 41. Single-pillar
 * scope (only Finance exists), so "sign-up" is not applicable - there's no backend/account
 * concept in this local-only app, per Doc 40's own out-of-scope note that account/profile
 * management is a sibling PRD's concern once accounts exist. What this DOES implement, per
 * Doc 40's explicit requirements: a value-prop screen before any permission ask (user story
 * #1), a "Skip for now" escape hatch so denial never dead-ends (user story #5 - the previous
 * version of this screen had no path forward after a denial, a real gap fixed here), and a
 * first-value moment showing a genuine result before landing on Home. Every consent decision
 * is recorded via ConsentEntity per Doc 41's consent-record requirement.
 */
@Composable
fun PermissionScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as App
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var scanSummary by remember { mutableStateOf<ScanSummary?>(null) }

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED
    }

    suspend fun finishOnboarding() {
        // Recurring detection needs to have run before the summary is built, or "N look like a
        // recurring bill/subscription" would always read 0 on a first-ever scan.
        val insights = FinanceInsightsRepository(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao(),
            budgetDao = app.database.budgetDao(),
            subscriptionDao = app.database.subscriptionDao(),
            billDao = app.database.billDao()
        )
        insights.refreshRecurringDetection()
        scanSummary = buildScanSummary(app)
        step = OnboardingStep.FIRST_VALUE
    }

    suspend fun proceedToNotificationStep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            step = OnboardingStep.NOTIFICATION_PERMISSION
        } else {
            // No runtime notification permission needed pre-Android 13 - the worker still
            // needs scheduling so the in-app Notification Center stays populated.
            NotificationCheckWorker.schedulePeriodic(context)
            finishOnboarding()
        }
    }

    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        scope.launch {
            app.database.consentDao().upsert(ConsentEntity(CONSENT_SMS, granted))
        }
        if (granted) {
            step = OnboardingStep.SCANNING
            scope.launch {
                SmsHistoryScanner.scanIfNeeded(context)
                proceedToNotificationStep()
            }
        } else {
            scope.launch { proceedToNotificationStep() }
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            app.database.consentDao().upsert(ConsentEntity(CONSENT_NOTIFICATIONS, granted))
            NotificationCheckWorker.schedulePeriodic(context)
            finishOnboarding()
        }
    }

    LaunchedEffect(Unit) {
        if (hasSmsPermission()) {
            step = OnboardingStep.SCANNING
            SmsHistoryScanner.scanIfNeeded(context)
            proceedToNotificationStep()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            OnboardingStep.WELCOME -> {
                Text("Welcome", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text(
                    "This app automatically captures your expenses from bank and UPI SMS, " +
                        "tracks budgets, subscriptions, and bills - without you typing anything " +
                        "in. Next, we'll explain exactly what access that needs and why, before " +
                        "asking for anything.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { step = OnboardingStep.SMS_PERMISSION }) { Text("Continue") }
            }

            OnboardingStep.SMS_PERMISSION -> {
                Text("Automatic expense capture", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text(
                    "This app reads bank and UPI transaction SMS on your phone to log expenses " +
                        "automatically. Only the amount, merchant, and date are ever stored or sent " +
                        "anywhere - the original message text never leaves your device. Other SMS " +
                        "(personal messages, OTPs) are ignored and never stored.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    smsLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                }) { Text("Grant SMS access") }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    scope.launch {
                        app.database.consentDao().upsert(ConsentEntity(CONSENT_SMS, false))
                        proceedToNotificationStep()
                    }
                }) { Text("Skip for now - I'll add expenses manually") }
            }

            OnboardingStep.SCANNING -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Scanning your existing messages for past transactions…")
            }

            OnboardingStep.NOTIFICATION_PERMISSION -> {
                Text("Stay on top of bills & budgets", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Text(
                    "We can notify you when a bill is due, a subscription is about to renew, " +
                        "or a budget goes over - only for things you're already tracking, nothing else.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Allow notifications")
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    scope.launch {
                        app.database.consentDao().upsert(ConsentEntity(CONSENT_NOTIFICATIONS, false))
                        NotificationCheckWorker.schedulePeriodic(context)
                        finishOnboarding()
                    }
                }) { Text("Not now") }
            }

            OnboardingStep.FIRST_VALUE -> {
                Text("You're all set", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                // AI-polished phrasing (2026-08) - firstValueMessage's deterministic sentence
                // stays the source of truth; the AI call only warms up the phrasing, with the plain
                // sentence as the immediate/fallback value. See AiTextPolisher's kdoc.
                val factual = remember(scanSummary) { firstValueMessage(hasSmsPermission(), scanSummary) }
                val polished by produceState(initialValue = factual, factual) {
                    value = AiTextPolisher.polish(factual)
                }
                Text(polished, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGranted) { Text("Go to my Finance home") }
            }
        }
    }
}
