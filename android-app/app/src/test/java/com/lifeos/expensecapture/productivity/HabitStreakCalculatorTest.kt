package com.lifeos.expensecapture.productivity

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitStreakCalculatorTest {

    private val today = 20000L // arbitrary fixed epoch-day

    @Test
    fun `no completions at all is a zero streak`() {
        assertEquals(0, HabitStreakCalculator.currentStreak(emptySet(), today))
    }

    @Test
    fun `consecutive days through today count the full run`() {
        val days = setOf(today, today - 1, today - 2)
        assertEquals(3, HabitStreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `today not done yet still counts yesterday's run - the day isn't over`() {
        val days = setOf(today - 1, today - 2, today - 3)
        assertEquals(3, HabitStreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `a full missed day resets to zero, not a negative or stale count`() {
        val days = setOf(today - 3, today - 4, today - 5)
        assertEquals(0, HabitStreakCalculator.currentStreak(days, today))
    }

    @Test
    fun `a gap in the middle of history does not extend the current run`() {
        val days = setOf(today, today - 1, today - 5, today - 6)
        assertEquals(2, HabitStreakCalculator.currentStreak(days, today))
    }
}
