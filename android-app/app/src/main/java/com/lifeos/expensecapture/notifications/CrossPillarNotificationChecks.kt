package com.lifeos.expensecapture.notifications

import android.content.Context
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Pre-beta hardening (Priority 5 - architecture): split out of NotificationCheckWorker (see
 * FinanceNotificationChecks' kdoc for the full rationale). These two checks don't belong to
 * either single pillar - they summarize across both - so they get their own file rather than
 * being forced into Finance's or Productivity's. No behavior change - every function body is
 * unchanged, just relocated.
 */
object CrossPillarNotificationChecks {

    private const val MORNING_WINDOW_START_HOUR = 7
    private const val MORNING_WINDOW_END_HOUR = 9 // inclusive; wide enough that a 2h worker cadence can't skip it entirely

    /** Night Summary PRD (Phase 3 Doc 02): a low-priority notification once per day, after a
     * fixed evening hour, pointing at the recap - not an arbitrated pillar alert. */
    suspend fun checkNightSummary(context: Context, db: AppDatabase) {
        if (LocalTime.now().hour < 20) return
        val route = "night_summary"
        if (NotificationSender.recentlyNotified(db, NotificationType.NIGHT_SUMMARY_READY, route)) return

        NotificationSender.notify(
            context = context,
            type = NotificationType.NIGHT_SUMMARY_READY,
            title = "Your day in review is ready",
            body = "See what got captured today and what's coming up tomorrow",
            route = route,
            cooldownKey = route,
            channel = NotificationChannels.SUMMARY
        )
    }

    /**
     * "Reminders everywhere": a single proactive push in the early morning summarizing the day
     * ahead - unlike the in-app Morning Briefing card (which only shows if the user happens to
     * open the app), this fires on its own. Only sent when there's a genuine reason to (a due
     * task, a habit, a bill) - a quiet day stays quiet rather than pushing a manufactured
     * "good morning, nothing's up" notification.
     */
    suspend fun checkMorningHeadsUp(context: Context, db: AppDatabase, insights: FinanceInsightsRepository) {
        val hour = LocalTime.now().hour
        if (hour !in MORNING_WINDOW_START_HOUR..MORNING_WINDOW_END_HOUR) return
        val route = "productivity_home"
        if (NotificationSender.recentlyNotified(db, NotificationType.MORNING_HEADSUP, route)) return

        val zone = ZoneId.systemDefault()
        val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val dueTodayTasks = db.taskDao().observeAll().first()
            .filter { !it.completed && it.dueDate != null && it.dueDate < endOfToday }

        val today = LocalDate.now(zone).toEpochDay()
        val habits = db.habitDao().observeAll().first()
        val doneTodayIds = db.habitCompletionDao().observeAll().first()
            .filter { it.dateEpochDay == today }.map { it.habitId }.toSet()
        val pendingHabits = habits.filter { it.id !in doneTodayIds }

        val billsDueSoon = insights.observeBills().first().filter {
            it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.DUE_TODAY ||
                it.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE
        }

        if (dueTodayTasks.isEmpty() && pendingHabits.isEmpty() && billsDueSoon.isEmpty()) return

        val parts = mutableListOf<String>()
        if (dueTodayTasks.isNotEmpty()) parts += "${dueTodayTasks.size} task${if (dueTodayTasks.size == 1) "" else "s"} due today"
        if (billsDueSoon.isNotEmpty()) parts += "${billsDueSoon.size} bill${if (billsDueSoon.size == 1) "" else "s"} due"
        if (pendingHabits.isNotEmpty()) parts += "${pendingHabits.size} habit${if (pendingHabits.size == 1) "" else "s"} to keep up"

        NotificationSender.notify(
            context = context,
            type = NotificationType.MORNING_HEADSUP,
            title = "Your day ahead",
            body = parts.joinToString(", ").replaceFirstChar { it.uppercase() },
            route = route,
            cooldownKey = route
        )
    }
}
