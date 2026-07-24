package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per calendar day a habit was marked done. `dateEpochDay` is `LocalDate.toEpochDay()`,
 * not an epoch-millis timestamp - habits are tracked per calendar day, not per instant, so this
 * avoids timezone/time-of-day edge cases when computing streaks. The unique index makes marking
 * the same habit done twice in one day a no-op instead of a duplicate row (same reasoning as
 * TransactionEntity's sourceHash - see docs/coders-documentation/day-3.md).
 */
@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "dateEpochDay"], unique = true)]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long
)
