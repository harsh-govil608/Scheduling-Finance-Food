package com.lifeos.expensecapture.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 *
 * Pre-beta hardening (Priority 5 - architecture): this file used to also contain all 12 of the
 * individual check functions (554 lines total). They're now grouped by pillar in
 * FinanceNotificationChecks, ProductivityNotificationChecks, and CrossPillarNotificationChecks -
 * this class is left as the orchestrator: schedule/trigger the worker, hold the concurrency guard,
 * and call each check in turn. No behavior change - every check still runs in the same order,
 * under the same Mutex, with the same per-check try/catch isolation.
 */
class NotificationCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        /**
         * Pre-beta hardening (Priority 4 - reliability, race condition): `runOnce()` used to
         * enqueue a plain, unnamed one-time work request every single time - every HomeViewModel
         * construction (which happens on every Home-tab open, including rapid tab-switching or a
         * config change recomposing the screen) fired a brand new one, free to run fully
         * concurrently with any other in-flight execution. That's a real check-then-act race:
         * two concurrent runs of syncBillTasks could both see "no task for this bill yet" before
         * either commits its insert, producing a genuine duplicate task. Named + KEEP means a
         * trigger that arrives while one is already pending/running is simply dropped - the
         * check still happens soon, just not redundantly N times over.
         */
        private const val ONE_TIME_WORK_NAME = "notification_check_once"

        /** A second, cheaper layer than WorkManager naming alone: this survives even the rarer
         * case of the periodic worker and a run-once request happening to execute at the same
         * moment (they're different unique-work names, so WorkManager alone won't serialize
         * them). Held for doWork()'s entire body - a second execution simply waits, and by the
         * time it runs, the first's writes are already committed, so its own idempotency checks
         * (findLatestForBill, recentlyNotified) correctly see "already handled" instead of racing. */
        private val executionMutex = Mutex()

        fun schedulePeriodic(context: Context) {
            // "Reminders everywhere," not a daily batch: 6h -> 2h, the closest a periodic
            // WorkManager job (min 15 min, subject to Doze/battery deferral regardless of the
            // interval chosen) gets to feeling continuous without draining the battery for it.
            val request = PeriodicWorkRequestBuilder<NotificationCheckWorker>(2, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notification_check",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun runOnce(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<NotificationCheckWorker>().build()
            )
        }
    }

    override suspend fun doWork(): Result = executionMutex.withLock {
        // Pre-beta hardening (Priority 4 - reliability): every check below used to run as one
        // unbroken sequence of suspend calls - a single exception anywhere (say, checkBills
        // hitting a null it didn't expect) silently aborted every check after it in that pass,
        // with zero record of what happened. WorkManager catches the resulting failure internally
        // (it doesn't crash the app), but the practical effect was identical: half the proactive
        // signals this app is built on could silently stop firing with nothing to show for it.
        // Each check now runs in its own try/catch via runCheck(), logged with AppLogger so a
        // failure is visible (see the Diagnostics screen) instead of just vanishing, and one
        // check's failure can no longer take out every other one in the same pass.
        //
        // The whole body also runs under executionMutex (see its kdoc) - two concurrent
        // executions of this worker running their check suites interleaved is exactly the
        // check-then-act race that let syncBillTasks create a duplicate task under overlap.
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (hasSmsPermission) {
            runCheck("smsHistoryScan") { SmsHistoryScanner.scanIfNeeded(applicationContext) }
        }

        val db = AppDatabase.getInstance(applicationContext)
        val insights = FinanceInsightsRepository(
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao(),
            subscriptionDao = db.subscriptionDao(),
            billDao = db.billDao()
        )
        runCheck("refreshRecurringDetection") { insights.refreshRecurringDetection() }

        runCheck("checkBills") { FinanceNotificationChecks.checkBills(applicationContext, db, insights) }
        runCheck("checkSubscriptions") { FinanceNotificationChecks.checkSubscriptions(applicationContext, db, insights) }
        runCheck("checkBudgets") { FinanceNotificationChecks.checkBudgets(applicationContext, db, insights) }
        runCheck("checkBudgetPace") { FinanceNotificationChecks.checkBudgetPace(applicationContext, db, insights) }
        runCheck("checkUncategorizedSpend") { FinanceNotificationChecks.checkUncategorizedSpend(applicationContext, db) }
        runCheck("checkTasks") { ProductivityNotificationChecks.checkTasks(applicationContext, db) }
        runCheck("checkTasksDueSoon") { ProductivityNotificationChecks.checkTasksDueSoon(applicationContext, db) }
        runCheck("checkHabits") { ProductivityNotificationChecks.checkHabits(applicationContext, db) }
        runCheck("checkHabitsAtRisk") { ProductivityNotificationChecks.checkHabitsAtRisk(applicationContext, db) }
        runCheck("checkNightSummary") { CrossPillarNotificationChecks.checkNightSummary(applicationContext, db) }
        runCheck("checkMorningHeadsUp") { CrossPillarNotificationChecks.checkMorningHeadsUp(applicationContext, db, insights) }
        runCheck("checkGoalsOffTrack") { ProductivityNotificationChecks.checkGoalsOffTrack(applicationContext, db) }
        runCheck("syncBillTasks") { FinanceNotificationChecks.syncBillTasks(db, insights) }

        Result.success()
    }

    private suspend fun runCheck(name: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            AppLogger.e("NotificationCheckWorker", "check failed: $name", e)
        }
    }
}
