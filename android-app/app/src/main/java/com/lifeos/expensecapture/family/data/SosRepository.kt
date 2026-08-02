package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyEventType
import com.lifeos.expensecapture.family.model.FamilyNotification
import com.lifeos.expensecapture.family.model.FamilyNotificationType
import com.lifeos.expensecapture.family.model.GeoPoint
import com.lifeos.expensecapture.family.model.SOSAlert
import com.lifeos.expensecapture.family.model.SOSStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * SOS workflow (2026-08 Family module) - triggering SOS writes an SOSAlert doc (with the
 * member's captured location, see SosViewModel for where FusedLocationProviderClient is called)
 * and a FamilyNotification per OTHER family member, both real-time-visible via Firestore
 * listeners the instant they're written. That covers "emergency notifications" for any family
 * member who has the app open; a member who doesn't will only see it on next open, since actual
 * background push requires an FCM Cloud Function trigger (a Node/TS deploy, not client Kotlin) -
 * flagged as the fast-follow this client-only pass can't complete alone.
 */
class SosRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore),
    private val notificationRepository: FamilyNotificationRepository = FamilyNotificationRepository(firestore)
) {
    private fun sosCollection(familyId: String) = firestore.collection("families").document(familyId).collection("sosAlerts")

    suspend fun triggerSos(
        familyId: String,
        userId: String,
        userName: String,
        location: GeoPoint?,
        otherMemberIds: List<String>
    ): FamilyResult<String> {
        return try {
            val ref = sosCollection(familyId).document()
            val alert = SOSAlert(
                id = ref.id,
                familyId = familyId,
                triggeredByUserId = userId,
                triggeredByName = userName,
                triggeredAt = System.currentTimeMillis(),
                location = location,
                status = SOSStatus.ACTIVE
            )
            ref.set(alert).await()

            eventStream.log(
                FamilyEvent(
                    familyId = familyId,
                    type = FamilyEventType.SOS_TRIGGERED,
                    actorId = userId,
                    actorName = userName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf(
                        "latitude" to (location?.latitude?.toString() ?: "unknown"),
                        "longitude" to (location?.longitude?.toString() ?: "unknown")
                    )
                )
            )

            otherMemberIds.filter { it != userId }.forEach { memberId ->
                notificationRepository.add(
                    FamilyNotification(
                        familyId = familyId,
                        type = FamilyNotificationType.SOS,
                        title = "$userName needs help",
                        body = "SOS triggered - live location shared with the family",
                        relatedUserId = userId,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            FamilyResult.Success(ref.id)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't send SOS")
        }
    }

    suspend fun resolveSos(familyId: String, alertId: String, actorId: String, actorName: String): FamilyResult<Unit> {
        return try {
            sosCollection(familyId).document(alertId).update(
                mapOf("status" to SOSStatus.RESOLVED.name, "resolvedAt" to System.currentTimeMillis())
            ).await()
            eventStream.log(
                FamilyEvent(
                    familyId = familyId,
                    type = FamilyEventType.SOS_RESOLVED,
                    actorId = actorId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis()
                )
            )
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't resolve SOS")
        }
    }

    fun observeActiveAlerts(familyId: String): Flow<List<SOSAlert>> = callbackFlow {
        val registration = sosCollection(familyId)
            .whereEqualTo("status", SOSStatus.ACTIVE.name)
            .orderBy("triggeredAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(SOSAlert::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }
}
