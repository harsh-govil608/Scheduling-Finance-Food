package com.lifeos.expensecapture.productivity

import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import java.time.Instant
import java.time.ZoneId

/**
 * The Home-pillar counterpart to SpendingInsightEngine - the same "narrative synthesis of real
 * numbers, not a chat box" idea, applied to tasks/habits instead of transactions. Rule-based, no
 * model, same as every other insight in this app.
 *
 * Only ever celebrates, never scolds: HabitsViewModel's own streak logic is explicitly
 * "supportive, never punitive" (see its kdoc), and that discipline extends here - a worse task
 * week than last week stays silent rather than being narrated as a decline. There's always a
 * "say nothing" option; a quiet week isn't a failure to report on.
 */
object ProductivityInsightEngine {

    private const val MIN_STREAK_TO_CELEBRATE = 3
    private const val MIN_TASKS_FOR_MOMENTUM_MENTION = 2

    fun compute(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        now: Long = System.currentTimeMillis()
    ): String? {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val todayEpochDay = today.toEpochDay()

        val completionsByHabit = completions.groupBy { it.habitId }
        val bestStreak = habits
            .mapNotNull { habit ->
                val days = completionsByHabit[habit.id]?.map { it.dateEpochDay }?.toSet() ?: emptySet()
                val streak = currentStreak(days, todayEpochDay)
                if (streak >= MIN_STREAK_TO_CELEBRATE) habit.name to streak else null
            }
            .maxByOrNull { (_, streak) -> streak }

        val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val lastWeekStart = weekStart - 7L * 86_400_000L

        val completedThisWeek = tasks.count { it.completed && (it.completedAt ?: 0L) >= weekStart }
        val completedLastWeek = tasks.count { it.completed && (it.completedAt ?: 0L) in lastWeekStart until weekStart }

        val taskMomentumLine = when {
            completedLastWeek > 0 && completedThisWeek > completedLastWeek ->
                "You've completed $completedThisWeek task${if (completedThisWeek == 1) "" else "s"} this week, up from $completedLastWeek last week."
            completedLastWeek == 0 && completedThisWeek >= MIN_TASKS_FOR_MOMENTUM_MENTION ->
                "You've completed $completedThisWeek tasks this week - good momentum."
            else -> null
        }

        val parts = listOfNotNull(
            bestStreak?.let { (name, streak) -> "\"$name\" is at a $streak-day streak - your longest active one right now." },
            taskMomentumLine
        )
        return if (parts.isEmpty()) null else parts.joinToString(" ")
    }

    /** Same algorithm as HabitsViewModel.currentStreak - kept as its own small copy rather than a
     * shared utility, since HabitsViewModel's version is private to that screen's own UI-facing
     * HabitRow model and forcing a shared extraction for one reuse isn't worth the indirection. */
    private fun currentStreak(days: Set<Long>, today: Long): Int {
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
