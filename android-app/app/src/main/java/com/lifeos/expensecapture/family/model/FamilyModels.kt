package com.lifeos.expensecapture.family.model

/**
 * Family module (2026-08) - core Firestore-backed models. Everything here is a plain Kotlin data
 * class rather than a Room @Entity: this module is cross-device by nature (a family's data has
 * to be visible on every member's phone, not just the device that created it), which is exactly
 * what the rest of this app's local-only Room database was never built for - see AppDatabase's
 * kdoc and MIGRATION history for that deliberate boundary. Firestore document IDs double as
 * these models' `id` fields; there is no separate local primary key scheme to keep in sync.
 *
 * Collection layout:
 *   families/{familyId}
 *   families/{familyId}/members/{userId}
 *   families/{familyId}/invitations/{inviteId}
 *   families/{familyId}/events/{eventId}          - FamilyEvent, the AI-ready event stream
 *   families/{familyId}/tasks/{taskId}             - SharedTask
 *   families/{familyId}/calendarEvents/{eventId}   - SharedCalendarEvent
 *   families/{familyId}/expenses/{expenseId}       - SharedExpense
 *   families/{familyId}/documents/{documentId}     - SharedDocument
 *   families/{familyId}/healthRecords/{recordId}   - HealthRecord
 *   families/{familyId}/emergencyContacts/{id}     - EmergencyContact
 *   families/{familyId}/sosAlerts/{alertId}        - SOSAlert
 *   families/{familyId}/notifications/{id}         - FamilyNotification
 */

/** Owner created the family and can never be removed except by deleting the family outright.
 * Parent/Adult both get full visibility by default (subject to PermissionSet overrides) - the
 * distinction exists for role-management UI (who else can promote/demote members) rather than a
 * data-visibility difference. Child and Guest are the two roles PermissionSet exists to restrict. */
enum class FamilyRole { OWNER, PARENT, ADULT, CHILD, GUEST }

enum class PermissionType { LOCATION, DOCUMENTS, HEALTH, EXPENSES }

data class FamilyEntity(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L,
    /** Denormalized member-id list so "which families is this user in" is a single-field query
     * against the family doc itself, without a collection-group query across every family's
     * members subcollection. Kept in sync by FamilyRepository whenever membership changes. */
    val memberIds: List<String> = emptyList()
)

data class FamilyMember(
    val userId: String = "",
    val familyId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: FamilyRole = FamilyRole.ADULT,
    val joinedAt: Long = 0L,
    /** Per-member override of what others can see about *this* member - a Guest's own
     * PermissionSet might allow nothing, while a Parent's allows everything. Checked via
     * PermissionGate (see PermissionGate.kt), never inlined ad hoc in a screen. */
    val permissions: PermissionSet = PermissionSet()
)

/** Defaults deliberately favor Guest/Child-safe visibility being OFF and Adult/Parent-safe
 * visibility being the caller's responsibility to set explicitly when a member is created with a
 * sensitive role - see FamilyRepository.addMember's kdoc. */
data class PermissionSet(
    val locationVisible: Boolean = true,
    val documentsVisible: Boolean = true,
    val healthVisible: Boolean = true,
    val expensesVisible: Boolean = true
) {
    fun isVisible(type: PermissionType): Boolean = when (type) {
        PermissionType.LOCATION -> locationVisible
        PermissionType.DOCUMENTS -> documentsVisible
        PermissionType.HEALTH -> healthVisible
        PermissionType.EXPENSES -> expensesVisible
    }
}

enum class InvitationStatus { PENDING, ACCEPTED, EXPIRED, REVOKED }

/** Both "invite via email" and "invite via share link" resolve to the same Invitation doc - a
 * short random `code` is the actual join key (typed in manually or embedded in a deep-link URL),
 * `invitedEmail` is only ever a hint shown in the family's pending-invites list, never something
 * the client enforces against (Firestore has no way to verify an email address belongs to the
 * account completing the join - that check happens implicitly via Firebase Auth's own email
 * verification, out of this module's scope). */
data class Invitation(
    val id: String = "",
    val familyId: String = "",
    val code: String = "",
    val invitedEmail: String? = null,
    val proposedRole: FamilyRole = FamilyRole.ADULT,
    val createdBy: String = "",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val status: InvitationStatus = InvitationStatus.PENDING
)

enum class PresenceStatus { ONLINE, OFFLINE, AWAY }

data class MemberPresence(
    val userId: String = "",
    val status: PresenceStatus = PresenceStatus.OFFLINE,
    val lastSeenAt: Long = 0L,
    /** Null unless the member has an active location-sharing session AND
     * PermissionSet.locationVisible is true for them - the repository layer, not just the UI,
     * withholds this field for members who haven't opted in (see PresenceRepository). */
    val lastLocation: GeoPoint? = null,
    val lastLocationAt: Long = 0L
)

data class GeoPoint(val latitude: Double = 0.0, val longitude: Double = 0.0)

/**
 * AI-ready event stream (the founder's own architectural ask): every meaningful family action
 * becomes one of these instead of being inferable only by diffing collections over time. `type`
 * is a stable string key (see FamilyEventType), `payload` is a flat string-keyed map so new event
 * types never require a schema migration - a future natural-language query/summary feature reads
 * this collection ordered by `timestamp`, nothing else needs to change to support that later.
 */
data class FamilyEvent(
    val id: String = "",
    val familyId: String = "",
    val type: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val timestamp: Long = 0L,
    val payload: Map<String, String> = emptyMap()
)

/** Stable event-type keys for FamilyEvent.type - a closed set kept here so producers and any
 * future consumer (activity feed, AI summarizer) agree on spelling without stringly-typed drift. */
object FamilyEventType {
    const val FAMILY_CREATED = "family_created"
    const val MEMBER_JOINED = "member_joined"
    const val MEMBER_ROLE_CHANGED = "member_role_changed"
    const val MEMBER_PERMISSIONS_CHANGED = "member_permissions_changed"
    const val TASK_CREATED = "task_created"
    const val TASK_COMPLETED = "task_completed"
    const val CALENDAR_EVENT_CREATED = "calendar_event_created"
    const val EXPENSE_ADDED = "expense_added"
    const val DOCUMENT_ADDED = "document_added"
    const val HEALTH_RECORD_ADDED = "health_record_added"
    const val EMERGENCY_CONTACT_ADDED = "emergency_contact_added"
    const val SOS_TRIGGERED = "sos_triggered"
    const val SOS_RESOLVED = "sos_resolved"
    const val MEMBER_ARRIVED = "member_arrived"
    const val MEMBER_DEPARTED = "member_departed"
}
