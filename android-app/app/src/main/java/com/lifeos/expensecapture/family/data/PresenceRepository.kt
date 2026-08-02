package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.lifeos.expensecapture.family.model.GeoPoint
import com.lifeos.expensecapture.family.model.MemberPresence
import com.lifeos.expensecapture.family.model.PermissionType
import com.lifeos.expensecapture.family.model.PresenceStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Real-time member presence + optional location (2026-08 Family module). "Real-time" here means
 * a Firestore listener, the same mechanism every write in this module goes through - there is no
 * separate websocket/polling layer. Presence updates are a heartbeat write from the client (see
 * [markOnline]/[markOffline]) rather than true server-side connect/disconnect detection (that
 * would need Firebase Realtime Database's onDisconnect, a second database this module doesn't
 * otherwise need) - acceptable staleness for a family app, not for a trading floor.
 *
 * Location is written only when a member has actively started sharing (see [updateLocation]) -
 * never polled/read in the background without that explicit per-session opt-in, on top of the
 * standing PermissionSet.locationVisible check every reader applies before displaying it.
 */
class PresenceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun presenceCollection(familyId: String) =
        firestore.collection("families").document(familyId).collection("presence")

    suspend fun markOnline(familyId: String, userId: String) {
        presenceCollection(familyId).document(userId).set(
            mapOf(
                "userId" to userId,
                "status" to PresenceStatus.ONLINE.name,
                "lastSeenAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun markOffline(familyId: String, userId: String) {
        presenceCollection(familyId).document(userId).set(
            mapOf(
                "userId" to userId,
                "status" to PresenceStatus.OFFLINE.name,
                "lastSeenAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    /** Only called while the member's own active location-sharing session is running - see
     * SosViewModel/PresenceViewModel's kdoc for where that session lifecycle lives. Withholding
     * this from OTHER members based on PermissionSet is the reader's job (PermissionGate), not
     * this write path's - a member always has the right to see/write their own location. */
    suspend fun updateLocation(familyId: String, userId: String, location: GeoPoint) {
        presenceCollection(familyId).document(userId).set(
            mapOf(
                "userId" to userId,
                "lastLocation" to mapOf("latitude" to location.latitude, "longitude" to location.longitude),
                "lastLocationAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    fun observePresence(familyId: String): Flow<List<MemberPresence>> = callbackFlow {
        val registration = presenceCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val presence = snapshot?.documents?.mapNotNull { it.toObject(MemberPresence::class.java) } ?: emptyList()
            trySend(presence)
        }
        awaitClose { registration.remove() }
    }

    /** [PermissionType.LOCATION]-gated view for UI callers - strips location fields for any
     * member whose own PermissionSet doesn't allow sharing it, so a screen can't accidentally
     * render location it never should have rendered by skipping the check once and reusing the
     * raw list. */
    fun observeVisiblePresence(
        familyId: String,
        permissionsByUserId: Map<String, Boolean>
    ): Flow<List<MemberPresence>> = observePresence(familyId).map { presenceList ->
        presenceList.map { presence ->
            if (permissionsByUserId[presence.userId] == true) presence
            else presence.copy(lastLocation = null, lastLocationAt = 0L)
        }
    }
}
