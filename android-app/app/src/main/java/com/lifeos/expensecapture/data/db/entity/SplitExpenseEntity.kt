package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Split Expenses (real user review: "how do I note expenses split among a group and later they
 * are returned back"). Scoped to what's honest for a single-device, no-accounts app: the device
 * owner is always the one who fronted `totalAmount` - there's no login/sync, so this can't track
 * a group's shared view of who-owes-whom the way a multi-user app like Splitwise would. Each
 * participant's share and paid-back status lives in SplitParticipantEntity, one row per person.
 */
@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val totalAmount: Double,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
