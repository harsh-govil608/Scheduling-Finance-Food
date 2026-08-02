package com.lifeos.expensecapture.family.data

import com.google.firebase.firestore.FirebaseFirestore
import com.lifeos.expensecapture.family.model.EmergencyContact
import com.lifeos.expensecapture.family.model.FamilyEvent
import com.lifeos.expensecapture.family.model.FamilyEventType
import com.lifeos.expensecapture.family.model.HealthRecord
import com.lifeos.expensecapture.family.model.SharedCalendarEvent
import com.lifeos.expensecapture.family.model.SharedDocument
import com.lifeos.expensecapture.family.model.SharedExpense
import com.lifeos.expensecapture.family.model.SharedTask
import kotlinx.coroutines.flow.Flow

/**
 * Six thin facades over [FamilyCollectionRepository] (2026-08 Family module) - one per shared
 * module, each just wiring the collection name/type and logging the matching FamilyEventType on
 * create so the dashboard's activity feed picks it up for free. Tasks is the fully-built
 * reference pattern (assignment, due date, completion toggle); the other five follow the same
 * shape with simpler fields, real Firestore CRUD throughout - none of these six is mock/sample
 * data, per the module's own "avoid mock data" requirement.
 */

class SharedTaskRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "tasks", SharedTask::class.java
    ) { item, id -> item.copy(id = id) }

    fun observeAll(): Flow<List<SharedTask>> = repo.observeAll(orderByField = "createdAt")

    suspend fun add(task: SharedTask, actorName: String): FamilyResult<String> {
        val result = repo.add(task)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = task.familyId,
                    type = FamilyEventType.TASK_CREATED,
                    actorId = task.createdBy,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("title" to task.title)
                )
            )
        }
        return result
    }

    suspend fun setCompleted(task: SharedTask, completed: Boolean, actorId: String, actorName: String): FamilyResult<Unit> {
        val result = repo.update(task.id, mapOf("completed" to completed))
        if (result is FamilyResult.Success && completed) {
            eventStream.log(
                FamilyEvent(
                    familyId = task.familyId,
                    type = FamilyEventType.TASK_COMPLETED,
                    actorId = actorId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("title" to task.title)
                )
            )
        }
        return result
    }

    suspend fun delete(taskId: String): FamilyResult<Unit> = repo.delete(taskId)
}

class SharedCalendarRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "calendarEvents", SharedCalendarEvent::class.java
    ) { item, id -> item.copy(id = id) }

    fun observeAll(): Flow<List<SharedCalendarEvent>> = repo.observeAll(orderByField = "startAt", descending = false)

    suspend fun add(event: SharedCalendarEvent, actorName: String): FamilyResult<String> {
        val result = repo.add(event)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = event.familyId,
                    type = FamilyEventType.CALENDAR_EVENT_CREATED,
                    actorId = event.createdBy,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("title" to event.title)
                )
            )
        }
        return result
    }

    suspend fun delete(eventId: String): FamilyResult<Unit> = repo.delete(eventId)
}

class SharedExpenseRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "expenses", SharedExpense::class.java
    ) { item, id -> item.copy(id = id) }

    fun observeAll(): Flow<List<SharedExpense>> = repo.observeAll(orderByField = "date")

    suspend fun add(expense: SharedExpense, actorName: String): FamilyResult<String> {
        val result = repo.add(expense)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = expense.familyId,
                    type = FamilyEventType.EXPENSE_ADDED,
                    actorId = expense.paidByUserId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("description" to expense.description, "amount" to expense.amount.toString())
                )
            )
        }
        return result
    }

    suspend fun delete(expenseId: String): FamilyResult<Unit> = repo.delete(expenseId)
}

class SharedDocumentRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "documents", SharedDocument::class.java
    ) { item, id -> item.copy(id = id) }

    fun observeAll(): Flow<List<SharedDocument>> = repo.observeAll(orderByField = "uploadedAt")

    suspend fun add(document: SharedDocument, actorName: String): FamilyResult<String> {
        val result = repo.add(document)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = document.familyId,
                    type = FamilyEventType.DOCUMENT_ADDED,
                    actorId = document.uploadedByUserId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("title" to document.title)
                )
            )
        }
        return result
    }

    suspend fun delete(documentId: String): FamilyResult<Unit> = repo.delete(documentId)
}

class HealthRecordRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "healthRecords", HealthRecord::class.java
    ) { item, id -> item.copy(id = id) }

    /** [PermissionType.HEALTH]-sensitive by nature - callers must PermissionGate this list per
     * member before rendering, this repository itself has no notion of who's asking. */
    fun observeAll(): Flow<List<HealthRecord>> = repo.observeAll(orderByField = "recordDate")

    suspend fun add(record: HealthRecord, actorName: String): FamilyResult<String> {
        val result = repo.add(record)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = record.familyId,
                    type = FamilyEventType.HEALTH_RECORD_ADDED,
                    actorId = record.createdBy,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("title" to record.title, "memberUserId" to record.memberUserId)
                )
            )
        }
        return result
    }

    suspend fun delete(recordId: String): FamilyResult<Unit> = repo.delete(recordId)
}

class EmergencyContactRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    familyId: String,
    private val eventStream: EventStreamRepository = EventStreamRepository(firestore)
) {
    private val repo = FamilyCollectionRepository(
        firestore, familyId, "emergencyContacts", EmergencyContact::class.java
    ) { item, id -> item.copy(id = id) }

    fun observeAll(): Flow<List<EmergencyContact>> = repo.observeAll(orderByField = "createdAt", descending = false)

    suspend fun add(contact: EmergencyContact, actorId: String, actorName: String): FamilyResult<String> {
        val result = repo.add(contact)
        if (result is FamilyResult.Success) {
            eventStream.log(
                FamilyEvent(
                    familyId = contact.familyId,
                    type = FamilyEventType.EMERGENCY_CONTACT_ADDED,
                    actorId = actorId,
                    actorName = actorName,
                    timestamp = System.currentTimeMillis(),
                    payload = mapOf("name" to contact.name)
                )
            )
        }
        return result
    }

    suspend fun delete(contactId: String): FamilyResult<Unit> = repo.delete(contactId)
}
