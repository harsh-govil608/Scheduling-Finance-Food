package com.lifeos.expensecapture.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.MainActivity
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Notification Center PRD (Phase 3 Doc 03) + the Notification Behaviors sections of Doc 19
 * (Subscription Manager), Doc 20 (Budget Planner), Doc 22 (Bills), Doc 09 (Smart Reminders for
 * tasks), and Doc 13 (Habits' daily reminder). Deliberately simple: checks bills/subscriptions/
 * budgets/tasks/habits/night-summary-readiness on a schedule (and once whenever the app opens),
 * cooldown-gated per item so nothing re-notifies more than roughly once a day.
 *
 * There is no arbitration engine here - Phase 2's Notification System (Doc 14) was never
 * built as code, so this is a direct per-source check, not a shared interruption-budget
 * system weighing competing pillar alerts against each other. See day-2.md.
 *
 * Also runs SmsHistoryScanner's catch-up pass first (see its kdoc for why that matters) - this
 * worker was already the one thing guaranteed to run periodically regardless of whether the user
 * opens the app, which makes it the natural place for that safety net.
 */
class NotificationCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val COOLDOWN_MILLIS = 20L * 60 * 60 * 1000 // ~20h: at most once/day per item

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<NotificationCheckWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notification_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun runOnce(context: Context) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<NotificationCheckWorker>().build())
        }
    }

    override suspend fun doWork(): Result {
        // Safety net for the SmsHistoryScanner interruption class of bug (see its kdoc): every
        // periodic run and every Home open (runOnce is called from both) also catches up on any
        // SMS the live receiver path might have missed, instead of relying solely on the
        // one-shot onboarding scan ever finishing in one pass.
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasSmsPermission) {
            SmsHistoryScanner.scanIfNeeded(applicationContext)
        }

        val db = AppDatabase.getInstance(applicationContext)
        val insights = FinanceInsightsRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao(),
            subscriptionDao = db.subscriptionDao(),
            billDao = db.billDao()
        )
        insights.refreshRecurringDetection()

        checkBills(db, insights)
        checkSubscriptions(db, insights)
        checkBudgets(db, insights)
        checkTasks(db)
        checkHabits(db)
        checkNightSummary(db)

        return Result.success()
    }

    private suspend fun checkBills(db: AppDatabase, insights: FinanceInsightsRepository) {
        val bills = insights.observeBills().first()
        for (item in bills) {
            val dueSoon = item.displayStatus == FinanceInsightsRepository.BillDisplayStatus.DUE_TODAY ||
                item.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE
            if (!dueSoon) continue
            val route = "bills"
            if (recentlyNotified(db, NotificationType.BILL_DUE, route + item.bill.id)) continue

            notify(
                type = NotificationType.BILL_DUE,
                title = "${item.bill.payeeDisplay} is due",
                body = "~₹${"%.2f".format(item.bill.typicalAmount)}, usually around day ${item.bill.dueDayOfMonth}",
                route = route,
                cooldownKey = route + item.bill.id
            )
        }
    }

    private suspend fun checkSubscriptions(db: AppDatabase, insights: FinanceInsightsRepository) {
        val subs = insights.observeSubscriptions().first()
        for (item in subs) {
            if (item.displayStatus != FinanceInsightsRepository.SubscriptionDisplayStatus.RENEWAL_UPCOMING) continue
            val route = "subscriptions"
            val cooldownKey = route + item.subscription.id
            if (recentlyNotified(db, NotificationType.SUBSCRIPTION_RENEWAL, cooldownKey)) continue

            notify(
                type = NotificationType.SUBSCRIPTION_RENEWAL,
                title = "${item.subscription.merchantDisplay} renews soon",
                body = "₹${"%.2f".format(item.subscription.amount)} expected around this time",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    private suspend fun checkBudgets(db: AppDatabase, insights: FinanceInsightsRepository) {
        val budgets = insights.observeBudgetProgress().first()
        for (progress in budgets) {
            if (progress.spentThisMonth <= progress.budget.monthlyLimit) continue
            val route = "budgets"
            val cooldownKey = route + progress.budget.id
            if (recentlyNotified(db, NotificationType.BUDGET_OVER_LIMIT, cooldownKey)) continue

            notify(
                type = NotificationType.BUDGET_OVER_LIMIT,
                title = "${progress.categoryName} is over budget",
                body = "₹${"%.2f".format(progress.spentThisMonth)} spent of a ₹${"%.2f".format(progress.budget.monthlyLimit)} limit this month",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /** Smart Reminders PRD (Phase 3 Doc 09), scoped to exactly the one thing Task Management
     * (Doc 10) named as its own dependency: a reminder when a task's due date arrives. No
     * AI-driven timing/re-prioritization - a task is due when its dueDate says so, checked here
     * the same cooldown-gated way as every other notification source. */
    private suspend fun checkTasks(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val tasks = db.taskDao().observeAll().first()
        for (task in tasks) {
            if (task.completed) continue
            val dueDate = task.dueDate ?: continue
            if (dueDate > now) continue
            val route = "tasks"
            val cooldownKey = route + task.id
            if (recentlyNotified(db, NotificationType.TASK_DUE, cooldownKey)) continue

            notify(
                type = NotificationType.TASK_DUE,
                title = "Task due: ${task.title}",
                body = "This was due - mark it done or reschedule",
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
    private suspend fun checkHabits(db: AppDatabase) {
        if (LocalTime.now().hour < 18) return
        val habits = db.habitDao().observeAll().first()
        if (habits.isEmpty()) return

        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val completions = db.habitCompletionDao().observeAll().first()
        val doneTodayIds = completions.filter { it.dateEpochDay == today }.map { it.habitId }.toSet()
        val pending = habits.filter { it.id !in doneTodayIds }
        if (pending.isEmpty()) return

        val route = "habits"
        if (recentlyNotified(db, NotificationType.HABIT_REMINDER, route)) return

        notify(
            type = NotificationType.HABIT_REMINDER,
            title = "A few habits still open today",
            body = "${pending.joinToString(", ") { it.name }} - no rush, just a reminder",
            route = route,
            cooldownKey = route
        )
    }

    /** Night Summary PRD (Phase 3 Doc 02): a low-priority notification once per day, after a
     * fixed evening hour, pointing at the recap - not an arbitrated pillar alert. */
    private suspend fun checkNightSummary(db: AppDatabase) {
        if (LocalTime.now().hour < 20) return
        val route = "night_summary"
        if (recentlyNotified(db, NotificationType.NIGHT_SUMMARY_READY, route)) return

        notify(
            type = NotificationType.NIGHT_SUMMARY_READY,
            title = "Your day in review is ready",
            body = "See what got captured today and what's coming up tomorrow",
            route = route,
            cooldownKey = route,
            channel = NotificationChannels.SUMMARY
        )
    }

    private suspend fun recentlyNotified(db: AppDatabase, type: NotificationType, cooldownKey: String): Boolean {
        val since = System.currentTimeMillis() - COOLDOWN_MILLIS
        return db.notificationDao().countRecent(type, cooldownKey, since) > 0
    }

    private suspend fun notify(
        type: NotificationType,
        title: String,
        body: String,
        route: String,
        cooldownKey: String,
        channel: String = NotificationChannels.REMINDERS
    ) {
        val db = AppDatabase.getInstance(applicationContext)
        db.notificationDao().insert(
            NotificationEntity(type = type, title = title, body = body, deepLinkRoute = route, sourceKey = cooldownKey)
        )

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return // Recorded in the Center regardless; system push just skipped.

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, cooldownKey.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(cooldownKey.hashCode(), notification)
    }
}
