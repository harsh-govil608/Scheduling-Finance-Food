package com.lifeos.expensecapture.family.model

/**
 * Family Expense Tracker (2026-08 real user request) - one entry per SMS-auto-captured
 * transaction, synced from a member's local Room database (see TransactionEntity) the instant
 * it's parsed. Lives under families/{familyId}/ledger/{id}, a sibling collection to every other
 * shared module - see FamilyModels.kt's kdoc for the full collection layout.
 *
 * Deliberately a flat copy of just the fields the family dashboard needs (merchant/amount/
 * direction/category/who/when), not a reference back to the local TransactionEntity - the local
 * transaction's Room row ID means nothing on another member's phone, and this entry needs to
 * keep existing even if the original local row is later edited/deleted on the source device.
 * `direction`/`categoryName` are plain strings rather than importing TransactionDirection/
 * CategoryEntity, keeping this module decoupled from local Room entities - see
 * SharedExpense's kdoc for the same reasoning applied to Smart Split.
 */
data class FamilyLedgerEntry(
    val id: String = "",
    val familyId: String = "",
    val memberUserId: String = "",
    val memberName: String = "",
    val merchantName: String = "",
    val amount: Double = 0.0,
    val direction: String = "",
    val categoryName: String = "",
    val date: Long = 0L,
    val syncedAt: Long = 0L
)
