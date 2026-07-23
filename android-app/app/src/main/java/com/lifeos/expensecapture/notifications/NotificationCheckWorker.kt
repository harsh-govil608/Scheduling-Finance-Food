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
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Notification Center PRD (Phase 3 Doc 03) + the Notification Behaviors sections of Doc 19
 * (Subscription Manager), Doc 20 (Budget Planner), and Doc 22 (Bills). Deliberately simple:
 * checks bills/subscriptions/budgets/night-summary-readiness on a schedule (and once whenever
 * the app opens), cooldown-gated per item so nothing re-notifies more than roughly once a day.
 *
 * There is no arbitration engine here - Phase 2's Notification System (Doc 14) was never
 * built as code, so this is a direct per-source check, not a shared interruption-budget
 * system weighing competing pillar alerts against each other. See day-2.md.
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
