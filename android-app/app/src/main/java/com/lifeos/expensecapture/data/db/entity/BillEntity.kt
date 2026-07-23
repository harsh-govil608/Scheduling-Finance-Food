package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bills PRD (Phase 3, Doc 22) state model. Unlike Subscriptions, amount is variable by design
 * (typicalAmount is a rough anchor, not an expected exact match) - this is the PRD's own stated
 * distinction from Subscription Manager: "amounts vary period to period and due dates can
 * shift." upcoming/due-today/overdue are derived at display time from dueDayOfMonth vs today,
 * not persisted.
 */
enum class BillStatus { DETECTED_UNCONFIRMED, CONFIRMED_TRACKED, CANCELLED }

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val payeeNormalized: String,
    val payeeDisplay: String,
    val typicalAmount: Double,
    val dueDayOfMonth: Int,
    val lastPaidDate: Long?,
    val status: BillStatus,
    val detectedAt: Long = System.currentTimeMillis(),
    val isManuallyAdded: Boolean = false
)
