package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Projects PRD, Phase 3 Doc 11, scoped to its narrowest useful form: a named group tasks can be
 * tagged into. The full PRD's dependency graphs between tasks, milestone tracking, and
 * project-level AI progress synthesis are all out of scope - no AI/ML anywhere in this app, and
 * a dependency graph needs a real graph data structure and UI this pilot doesn't need yet.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)
