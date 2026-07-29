package com.lifeos.expensecapture.productivity

/**
 * Extracted from HabitsViewModel (found via a real user request, 2026-07): surfacing the best
 * current streak on Home as an engagement hook needed this exact calculation at a second call
 * site. Kept as one shared function rather than a duplicate specifically because of what it
 * encodes - the Habits PRD calls this streak model one of its highest-risk-to-get-wrong pieces
 * (never reset-to-zero framing, never shame-coded), and a second hand-copied version is exactly
 * how two surfaces quietly drift apart on that behavior over time.
 *
 * Counts backward from today (or from yesterday, if today just hasn't happened yet - that's not
 * a miss, the day isn't over). Only actually broken once a full day passes with no completion at
 * all, at which point this returns 0 - the "ready when you are" copy lives in each caller's UI
 * layer, not here, since a streak COUNT can be honest while the FRAMING around a 0 stays
 * supportive.
 */
object HabitStreakCalculator {
    fun currentStreak(days: Set<Long>, today: Long): Int {
        if (days.isEmpty()) return 0
        val mostRecent = days.max()
        if (mostRecent < today - 1) return 0
        var streak = 0
        var day = if (days.contains(today)) today else today - 1
        while (days.contains(day)) {
            streak++
            day--
        }
        return streak
    }
}
