package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType { NOTE, JOURNAL }

/**
 * Notes PRD (Phase 3 Doc 37) and Journal PRD (Doc 38) share one entity distinguished by `type` -
 * both are structurally identical (a title, a body, a timestamp), the same consolidation
 * reasoning already used for Subscriptions/Bills (Day 2) and Weekly/Monthly Review (Day 4).
 * Scoped to plain CRUD: the full PRDs' AI-assisted tagging/search and cross-referencing between
 * entries are out of scope - no AI/ML anywhere in this app.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: NoteType,
    val title: String = "",
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
