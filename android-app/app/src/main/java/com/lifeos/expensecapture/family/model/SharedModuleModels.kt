package com.lifeos.expensecapture.family.model

/**
 * Family module (2026-08) - the six shared-module models (Tasks, Calendar, Expenses, Documents,
 * Health Records, Emergency Contacts), each a Firestore document under
 * families/{familyId}/<collection>/{id}. All real (no mock/sample rows shipped anywhere in this
 * module - see FamilyRepository's kdoc), all permission-gated at render time via PermissionGate
 * for the four types PermissionSet actually restricts (location/documents/health/expenses); Tasks
 * and Calendar have no PermissionType of their own and are visible to every member by design -
 * a shared to-do list that half the family can't see isn't shared.
 */

data class SharedTask(
    val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val assignedToUserId: String? = null,
    val dueDate: Long? = null,
    val completed: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = 0L
)

data class SharedCalendarEvent(
    val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val startAt: Long = 0L,
    val endAt: Long? = null,
    val location: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L
)

/** Deliberately separate from this app's existing local TransactionEntity (Finance pillar) -
 * that entity is the founder's own single-device SMS-parsed ledger; a family-shared expense is a
 * different real-world thing (who paid, split visibility across members) even when the amount
 * happens to be the same ₹. See SplitExpenseEntity for the closest existing local analogue this
 * mirrors the shape of. */
data class SharedExpense(
    val id: String = "",
    val familyId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidByUserId: String = "",
    val date: Long = 0L,
    val createdAt: Long = 0L
)

data class SharedDocument(
    val id: String = "",
    val familyId: String = "",
    val title: String = "",
    val storageUrl: String = "",
    val uploadedByUserId: String = "",
    val uploadedAt: Long = 0L
)

data class HealthRecord(
    val id: String = "",
    val familyId: String = "",
    val memberUserId: String = "",
    val title: String = "",
    val notes: String = "",
    val recordDate: Long = 0L,
    val createdBy: String = "",
    val createdAt: Long = 0L
)

data class EmergencyContact(
    val id: String = "",
    val familyId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val createdAt: Long = 0L
)

enum class SOSStatus { ACTIVE, RESOLVED }

data class SOSAlert(
    val id: String = "",
    val familyId: String = "",
    val triggeredByUserId: String = "",
    val triggeredByName: String = "",
    val triggeredAt: Long = 0L,
    val location: GeoPoint? = null,
    val status: SOSStatus = SOSStatus.ACTIVE,
    val resolvedAt: Long? = null
)

enum class FamilyNotificationType { REMINDER, ARRIVAL, DEPARTURE, MEDICINE, BILL_DUE, SOS }

data class FamilyNotification(
    val id: String = "",
    val familyId: String = "",
    val type: FamilyNotificationType = FamilyNotificationType.REMINDER,
    val title: String = "",
    val body: String = "",
    val relatedUserId: String? = null,
    val createdAt: Long = 0L,
    val read: Boolean = false
)
