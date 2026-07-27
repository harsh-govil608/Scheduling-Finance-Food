package com.lifeos.expensecapture.productivity

import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Pre-beta hardening: the "never celebrate a decline" rule here is a direct product requirement
 * (same "supportive, never punitive" discipline as HabitsViewModel's streak copy) - a regression
 * that silently started narrating a worse week as if it were good news would be a real product
 * bug, not just a cosmetic one, so that behavior gets its own explicit test below.
 */
class ProductivityInsightEngineTest {

    private val zone = ZoneId.systemDefault()

    // A fixed Wednesday so "this week" / "last week" boundaries are stable across runs.
    private val today = LocalDate.of(2026, 3, 18)
    private val now = today.atTime(LocalTime.NOON).atZone(zone).toInstant().toEpochMilli()
    private val weekStartMillis = today.minusDays((today.dayOfWeek.value - 1).toLong())
        .atStartOfDay(zone).toInstant().toEpochMilli()
    private val lastWeekStartMillis = weekStartMillis - 7L * 86_400_000L

    private fun epochDay(daysAgo: Long): Long = today.minusDays(daysAgo).toEpochDay()

    private fun completedTask(id: Long, completedAt: Long) = TaskEntity(
        id = id, title = "Task $id", completed = true, completedAt = completedAt
    )

    @Test
    fun `returns null when there is no habit or task signal at all`() {
        val result = ProductivityInsightEngine.compute(emptyList(), emptyList(), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `does not celebrate a streak below the minimum length`() {
        val habit = HabitEntity(id = 1, name = "Drink water")
        val completions = listOf(
            HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(0)),
            HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(1))
        )
        val result = ProductivityInsightEngine.compute(emptyList(), listOf(habit), completions, now)
        assertNull(result)
    }

    @Test
    fun `celebrates a real active streak at or above the minimum length`() {
        val habit = HabitEntity(id = 1, name = "Drink water")
        val completions = (0..3L).map { HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(it)) }

        val result = ProductivityInsightEngine.compute(emptyList(), listOf(habit), completions, now)

        requireNotNull(result)
        assertTrue(result.contains("Drink water"))
        assertTrue(result.contains("4-day streak"))
    }

    @Test
    fun `does not celebrate a streak that has actually lapsed`() {
        val habit = HabitEntity(id = 1, name = "Gym")
        // A long-ago streak of 5 days, but nothing done for the last 3 days - genuinely lapsed.
        val completions = (3..7L).map { HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(it)) }

        val result = ProductivityInsightEngine.compute(emptyList(), listOf(habit), completions, now)
        assertNull(result)
    }

    @Test
    fun `picks the longer of two active streaks`() {
        val habits = listOf(HabitEntity(id = 1, name = "Short"), HabitEntity(id = 2, name = "Long"))
        val completions = (0..2L).map { HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(it)) } +
            (0..5L).map { HabitCompletionEntity(habitId = 2, dateEpochDay = epochDay(it)) }

        val result = ProductivityInsightEngine.compute(emptyList(), habits, completions, now)

        requireNotNull(result)
        assertTrue(result.contains("Long"))
        assertTrue(result.contains("6-day streak"))
    }

    @Test
    fun `stays silent on task momentum below the minimum when there was no prior week`() {
        val tasks = listOf(completedTask(1, weekStartMillis + 1000))
        val result = ProductivityInsightEngine.compute(tasks, emptyList(), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `reports good momentum when enough tasks completed with no prior week to compare`() {
        val tasks = listOf(
            completedTask(1, weekStartMillis + 1000),
            completedTask(2, weekStartMillis + 2000)
        )
        val result = ProductivityInsightEngine.compute(tasks, emptyList(), emptyList(), now)

        requireNotNull(result)
        assertTrue(result.contains("2 tasks"))
    }

    @Test
    fun `reports an increase over last week when this week is genuinely higher`() {
        val tasks = listOf(
            completedTask(1, weekStartMillis + 1000),
            completedTask(2, weekStartMillis + 2000),
            completedTask(3, weekStartMillis + 3000),
            completedTask(4, lastWeekStartMillis + 1000)
        )
        val result = ProductivityInsightEngine.compute(tasks, emptyList(), emptyList(), now)

        requireNotNull(result)
        assertTrue(result.contains("3 task"))
        assertTrue(result.contains("1 last week"))
    }

    @Test
    fun `never narrates a decline versus last week`() {
        val tasks = listOf(
            completedTask(1, weekStartMillis + 1000), // 1 this week
            completedTask(2, lastWeekStartMillis + 1000),
            completedTask(3, lastWeekStartMillis + 2000),
            completedTask(4, lastWeekStartMillis + 3000) // 3 last week - a real decline
        )
        val result = ProductivityInsightEngine.compute(tasks, emptyList(), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `combines a streak line and a task momentum line when both are true`() {
        val habit = HabitEntity(id = 1, name = "Mandir")
        val completions = (0..2L).map { HabitCompletionEntity(habitId = 1, dateEpochDay = epochDay(it)) }
        val tasks = listOf(
            completedTask(1, weekStartMillis + 1000),
            completedTask(2, weekStartMillis + 2000)
        )

        val result = ProductivityInsightEngine.compute(tasks, listOf(habit), completions, now)

        requireNotNull(result)
        assertTrue(result.contains("Mandir"))
        assertTrue(result.contains("2 tasks"))
    }

    @Test
    fun `a habit with zero completions ever produces no streak line`() {
        val habit = HabitEntity(id = 1, name = "New habit")
        val result = ProductivityInsightEngine.compute(emptyList(), listOf(habit), emptyList(), now)
        assertNull(result)
    }
}
