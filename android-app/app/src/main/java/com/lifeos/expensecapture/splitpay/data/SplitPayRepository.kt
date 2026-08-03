package com.lifeos.expensecapture.splitpay.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.splitpay.model.ParticipantStatus
import com.lifeos.expensecapture.splitpay.model.SmartSplit
import com.lifeos.expensecapture.splitpay.model.SmartSplitParticipant
import com.lifeos.expensecapture.splitpay.model.UserPayProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class SplitPayResult<out T> {
    data class Success<T>(val value: T) : SplitPayResult<T>()
    data class Failure(val message: String) : SplitPayResult<Nothing>()
}

/**
 * Smart Split's data layer (2026-08) - see SplitPayModels.kt's kdoc for the collection layout.
 * `participants` lives as a subcollection (not an embedded array on SmartSplit) specifically so
 * the external web page (Track B, see docs/pay/index.html) can update ONE participant's status
 * with a narrowly-scoped Firestore security rule, rather than needing write access to the whole
 * settlement document including its financial totals.
 */
class SplitPayRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun splitsCollection() = firestore.collection("smartSplits")
    private fun participantsCollection(splitId: String) = splitsCollection().document(splitId).collection("participants")
    private fun usersCollection() = firestore.collection("users")

    suspend fun upsertPayProfile(profile: UserPayProfile): SplitPayResult<Unit> {
        if (profile.uid.isBlank()) return SplitPayResult.Failure("Not signed in")
        return try {
            usersCollection().document(profile.uid).set(profile).await()
            SplitPayResult.Success(Unit)
        } catch (e: Exception) {
            SplitPayResult.Failure(e.message ?: "Couldn't save your pay settings")
        }
    }

    /** Called on every sign-in (see FamilyAppViewModel) now that Firebase Auth itself is phone-
     * based - keeps this collection's phoneNumber/displayName in step with the auth account
     * without a separate manual "set your phone" step, and without ever touching upiId (merge,
     * not overwrite - a user's already-set UPI ID must survive every sign-in). */
    suspend fun syncPhoneAndName(uid: String, phoneNumber: String?, displayName: String): SplitPayResult<Unit> {
        if (uid.isBlank()) return SplitPayResult.Failure("Not signed in")
        return try {
            val fields = mutableMapOf<String, Any?>("uid" to uid, "displayName" to displayName)
            if (!phoneNumber.isNullOrBlank()) fields["phoneNumber"] = phoneNumber
            usersCollection().document(uid).set(fields, com.google.firebase.firestore.SetOptions.merge()).await()
            SplitPayResult.Success(Unit)
        } catch (e: Exception) {
            SplitPayResult.Failure(e.message ?: "Couldn't sync your profile")
        }
    }

    /** Bug fix (real user report, 2026-08 - see SmartSplitsScreen's kdoc): Firestore's
     * `.document("")` throws synchronously rather than just returning no data, which crashed
     * this whole flow for a signed-out user before the UI-level sign-in gate existed. Emits null
     * instead of ever making that call - a defense-in-depth backstop, not a replacement for the
     * UI gate (a signed-out user should never reach a screen that calls this at all). */
    fun observePayProfile(uid: String): Flow<UserPayProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = usersCollection().document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserPayProfile::class.java))
        }
        awaitClose { registration.remove() }
    }

    /** Track A's "his name lights up because he has the app" - a plain equality lookup on the
     * phone number the payer typed. Numbers are matched exactly as stored (see
     * normalizePhoneNumber in SmartSplitCreateScreen for the one normalization rule applied
     * before saving/searching - stripping spaces/dashes/a leading +91). */
    suspend fun findUserByPhone(phoneNumber: String): UserPayProfile? {
        return try {
            val result = usersCollection().whereEqualTo("phoneNumber", phoneNumber).limit(1).get().await()
            result.documents.firstOrNull()?.toObject(UserPayProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createSplit(split: SmartSplit, participants: List<SmartSplitParticipant>): SplitPayResult<String> {
        return try {
            val ref = splitsCollection().document()
            val saved = split.copy(id = ref.id)
            ref.set(saved).await()
            participants.forEach { participant ->
                val participantRef = participantsCollection(ref.id).document()
                participantRef.set(participant.copy(id = participantRef.id, splitId = ref.id)).await()
            }
            SplitPayResult.Success(ref.id)
        } catch (e: Exception) {
            SplitPayResult.Failure(e.message ?: "Couldn't save this split")
        }
    }

    fun observeSplit(splitId: String): Flow<SmartSplit?> = callbackFlow {
        val registration = splitsCollection().document(splitId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(SmartSplit::class.java))
        }
        awaitClose { registration.remove() }
    }

    /** Every split the current user created (they fronted the money) - the "owed to me" side.
     * The "I owe someone else" side is [observeSplitsIOwe], a separate query since it reads
     * participant rows across other people's splits, not SmartSplit docs of one's own. */
    fun observeMySplits(uid: String): Flow<List<SmartSplit>> = callbackFlow {
        val registration = splitsCollection()
            .whereEqualTo("payerId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(SmartSplit::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun observeParticipants(splitId: String): Flow<List<SmartSplitParticipant>> = callbackFlow {
        val registration = participantsCollection(splitId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(SmartSplitParticipant::class.java) } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    /** Every split a signed-in app user owes money in, across every OTHER person's splits - a
     * collectionGroup query across every split's participants subcollection, filtered to this
     * user's own participant rows. Powers "You owe ₹500 for Dinner" on the owing side. */
    fun observeSplitsIOwe(uid: String): Flow<List<SmartSplitParticipant>> = callbackFlow {
        val registration = firestore.collectionGroup("participants")
            .whereEqualTo("participantUserId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(SmartSplitParticipant::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateParticipantStatus(splitId: String, participantId: String, status: ParticipantStatus): SplitPayResult<Unit> {
        return try {
            val fields = mutableMapOf<String, Any?>("status" to status.name)
            when (status) {
                ParticipantStatus.PAID_VIA_UPI, ParticipantStatus.CLAIMED_PAID -> fields["paidAt"] = System.currentTimeMillis()
                ParticipantStatus.CONFIRMED -> fields["confirmedAt"] = System.currentTimeMillis()
                ParticipantStatus.PENDING -> {}
            }
            participantsCollection(splitId).document(participantId).update(fields).await()
            SplitPayResult.Success(Unit)
        } catch (e: Exception) {
            SplitPayResult.Failure(e.message ?: "Couldn't update status")
        }
    }
}
