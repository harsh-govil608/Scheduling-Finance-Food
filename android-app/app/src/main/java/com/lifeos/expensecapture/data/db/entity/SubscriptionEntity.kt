package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Subscription Manager PRD (Phase 3, Doc 19) state model: detected-unconfirmed, confirmed-
 * tracked, and cancelled (the PRD's other named states - renewal-upcoming, renewed,
 * unrecognized-flagged, lapsed - are derived at display time from lastSeenTransactionDate vs
 * cadenceDays rather than persisted, since they're a function of "now", not stored facts).
 */
enum class SubscriptionStatus { DETECTED_UNCONFIRMED, CONFIRMED_TRACKED, CANCELLED }

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantNormalized: String,
    val merchantDisplay: String,
    val amount: Double,
    val cadenceDays: Int,
    val lastTransactionDate: Long,
    val status: SubscriptionStatus,
    val detectedAt: Long = System.currentTimeMillis(),
    val isManuallyAdded: Boolean = false
)
