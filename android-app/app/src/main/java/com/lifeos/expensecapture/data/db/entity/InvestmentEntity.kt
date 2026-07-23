package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Investments (Future) PRD, Phase 3 Doc 23: the PRD explicitly scopes itself to "a minimal,
 * manually entered, read-only holdings feature" with brokerage sync, tax-lot tracking, and
 * advice all explicitly named as deferred within the document itself - so this minimal model
 * IS the PRD's own completion bar, not a corner cut relative to it.
 */
@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentValue: Double,
    val createdAt: Long = System.currentTimeMillis()
)
