package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Learned merchant -> category mappings. Seeded from user corrections
 * (see TransactionRepository.recategorize). This table is exactly the seed dataset
 * Phase 5's future categorization ML would train on - see architecture doc Section 12.
 */
@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantPattern: String,
    val categoryId: Long,
    val createdFromUserCorrection: Boolean = false
)
