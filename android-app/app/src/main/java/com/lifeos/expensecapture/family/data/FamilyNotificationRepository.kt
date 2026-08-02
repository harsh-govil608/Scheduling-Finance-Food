package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.family.model.FamilyNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Family notification center (2026-08 Family module) - reminders, arrivals/departures, medicine
 * alerts, bill-due alerts, and SOS, all one FamilyNotification collection per family
 * (families/{familyId}/notifications), mirroring this app's existing local NotificationDao/
 * NotificationCenterScreen pattern but cross-device. Real push delivery (so a member sees an
 * alert without the app already open) needs an FCM Cloud Function - this repository is the
 * client-visible-once-open half of that; see SosRepository's kdoc for the same caveat.
 */
class FamilyNotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun notificationsCollection(familyId: String) =
        firestore.collection("families").document(familyId).collection("notifications")

    suspend fun add(notification: FamilyNotification): FamilyResult<String> {
        return try {
            val ref = notificationsCollection(notification.familyId).document()
            ref.set(notification.copy(id = ref.id)).await()
            FamilyResult.Success(ref.id)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't create notification")
        }
    }

    fun observeAll(familyId: String): Flow<List<FamilyNotification>> = callbackFlow {
        val registration = notificationsCollection(familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(FamilyNotification::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun markRead(familyId: String, notificationId: String): FamilyResult<Unit> {
        return try {
            notificationsCollection(familyId).document(notificationId).update("read", true).await()
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't update notification")
        }
    }
}
