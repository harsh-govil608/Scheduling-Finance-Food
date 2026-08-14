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
    val confirmedAt: Long? = null,
    /** Denormalized from the parent SmartSplit at creation time (real user report, 2026-08: the
     * "You owe" list showed only an amount, no indication who it was owed to or what for) - lets
     * SmartSplitsScreen's observeSplitsIOwe-backed list render both without a separate lookup per
     * row. Blank on any participant doc written before this field existed. */
    val payerName: String = "",
    val description: String = ""
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
 * shared.
 *
 * Two cleanup mechanisms, deliberately both: [SplitPayRepository.pruneOldSplitHistory] is an
 * opportunistic client-side purge checked whenever the history screen loads (works immediately,
 * but never runs if that screen is never opened again). `expiresAt` (real founder request,
 * 2026-08-11: "enable auto cleanup" for real, not just opportunistically) is a genuine
 * server-side Firestore TTL field - Firestore auto-deletes a document once its TTL field's value
 * is in the past, no app code involved, works even if this user never opens the app again. This
 * is the one field in this entire project stored as a Firestore Timestamp instead of a plain
 * epoch-millis Long - a hard Firestore platform requirement, TTL policies only ever act on a
 * Timestamp-typed field, never a number. Requires a TTL policy actually created in the Firebase
 * console (collection group `splitHistory`, field `expiresAt`) - the field alone does nothing
 * until that policy exists. `deletedAt` (still a plain Long) is kept separately for the history
 * list's own "deleted 3 days ago" display - the two fields answer different questions (when was
 * this deleted vs. when should Firestore remove the record) and shouldn't be conflated. */
data class SplitHistoryEntry(
    val id: String = "",
    val description: String = "",
    val totalAmount: Double = 0.0,
    val payerName: String = "",
    val participantNames: String = "",
    val deletedAt: Long = 0L,
    val expiresAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)
