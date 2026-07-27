package com.lifeos.expensecapture.notifications

import android.content.Context
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Pre-beta hardening (Priority 5 - architecture): split out of NotificationCheckWorker (see
 * FinanceNotificationChecks' kdoc for the full rationale). This is the Home-pillar subset - Tasks,
 * Habits, and Goals - the same grouping the app's own pillars use. No behavior change - every
 * function body is unchanged, just relocated.
 */
object ProductivityNotificationChecks {

    private const val TASK_DUE_SOON_WINDOW_MILLIS = 3L * 60 * 60 * 1000 // 3h - a genuine heads-up, not just "it's late"
    private const val MIN_COMPLETIONS_FOR_RHYTHM = 3
    private const val MIN_GAP_DAYS_FOR_RISK_CHECK = 2.0 // below this it's daily-ish - checkHabits already owns that
    private const val RISK_GAP_MULTIPLIER = 1.5
    private const val GOAL_ON_TRACK_RATIO = 0.7 // pace below 70% of what's needed counts as "off track"

    /** Smart Reminders PRD (Phase 3 Doc 09), scoped to exactly the one thing Task Management
     * (Doc 10) named as its own dependency: a reminder when a task's due date arrives. No
     * AI-driven timing/re-prioritization - a task is due when its dueDate says so, checked here
     * the same cooldown-gated way as every other notification source. */
    suspend fun checkTasks(context: Context, db: AppDatabase) {
        val now = System.currentTimeMillis()
        val tasks = db.taskDao().observeAll().first()
        for (task in tasks) {
            if (task.completed) continue
            val dueDate = task.dueDate ?: continue
            if (dueDate > now) continue
            val route = "tasks"
            val cooldownKey = route + task.id
            if (NotificationSender.recentlyNotified(db, NotificationType.TASK_DUE, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.TASK_DUE,
                title = "Task due: ${task.title}",
                body = "This was due - mark it done or reschedule",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /** "Reminders everywhere": a heads-up a few hours before a task is due, not just once it's
     * already late (checkTasks above). Its own NotificationType/cooldown so it can fire once as
     * a heads-up and, separately, once again later if it actually goes overdue. */
    suspend fun checkTasksDueSoon(context: Context, db: AppDatabase) {
        val now = System.currentTimeMillis()
        val windowEnd = now + TASK_DUE_SOON_WINDOW_MILLIS
        val tasks = db.taskDao().observeAll().first()
        for (task in tasks) {
            if (task.completed) continue
            val dueDate = task.dueDate ?: continue
            if (dueDate <= now || dueDate > windowEnd) continue
            val route = "tasks"
            val cooldownKey = "soon" + task.id
            if (NotificationSender.recentlyNotified(db, NotificationType.TASK_DUE_SOON, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.TASK_DUE_SOON,
                title = "Coming up: ${task.title}",
                body = "Due in the next few hours",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /**
     * The gap directly named in the "make Home proactive, not lazy" conversation: Tasks got a
     * Smart Reminder above, but Habits had zero proactive nudge at all - a user who forgets to
     * open the app just silently never checks a habit off. Bundled into ONE evening notification
     * per day (not one per habit) rather than per-item, both to avoid spamming and per the
     * Habits PRD's (Doc 13) own Notification Behaviors requirement that recovery/reminder
     * messaging "must not escalate into a nagging sequence" - a single gentle daily mention,
     * never repeated per-habit, never guilt-toned (see HabitsViewModel's kdoc for the same
     * "supportive, not punitive" rule applied here to the notification copy itself).
     */
    suspend fun checkHabits(context: Context, db: AppDatabase) {
        if (LocalTime.now().hour < 18) return
        val habits = db.habitDao().observeAll().first()
        if (habits.isEmpty()) return

        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val completions = db.habitCompletionDao().observeAll().first()
        val doneTodayIds = completions.filter { it.dateEpochDay == today }.map { it.habitId }.toSet()
        val pending = habits.filter { it.id !in doneTodayIds }
        if (pending.isEmpty()) return

        val route = "habits"
        if (NotificationSender.recentlyNotified(db, NotificationType.HABIT_REMINDER, route)) return

        NotificationSender.notify(
            context = context,
            type = NotificationType.HABIT_REMINDER,
            title = "A few habits still open today",
            body = "${pending.joinToString(", ") { it.name }} - no rush, just a reminder",
            route = route,
            cooldownKey = route
        )
    }

    /**
     * AI Transformation Plan, forward-looking predictions: checkHabits above only ever asks "was
     * today's box checked" - fine for daily habits, but silent for a habit with its own multi-day
     * rhythm (gym every 2-3 days, calling family weekly). This looks at each habit's own history
     * of check-ins, computes its typical gap, and flags one that's run meaningfully past its own
     * pattern - same "supportive, never punitive" copy discipline as HABIT_REMINDER and
     * HabitsViewModel's streak logic, never framed as a failure.
     */
    suspend fun checkHabitsAtRisk(context: Context, db: AppDatabase) {
        val habits = db.habitDao().observeAll().first()
        if (habits.isEmpty()) return
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val completionsByHabit = db.habitCompletionDao().observeAll().first().groupBy { it.habitId }

        for (habit in habits) {
            val days = completionsByHabit[habit.id]?.map { it.dateEpochDay }?.sorted() ?: continue
            if (days.size < MIN_COMPLETIONS_FOR_RHYTHM) continue

            val averageGapDays = days.zipWithNext { a, b -> (b - a).toDouble() }.average()
            if (averageGapDays < MIN_GAP_DAYS_FOR_RISK_CHECK) continue // daily-ish - checkHabits already covers this
            val daysSinceLast = today - days.last()
            if (daysSinceLast < averageGapDays * RISK_GAP_MULTIPLIER) continue

            val route = "habits"
            val cooldownKey = "risk" + habit.id
            if (NotificationSender.recentlyNotified(db, NotificationType.HABIT_AT_RISK, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.HABIT_AT_RISK,
                title = "\"${habit.name}\" might be due for a check-in",
                body = "You usually come back to this every ~${averageGapDays.toInt()} days - it's been $daysSinceLast. No pressure, just a nudge.",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /**
     * Closes the loop on the Goal targetAmount field added for the Spending Insight card's
     * goal-acceleration line: that line only ever appears passively, inside a Finance insight
     * that may not fire this month. This proactively flags a goal on its own, on a schedule,
     * when the current savings pace genuinely won't reach the target amount by its target date -
     * same "current monthly pace from net cash flow" estimate as SpendingInsightEngine's own
     * goal-acceleration math (kept as its own small copy here rather than a shared extraction -
     * both are a handful of lines, not worth the indirection for two call sites - see the
     * reliability review's note on this duplication). Supportive framing - "worth a check-in,"
     * never a countdown to failure.
     */
    suspend fun checkGoalsOffTrack(context: Context, db: AppDatabase) {
        val goals = db.goalDao().observeAll().first()
            .filter { !it.completed && (it.targetAmount ?: 0.0) > 0.0 && it.targetDate != null }
        if (goals.isEmpty()) return

        val now = System.currentTimeMillis()
        val monthStart = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val daysElapsed = ((now - monthStart) / 86_400_000.0).coerceAtLeast(1.0)
        val thisMonthTxns = db.transactionDao().getSince(monthStart)
        val credits = thisMonthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
        val debits = thisMonthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        val currentMonthlyPace = ((credits - debits) / daysElapsed) * 30.0

        for (goal in goals) {
            val targetAmount = goal.targetAmount ?: continue
            val targetDate = goal.targetDate ?: continue
            if (targetDate <= now) continue // already past due - a different concern than "off track"

            val monthsRemaining = (targetDate - now) / (30.0 * 86_400_000L)
            val requiredMonthlyPace = targetAmount / monthsRemaining
            if (currentMonthlyPace >= requiredMonthlyPace * GOAL_ON_TRACK_RATIO) continue

            val route = "goals"
            val cooldownKey = "offtrack" + goal.id
            if (NotificationSender.recentlyNotified(db, NotificationType.GOAL_OFF_TRACK, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.GOAL_OFF_TRACK,
                title = "\"${goal.title}\" might need a closer look",
                body = "At your current saving pace, this could be tight to reach by its target date - worth a check-in, no pressure.",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }
}
