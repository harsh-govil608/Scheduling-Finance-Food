package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Every recategorization a user makes. This is the pilot's core validation signal. */
@Entity(tableName = "corrections")
data class CorrectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val oldCategoryId: Long,
    val newCategoryId: Long,
    val correctedAt: Long = System.currentTimeMillis()
)
