package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Every SMS the parser could not extract a transaction from. Previously these were silently
 * discarded (see docs/coders-documentation/day-1.md Section 7) - that made a parse failure
 * indistinguishable from "nothing happened." Surfacing these lets the user manually convert
 * one into a real transaction, and gives us real data on how often/why parsing fails.
 *
 * `sourceHash` (sender::body) plus the unique index below exist for the same reason
 * TransactionEntity has one: without it, every re-scan of the same SMS inbox re-inserted every
 * unparsed message as a fresh duplicate row. This table didn't get the same fix at the time
 * TransactionEntity did, and became actively harmful once the scan started retrying on every
 * app resume instead of once - a real device went from 1266 to 2135 unparsed rows within
 * minutes from repeated rescans of the same already-seen messages.
 */
@Entity(
    tableName = "unparsed_messages",
    indices = [Index(value = ["sourceHash"], unique = true)]
)
data class UnparsedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val reason: String,
    val resolved: Boolean = false,
    val sourceHash: String = java.util.UUID.randomUUID().toString()
)
