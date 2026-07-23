package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Learned merchant -> category mappings. Two distinct origins now coexist here (Automation
 * Rules PRD, Phase 3 Doc 34, requires this distinction be visible, not just functional):
 * - createdFromUserCorrection = true: the AI's OWN inferred automation, silently created when
 *   a user corrects a transaction's category (Automation Philosophy's learning behavior -
 *   explicitly out of scope for Doc 34).
 * - createdFromUserCorrection = false (and isManuallyAuthored = true): a rule the user
 *   deliberately typed in via the Automation Rules screen - the actual "user-authored rule"
 *   Doc 34 is about, a materially different trust relationship per that PRD's own framing.
 *
 * isPaused lets a user temporarily disable a rule without losing it (Doc 34 Feature Scope:
 * "pause a rule temporarily without deleting it, in case my situation changes").
 */
@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantPattern: String,
    val categoryId: Long,
    val createdFromUserCorrection: Boolean = false,
    val isManuallyAuthored: Boolean = false,
    val isPaused: Boolean = false
)
