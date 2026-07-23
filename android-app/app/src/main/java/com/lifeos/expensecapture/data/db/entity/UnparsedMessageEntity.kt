package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Every SMS the parser could not extract a transaction from. Previously these were silently
 * discarded (see docs/coders-documentation/day-1.md Section 7) - that made a parse failure
 * indistinguishable from "nothing happened." Surfacing these lets the user manually convert
 * one into a real transaction, and gives us real data on how often/why parsing fails.
 */
@Entity(tableName = "unparsed_messages")
data class UnparsedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val reason: String,
    val resolved: Boolean = false
)
