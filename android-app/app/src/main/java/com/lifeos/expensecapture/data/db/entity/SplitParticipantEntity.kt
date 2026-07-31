package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One person's share of a SplitExpenseEntity - see its kdoc. `name` is plain free text, not a
 * link to any contact/user system (this app has none) - the same reasoning TaskEntity/
 * ProjectEntity use plain nullable IDs instead of Room @ForeignKey elsewhere in this schema.
 * `settled` is the whole point of this entity: whether this person has paid their share back,
 * set by the device owner tapping "Mark as paid" - there's no way for the app to know this on
 * its own (no shared ledger, no notification from the other person).
 */
@Entity(tableName = "split_participants")
data class SplitParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitExpenseId: Long,
    val name: String,
    val shareAmount: Double,
    val settled: Boolean = false,
    val settledAt: Long? = null
)
