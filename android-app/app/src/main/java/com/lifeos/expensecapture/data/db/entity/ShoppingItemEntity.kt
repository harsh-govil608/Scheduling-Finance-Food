package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Shopping PRD, Phase 3 Doc 36, scoped to a plain checklist - the full PRD's price tracking,
 * store-linked suggestions, and Finance-pillar spend correlation are all out of scope for now
 * (the last one is a genuinely interesting future Context Timeline-style connection, not
 * something to build speculatively before there's a reason to).
 */
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String = "",
    val checked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
