package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Habits PRD, Phase 3 Doc 13, scoped down: the full PRD calls for AI-assisted target
 * calibration, flexibility windows, goal linkage, and an adaptive coaching loop - none of which
 * exist without ML or a Goals feature to link against (Goals PRD, Doc 12, isn't built either).
 * What's kept is the PRD's own load-bearing requirement, called out explicitly as one of its
 * highest-risk areas: the streak/momentum model must be "supportive," never "reset-to-zero"
 * framing or shame-coded copy for a missed day. See HabitsViewModel for where that's enforced -
 * a missed day is computed and displayed as "resumable," never as a broken streak.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)
