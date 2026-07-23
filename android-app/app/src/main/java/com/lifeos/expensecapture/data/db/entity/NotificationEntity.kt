package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Notification Center PRD (Phase 3 Doc 03): the durable, reviewable record of every
 * notification this app has ever surfaced. Deliberately single-device scope - the full PRD's
 * cross-device read-state sync and arbitration-engine-fed digest/batching don't apply here,
 * since there's no backend and no arbitration engine (Phase 2 Notification System was never
 * implemented as code). See docs/coders-documentation/day-2.md for that scope boundary.
 */
enum class NotificationType { BILL_DUE, SUBSCRIPTION_RENEWAL, BUDGET_OVER_LIMIT, NIGHT_SUMMARY_READY }

@Entity(tableName = "app_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: NotificationType,
    val title: String,
    val body: String,
    /** Clean nav route this notification opens when tapped, e.g. "bills", "budgets". */
    val deepLinkRoute: String,
    /** Per-instance dedup key (e.g. "bills42") used only for the cooldown check - distinct
     * from deepLinkRoute since a route like "bills" is shared across many individual bills. */
    val sourceKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
