package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity

/**
 * Permissions & Consent PRD (Phase 3 Doc 41): "the consent record (state per permission per
 * user, queryable by any pillar)." permissionType is the primary key so each permission has
 * exactly one current record - re-requesting overwrites rather than appending, since only the
 * current state matters for gating, not a full history.
 */
@Entity(tableName = "consents", primaryKeys = ["permissionType"])
data class ConsentEntity(
    val permissionType: String,
    val granted: Boolean,
    val decidedAt: Long = System.currentTimeMillis()
)
