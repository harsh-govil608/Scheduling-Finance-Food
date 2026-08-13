package com.lifeos.expensecapture.splitpay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.notifications.NotificationSender
import com.lifeos.expensecapture.splitpay.data.SplitPayRepository

/**
 * Auto-notify in-app Smart Split participants (2026-08, real user request: "within the app auto
 * notify them for payment those who use this app" - the Create screen's "Create & Notify" button
 * previously didn't actually notify anyone). Mounted once at the app level (see PilotApp.kt) so
 * it fires regardless of which screen the user is on, rather than only when Smart Split itself
 * happens to be open.
 *
 * Honesty constraint: this is app-open-required, not true push - there's no FCM/Cloud Functions
 * infrastructure anywhere in this repo (same documented limitation as FamilyNotificationRepository).
 * It fires the moment a live Firestore listener (SplitPayRepository.observeSplitsIOwe) sees a new
 * participant row while the app is foreground or backgrounded-but-alive - never while the process
 * is fully dead. External (Track B, participantUserId == null) participants never appear in
 * observeSplitsIOwe's results, so they're naturally excluded here and keep getting the existing
 * manual "Share payment link" flow in SmartSplitDetailScreen untouched.
 */
@Composable
fun SmartSplitNotificationWatcher(app: App) {
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember { SplitPayRepository() }
    val currentUser by authRepository.observeAuthState().collectAsState(initial = authRepository.currentUser)

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        repository.observeSplitsIOwe(uid).collect { participants ->
            participants.forEach { participant ->
                val sourceKey = "smart_split_participant_${participant.id}"
                if (!app.database.notificationDao().existsBySourceKey(sourceKey)) {
                    val split = repository.getSplitOnce(participant.splitId)
                    NotificationSender.notify(
                        context = app,
                        type = NotificationType.SMART_SPLIT_ADDED,
                        title = "New split: ${split?.description?.ifBlank { "Expense" } ?: "Expense"}",
                        body = "${split?.payerName?.ifBlank { "Someone" } ?: "Someone"} added you - " +
                            "you owe ₹${"%.2f".format(participant.shareAmount)}",
                        route = "smart_split_detail/${participant.splitId}",
                        cooldownKey = sourceKey
                    )
                }
            }
        }
    }
}
