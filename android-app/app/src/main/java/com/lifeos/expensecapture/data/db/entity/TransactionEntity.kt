package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionDirection { DEBIT, CREDIT }

enum class TransactionSource { SMS_AUTO, MANUAL }

/**
 * Mirrors the `transactions` table in the architecture doc (Section 7), with one addition:
 * `synced` tracks whether this row has been pushed to the backend once sync is wired.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val direction: TransactionDirection,
    val merchantRaw: String,
    val merchantNormalized: String,
    val categoryId: Long,
    val date: Long, // epoch millis
    val source: TransactionSource,
    val confidenceScore: Float,
    val isUserCorrected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
