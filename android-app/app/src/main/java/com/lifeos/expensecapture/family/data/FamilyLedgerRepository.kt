package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.family.model.FamilyLedgerEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Family Expense Tracker's ledger (2026-08) - see FamilyLedgerEntry's kdoc for the collection
 * layout and why entries are flat copies rather than references. [syncEntry] is called from
 * ParseIncomingSmsWorker's live SMS capture path only (never the history backfill) - see that
 * worker's own kdoc on why anomaly/notification-style reactions are gated the same way.
 */
class FamilyLedgerRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun ledgerCollection(familyId: String) =
        firestore.collection("families").document(familyId).collection("ledger")

    suspend fun syncEntry(entry: FamilyLedgerEntry): FamilyResult<Unit> {
        return try {
            val ref = ledgerCollection(entry.familyId).document()
            ref.set(entry.copy(id = ref.id)).await()
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't sync to family ledger")
        }
    }

    /** Every entry at or after [sinceMillis], newest first - the Family Dashboard passes today's
     * start-of-day for "Total Family Spend Today," but this is deliberately not hardcoded to
     * "today" so a future weekly/monthly view can reuse it with a different boundary. */
    fun observeEntries(familyId: String, sinceMillis: Long): Flow<List<FamilyLedgerEntry>> = callbackFlow {
        val registration = ledgerCollection(familyId)
            .whereGreaterThanOrEqualTo("date", sinceMillis)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(FamilyLedgerEntry::class.java) } ?: emptyList())
            }
        awaitClose { registration.remove() }
    }
}
