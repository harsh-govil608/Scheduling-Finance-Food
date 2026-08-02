package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Shared plumbing for the six shared modules (Tasks/Calendar/Expenses/Documents/Health Records/
 * Emergency Contacts) - each is a plain Firestore collection under families/{familyId}/{name},
 * real-time-observed and CRUD'd the exact same way, so this is written once instead of six times.
 * Uses Firestore's own reflection-based (de)serialization (every model in SharedModuleModels.kt
 * is a data class with all-default-value fields, which is all `toObject`/`set` need) - the
 * `id` field is never written to the document itself (Firestore's own doc.id is authoritative);
 * [withId] rehydrates it onto the Kotlin object at read time only, for callers that need it to
 * target an update/delete.
 */
class FamilyCollectionRepository<T : Any>(
    firestore: FirebaseFirestore,
    familyId: String,
    collectionName: String,
    private val clazz: Class<T>,
    private val withId: (T, String) -> T
) {
    private val collection = firestore.collection("families").document(familyId).collection(collectionName)

    fun observeAll(orderByField: String, descending: Boolean = true): Flow<List<T>> = callbackFlow {
        val direction = if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
        val registration = collection.orderBy(orderByField, direction).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(clazz)?.let { withId(it, doc.id) }
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    suspend fun add(item: T): FamilyResult<String> {
        return try {
            val ref = collection.document()
            ref.set(item).await()
            FamilyResult.Success(ref.id)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't save")
        }
    }

    suspend fun update(id: String, fields: Map<String, Any?>): FamilyResult<Unit> {
        return try {
            collection.document(id).update(fields).await()
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't update")
        }
    }

    suspend fun delete(id: String): FamilyResult<Unit> {
        return try {
            collection.document(id).delete().await()
            FamilyResult.Success(Unit)
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Couldn't delete")
        }
    }
}
