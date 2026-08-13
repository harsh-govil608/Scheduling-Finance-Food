package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Investments (Future) PRD, Phase 3 Doc 23: the PRD explicitly scopes itself to "a minimal,
 * manually entered, read-only holdings feature" with brokerage sync, tax-lot tracking, and
 * advice all explicitly named as deferred within the document itself - so this minimal model
 * WAS the PRD's own completion bar, not a corner cut relative to it.
 *
 * Mutual-fund NAV sync (2026-08, real user request: "the investment option needs improvements,
 * mostly sync option") extends this rather than replacing it - a MANUAL holding (the original,
 * still-supported shape) behaves exactly as before: `currentValue` is whatever the user last
 * typed, `type`/`schemeCode`/`units`/`lastNavUpdatedAt` stay at their defaults/null. A
 * MUTUAL_FUND holding instead has `currentValue` computed and kept in sync by
 * InvestmentSyncTracker (`units * that scheme's latest AMFI NAV`) - the user never edits
 * `currentValue` directly for this type.
 */
enum class InvestmentType { MANUAL, MUTUAL_FUND }

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val currentValue: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val type: InvestmentType = InvestmentType.MANUAL,
    /** AMFI scheme code - MUTUAL_FUND only, null for MANUAL. Stable identifier used to re-match
     * this holding against AmfiNavRepository's daily NAV list on every sync. */
    val schemeCode: String? = null,
    /** Units held - MUTUAL_FUND only, null for MANUAL. */
    val units: Double? = null,
    val lastNavUpdatedAt: Long? = null
)
