package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifeos.expensecapture.family.model.FamilyEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * AI-ready event stream (2026-08) - the founder's own architectural ask: "every family event is
 * stored as a structured event stream that can later power natural-language queries and
 * summaries." Every other repository in this module calls [log] after a real write (member
 * joined, task created, SOS triggered, ...) rather than the UI trying to reconstruct "what
 * happened" later by diffing collections - see FamilyEventType for the closed set of event kinds.
 * Nothing here does any summarizing or querying itself; this is deliberately just the write path
 * and a plain time-ordered read, so a future NL-query feature has real structured data to read
 * from day one instead of a backend rewrite to start collecting it.
 */
class EventStreamRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun eventsCollection(familyId: String) =
        firestore.collection("families").document(familyId).collection("events")

    suspend fun log(event: FamilyEvent) {
        val ref = eventsCollection(event.familyId).document()
        ref.set(event.copy(id = ref.id)).await()
    }

    /** Newest first, capped at [limit] - a Family Dashboard's "Recent Activity" feed, not the
     * full history browser (that would paginate; out of scope for this pass). */
    fun observeRecentEvents(familyId: String, limit: Long = 30): Flow<List<FamilyEvent>> = callbackFlow {
        val registration = eventsCollection(familyId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { it.toObject(FamilyEvent::class.java) } ?: emptyList()
                trySend(events)
            }
        awaitClose { registration.remove() }
    }
}
