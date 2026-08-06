package com.lifeos.expensecapture.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.entity.NotificationType
import java.util.concurrent.TimeUnit

/**
 * Real user request (2026-08): "5 hours reminder notification" - a plain "log anything recently?"
 * check-in every 5 hours, unlike everything in [NotificationCheckWorker] (bills/budgets/tasks/
 * habits), which only fires when some real data condition is actually true. This is genuinely
 * periodic - the 5-hour WorkManager interval itself is the cadence, so unlike every other
 * notification in this app it deliberately does NOT call [NotificationSender.recentlyNotified]
 * first; it always fires on schedule (subject to the same Doze/battery deferral every
 * PeriodicWorkRequest is, exactly like [NotificationCheckWorker]'s own periodic job).
 *
 * A separate worker/schedule rather than folded into NotificationCheckWorker's existing 2-hour
 * job, since the two have fundamentally different triggering logic (conditional vs. always-fire)
 * and different intervals - conflating them would mean either this reminder firing every 2 hours
 * (not the requested 5) or every other check waiting 5 hours between passes.
 */
class PeriodicReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "periodic_checkin_reminder"

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<PeriodicReminderWorker>(5, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        NotificationSender.notify(
            context = applicationContext,
            type = NotificationType.PERIODIC_CHECK_IN,
            title = "Quick check-in",
            body = "Anything to log from the last few hours - an expense, a task, a habit?",
            route = "home",
            // Timestamp-based, not WorkRequest.id - a PeriodicWorkRequest keeps the same id
            // across every one of its periodic firings, which would collide here. Not that it
            // matters functionally (this type has no cooldown check to look sourceKey up for),
            // but each firing should still read as its own distinct record.
            cooldownKey = "periodic_checkin_${System.currentTimeMillis()}"
        )
        return Result.success()
    }
}
