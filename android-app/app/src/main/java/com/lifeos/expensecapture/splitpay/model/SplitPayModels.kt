package com.lifeos.expensecapture.splitpay.model

/**
 * Smart Split (2026-08) - the auto-working UPI version of Split Expenses (see
 * SplitExpenseEntity's kdoc for the original local-only feature this sits alongside, not
 * replaces). Cross-device by nature (someone else's phone has to see "you owe X" and settle it),
 * so this is Firestore-backed like the Family module, but deliberately NOT nested under a family
 * - splitting a dinner bill with Rahul and Amit has nothing to do with family membership. Reuses
 * the same Firebase project and FamilyAuthRepository sign-in - one account covers both modules.
 *
 * Collection layout:
 *   users/{uid}                          - UserPayProfile (upiId, phoneNumber, displayName)
 *   users/{uid}/splitHistory/{id}        - SplitHistoryEntry, see its own kdoc
 *   smartSplits/{splitId}                - SmartSplit
 *   smartSplits/{splitId}/participants/{participantId} - SmartSplitParticipant
 */

/** Keyed by Firebase Auth uid - the same identity FamilyAuthRepository already establishes.
 * `phoneNumber` is how a payer finds "does Rahul have the app" (Track A); `upiId` is what gets
 * embedded in every UPI deep link pointing at this person as the payee. Both are opt-in, set
 * once in Pay Settings - null until the user fills them in. */
data class UserPayProfile(
    val uid: String = "",
    val displayName: String = "",
    val phoneNumber: String? = null,
    val upiId: String? = null
)

enum class ParticipantStatus { PENDING, PAID_VIA_UPI, CLAIMED_PAID, CONFIRMED }

/** One person's share of a SmartSplit. `participantUserId` is non-null only for a matched app
 * user (Track A) - null means "external" (Track B), identified by name + phone only, per the
 * real user request this models: "type Amit, type in Amit's WhatsApp/Phone number... the app
 * notes that Amit is an External User." */
data class SmartSplitParticipant(
    val id: String = "",
    val splitId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val participantUserId: String? = null,
    val isExternal: Boolean = true,
    val shareAmount: Double = 0.0,
    val status: ParticipantStatus = ParticipantStatus.PENDING,
    val paidAt: Long? = null,
    val confirmedAt: Long? = null
)

data class SmartSplit(
    val id: String = "",
    val description: String = "",
    val totalAmount: Double = 0.0,
    val payerId: String = "",
    val payerName: String = "",
    val payerUpiId: String = "",
    val date: Long = 0L,
    val createdAt: Long = 0L
)

/** A lightweight record of a deleted split (2026-08, real user request: "unable to delete the
 * existing smart split - add history to store records of it and automatically delete after 1
 * month"). Kept separately from the live `smartSplits` collection rather than just soft-deleting
 * the SmartSplit doc in place, since deleting also has to purge its `participants` subcollection
 * (Firestore never cascade-deletes that on its own - same reasoning as FamilyRepository.deleteFamily)
 * - this is what's left over once that's done. Scoped under the deleting user's own `users/{uid}`
 * doc rather than a top-level collection, since "my split history" is inherently per-person, not
 * shared. Auto-purged after 30 days by [SplitPayRepository.pruneOldSplitHistory] - checked
 * opportunistically when the history screen loads, the same on-demand-cleanup pattern this app
 * already uses elsewhere (no server-side cron here). */
data class SplitHistoryEntry(
    val id: String = "",
    val description: String = "",
    val totalAmount: Double = 0.0,
    val payerName: String = "",
    val participantNames: String = "",
    val deletedAt: Long = 0L
)
