package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goals PRD, Phase 3 Doc 12, scoped to manual target-tracking only: a title, an optional target
 * date, and a completed flag the user sets themselves. The full PRD's AI-suggested goals,
 * automatic progress inference from other pillars' data, and habit-to-goal linkage are all out
 * of scope - no AI/ML anywhere in this app, and linking to Habits would need a many-to-many
 * relation this pilot doesn't need yet.
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDate: Long? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** AI Transformation Plan spending-insight card: a real rupee target (e.g. a house down
     * payment) is what makes "cutting X/day gets you there N months sooner" an honest, computed
     * number instead of a made-up one. Optional - a goal with no target amount just never appears
     * in that projection. */
    val targetAmount: Double? = null
)
