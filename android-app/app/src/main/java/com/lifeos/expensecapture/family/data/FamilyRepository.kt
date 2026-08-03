package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.family.model.FamilyEntity
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyEventType
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.FamilyRole
import com.lifeos.expensecapture.family.model.Invitation
import com.lifeos.expensecapture.family.model.InvitationStatus
import com.lifeos.expensecapture.family.model.PermissionSet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class FamilyResult<out T> {
    data class Success<T>(val value: T) : FamilyResult<T>()
    data class Failure(val message: String) : FamilyResult<Nothing>()
}

private const val INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no 0/O/1/I - hand-typed codes
private const val INVITE_EXPIRY_MILLIS = 7L * 24 * 60 * 60 * 1000 // 7 days

/**
 * Multi-family core (2026-08 Family module) - create/join a family, manage members' roles and
 * permissions, and issue invitations (both "share link" and "email" resolve to the same
 * Invitation doc + join code, see Invitation's kdoc). Every mutating call also appends a
 * FamilyEvent via [EventStreamRepository] so the dashboard's activity feed and any future
 * natural-language summary read the exact same source of truth as "what actually happened,"
 * rather than a separately-maintained activity log that could drift from it.
 */
class FamilyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private fun familyDoc(familyId: String) = firestore.collection("families").document(familyId)
    private fun membersCollection(familyId: String) = familyDoc(familyId).collection("members")
    private fun invitationsCollection(familyId: String) = familyDoc(familyId).collection("invitations")

    suspend fun createFamily(name: String, ownerId: String, ownerDisplayName: String): FamilyResult<String> {
        return try {
            val ref = firestore.collection("families").document()
            val family = FamilyEntity(
                id = ref.id,
                name = name,
                ownerId = ownerId,
                createdAt = System.currentTimeMillis(),
                memberIds = listOf(ownerId)
            )
            ref.set(family).await()
            membersCollection(ref.id).document(ownerId).set(
                FamilyMember(
                    userId = ownerId,
                    familyId = ref.id,
                    displayName = ownerDisplayName,
                    role = FamilyRole.OWNER,
                    joinedAt = System.currentTimeMillis(),
                    permissions = PermissionSet()
                )
            ).await()
            eventStream.log(
                FamilyEvent(
                    familyId = ref.id,
                    type = FamilyEventType.FAMILY_CREATED,
                    actorId = ownerId,
                    actorName = ownerDisplayName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("familyName" to name)
                )
            )
            FamilyResult.Success(ref.id)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't create family")
        }
    }

    fun observeFamily(familyId: String): Flow<FamilyEntity?> = callbackFlow {
        val registration = familyDoc(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(FamilyEntity::class.java))
        }
        awaitClose { registration.remove() }
    }

    /** Every family a user belongs to - the Family module's entry point decides between "no
     * families yet -> onboarding" and "families exist -> family switcher/dashboard" off this. */
    fun observeUserFamilies(userId: String): Flow<List<FamilyEntity>> = callbackFlow {
        val registration = firestore.collection("families")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(FamilyEntity::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    fun observeMembers(familyId: String): Flow<List<FamilyMember>> = callbackFlow {
        val registration = membersCollection(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { it.toObject(FamilyMember::class.java) } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateMemberRole(familyId: String, targetUserId: String, newRole: FamilyRole, actorId: String, actorName: String): FamilyResult<Unit> {
        return try {
            membersCollection(familyId).document(targetUserId).update("role", newRole.name).await()
            eventStream.log(
                FamilyEvent(
                    familyId = familyId,
                    type = FamilyEventType.MEMBER_ROLE_CHANGED,
                    actorId = actorId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("targetUserId" to targetUserId, "newRole" to newRole.name)
                )
            )
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't update role")
        }
    }

    suspend fun updateMemberPermissions(
        familyId: String,
        targetUserId: String,
        permissions: PermissionSet,
        actorId: String,
        actorName: String
    ): FamilyResult<Unit> {
        return try {
            membersCollection(familyId).document(targetUserId).update("permissions", permissions).await()
            eventStream.log(
                FamilyEvent(
                    familyId = familyId,
                    type = FamilyEventType.MEMBER_PERMISSIONS_CHANGED,
                    actorId = actorId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("targetUserId" to targetUserId)
                )
            )
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't update permissions")
        }
    }

    suspend fun createInvitation(
        familyId: String,
        invitedPhone: String?,
        proposedRole: FamilyRole,
        createdBy: String
    ): FamilyResult<Invitation> {
        return try {
            val ref = invitationsCollection(familyId).document()
            val invitation = Invitation(
                id = ref.id,
                familyId = familyId,
                code = randomInviteCode(),
                invitedPhone = invitedPhone,
                proposedRole = proposedRole,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + INVITE_EXPIRY_MILLIS,
                status = InvitationStatus.PENDING
            )
            ref.set(invitation).await()
            FamilyResult.Success(invitation)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't create invitation")
        }
    }

    fun observeInvitations(familyId: String): Flow<List<Invitation>> = callbackFlow {
        val registration = invitationsCollection(familyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(Invitation::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    /**
     * Joins by invite code (typed manually, or extracted from a share-link deep link before this
     * is called - see FamilyOnboardingViewModel). Validates expiry/status client-side; real
     * concurrent-use protection (two people racing the same single-use code) would need a
     * Firestore transaction or a Cloud Function - out of scope for a family-size invite flow
     * where that race is vanishingly unlikely, flagged here rather than silently assumed solved.
     */
    suspend fun joinFamilyByCode(code: String, userId: String, displayName: String): FamilyResult<String> {
        return try {
            val matches = firestore.collectionGroup("invitations")
                .whereEqualTo("code", code.trim().uppercase())
                .whereEqualTo("status", InvitationStatus.PENDING.name)
                .get()
                .await()
            val invitationDoc = matches.documents.firstOrNull()
                ?: return FamilyResult.Failure("Invite code not found or already used")
            val invitation = invitationDoc.toObject(Invitation::class.java)
                ?: return FamilyResult.Failure("Invite code not found or already used")
            if (invitation.expiresAt < System.currentTimeMillis()) {
                invitationDoc.reference.update("status", InvitationStatus.EXPIRED.name).await()
                return FamilyResult.Failure("This invite has expired")
            }

            membersCollection(invitation.familyId).document(userId).set(
                FamilyMember(
                    userId = userId,
                    familyId = invitation.familyId,
                    displayName = displayName,
                    role = invitation.proposedRole,
                    joinedAt = System.currentTimeMillis(),
                    permissions = PermissionSet()
                )
            ).await()
            familyDoc(invitation.familyId).update("memberIds", FieldValue.arrayUnion(userId)).await()
            invitationDoc.reference.update("status", InvitationStatus.ACCEPTED.name).await()

            eventStream.log(
                FamilyEvent(
                    familyId = invitation.familyId,
                    type = FamilyEventType.MEMBER_JOINED,
                    actorId = userId,
                    actorName = displayName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("role" to invitation.proposedRole.name)
                )
            )
            FamilyResult.Success(invitation.familyId)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't join family")
        }
    }

    private fun randomInviteCode(length: Int = 6): String =
        (1..length).map { INVITE_CODE_CHARS.random() }.joinToString("")
}
