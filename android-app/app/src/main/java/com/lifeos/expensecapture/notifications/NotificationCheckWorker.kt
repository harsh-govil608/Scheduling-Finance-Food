package com.lifeos.expensecapture.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.sms.SmsHistoryScanner
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        private const val DUE_SOON_WINDOW_DAYS = 3L // matches checkBills' own DUE_TODAY/OVERDUE horizon
        private const val TASK_DUE_SOON_WINDOW_MILLIS = 3L * 60 * 60 * 1000 // 3h - a genuine heads-up, not just "it's late"
        private const val MORNING_WINDOW_START_HOUR = 7
        private const val MORNING_WINDOW_END_HOUR = 9 // inclusive; wide enough that a 2h worker cadence can't skip it entirely
        private const val MIN_COMPLETIONS_FOR_RHYTHM = 3
        private const val MIN_GAP_DAYS_FOR_RISK_CHECK = 2.0 // below this it's daily-ish - checkHabits already owns that
        private const val RISK_GAP_MULTIPLIER = 1.5
        private const val GOAL_ON_TRACK_RATIO = 0.7 // pace below 70% of what's needed counts as "off track"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d")

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
        checkBudgetPace(db, insights)
        checkTasks(db)
        checkTasksDueSoon(db)
        checkHabits(db)
        checkHabitsAtRisk(db)
        checkNightSummary(db)
        checkMorningHeadsUp(db, insights)
        checkGoalsOffTrack(db)
        syncBillTasks(db, insights)

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

    /**
     * AI Transformation Plan H1: Finance knows a bill is coming due; Home's Tasks had no idea it
     * existed - a bill generated a push notification here and nothing else, never became a thing
     * to actually do. This bridges the two, deterministically, no model involved: within
     * `DUE_SOON_WINDOW_DAYS` of a CONFIRMED_TRACKED bill's due date, ensure a linked task exists
     * on Home's own Due Today list.
     *
     * Scoped to Bills only, not Subscriptions - Subscriptions are auto-debited recurring charges
     * (see SubscriptionEntity's kdoc), not something the user needs to go *do*, so a "pay Netflix"
     * task would be a false action item. Bills (Doc 22) are explicitly the variable-amount,
     * user-actioned kind (rent, informal loans) - the PRD's own distinction from Subscriptions.
     *
     * A separate data sync from checkBills' push notification above: this runs every worker pass
     * unconditionally (no cooldown) since it's idempotent - re-running it with unchanged data is a
     * no-op, not a repeat notification.
     */
    private suspend fun syncBillTasks(db: AppDatabase, insights: FinanceInsightsRepository) {
        val bills = insights.observeBills().first()
        for (item in bills) {
            val bill = item.bill
            val actionable = bill.status == BillStatus.CONFIRMED_TRACKED &&
                item.displayStatus != FinanceInsightsRepository.BillDisplayStatus.PAID_THIS_CYCLE &&
                item.daysUntilDue <= DUE_SOON_WINDOW_DAYS
            if (!actionable) continue

            val title = "Pay ${bill.payeeDisplay} (~₹${"%.2f".format(bill.typicalAmount)})"
            val existing = db.taskDao().findLatestForBill(bill.id)

            when {
                existing != null && !existing.completed -> {
                    // Update in place rather than spawning a duplicate every check.
                    if (existing.title != title || existing.dueDate != item.dueDateThisCycleMillis) {
                        db.taskDao().update(
                            existing.copy(title = title, dueDate = item.dueDateThisCycleMillis)
                        )
                    }
                }
                existing == null || (existing.dueDate ?: 0L) < item.dueDateThisCycleMillis -> {
                    // No linked task yet, or the last one was completed for an earlier cycle -
                    // this is a new cycle's bill, needs its own task instance (no recurrence
                    // engine in this app - see TaskEntity's kdoc - so a fresh row per cycle is
                    // the honest scope here, same as how a completed habit-day never gets reused).
                    db.taskDao().insert(
                        TaskEntity(
                            title = title,
                            dueDate = item.dueDateThisCycleMillis,
                            sourceBillId = bill.id
                        )
                    )
                }
                // else: already completed for this exact cycle - leave it, nothing to do.
            }
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

    /**
     * AI Transformation Plan, forward-looking predictions: checkBudgets above only ever reports
     * a category that's ALREADY over its limit - purely retrospective. This projects forward from
     * the current spend pace (the same linear run-rate FinanceInsightsRepository already uses for
     * BudgetProgress.projectedMonthEndSpend, not a new model) to warn *before* the limit is hit,
     * with an actual estimated date - "at this pace" framing, never stated as certain, same
     * honesty discipline Spend Prediction already applies elsewhere in this app.
     */
    private suspend fun checkBudgetPace(db: AppDatabase, insights: FinanceInsightsRepository) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val daysElapsed = today.dayOfMonth.toDouble().coerceAtLeast(1.0)
        val daysInMonth = today.lengthOfMonth()

        val budgets = insights.observeBudgetProgress().first()
        for (progress in budgets) {
            if (progress.spentThisMonth > progress.budget.monthlyLimit) continue // already over - checkBudgets owns that case
            if (progress.predictionConfidence == FinanceInsightsRepository.PredictionConfidence.INSUFFICIENT_DATA) continue

            val runRate = progress.spentThisMonth / daysElapsed
            if (runRate <= 0.0) continue
            val remaining = progress.budget.monthlyLimit - progress.spentThisMonth
            val exhaustionDayOfMonth = daysElapsed + (remaining / runRate)
            if (exhaustionDayOfMonth >= daysInMonth) continue // projected to last the month at this pace

            val exhaustionDate = today.withDayOfMonth(1).plusDays(exhaustionDayOfMonth.toLong() - 1)
            val daysEarly = daysInMonth - exhaustionDayOfMonth.toInt()
            val route = "budgets"
            val cooldownKey = "pace" + progress.budget.id
            if (recentlyNotified(db, NotificationType.BUDGET_PACE_WARNING, cooldownKey)) continue

            notify(
                type = NotificationType.BUDGET_PACE_WARNING,
                title = "${progress.categoryName} pace warning",
                body = "At this pace, you'll hit your ₹${"%.2f".format(progress.budget.monthlyLimit)} limit around " +
                    "${DATE_FORMATTER.format(exhaustionDate)} - about $daysEarly day${if (daysEarly == 1) "" else "s"} before month end",
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

    /** "Reminders everywhere": a heads-up a few hours before a task is due, not just once it's
     * already late (checkTasks above). Its own NotificationType/cooldown so it can fire once as
     * a heads-up and, separately, once again later if it actually goes overdue. */
    private suspend fun checkTasksDueSoon(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val windowEnd = now + TASK_DUE_SOON_WINDOW_MILLIS
        val tasks = db.taskDao().observeAll().first()
        for (task in tasks) {
            if (task.completed) continue
            val dueDate = task.dueDate ?: continue
            if (dueDate <= now || dueDate > windowEnd) continue
            val route = "tasks"
            val cooldownKey = "soon" + task.id
            if (recentlyNotified(db, NotificationType.TASK_DUE_SOON, cooldownKey)) continue

            notify(
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

    /**
     * AI Transformation Plan, forward-looking predictions: checkHabits above only ever asks "was
     * today's box checked" - fine for daily habits, but silent for a habit with its own multi-day
     * rhythm (gym every 2-3 days, calling family weekly). This looks at each habit's own history
     * of check-ins, computes its typical gap, and flags one that's run meaningfully past its own
     * pattern - same "supportive, never punitive" copy discipline as HABIT_REMINDER and
     * HabitsViewModel's streak logic, never framed as a failure.
     */
    private suspend fun checkHabitsAtRisk(db: AppDatabase) {
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
            if (recentlyNotified(db, NotificationType.HABIT_AT_RISK, cooldownKey)) continue

            notify(
                type = NotificationType.HABIT_AT_RISK,
                title = "\"${habit.name}\" might be due for a check-in",
                body = "You usually come back to this every ~${averageGapDays.toInt()} days - it's been $daysSinceLast. No pressure, just a nudge.",
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

    /**
     * "Reminders everywhere": a single proactive push in the early morning summarizing the day
     * ahead - unlike the in-app Morning Briefing card (which only shows if the user happens to
     * open the app), this fires on its own. Only sent when there's a genuine reason to (a due
     * task, a habit, a bill) - a quiet day stays quiet rather than pushing a manufactured
     * "good morning, nothing's up" notification.
     */
    private suspend fun checkMorningHeadsUp(db: AppDatabase, insights: FinanceInsightsRepository) {
        val hour = LocalTime.now().hour
        if (hour !in MORNING_WINDOW_START_HOUR..MORNING_WINDOW_END_HOUR) return
        val route = "productivity_home"
        if (recentlyNotified(db, NotificationType.MORNING_HEADSUP, route)) return

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

        notify(
            type = NotificationType.MORNING_HEADSUP,
            title = "Your day ahead",
            body = parts.joinToString(", ").replaceFirstChar { it.uppercase() },
            route = route,
            cooldownKey = route
        )
    }

    /**
     * Closes the loop on the Goal targetAmount field added for the Spending Insight card's
     * goal-acceleration line: that line only ever appears passively, inside a Finance insight
     * that may not fire this month. This proactively flags a goal on its own, on a schedule,
     * when the current savings pace genuinely won't reach the target amount by its target date -
     * same "current monthly pace from net cash flow" estimate as SpendingInsightEngine's own
     * goal-acceleration math (kept as its own small copy here rather than a shared extraction -
     * both are a handful of lines, not worth the indirection for two call sites). Supportive
     * framing - "worth a check-in," never a countdown to failure.
     */
    private suspend fun checkGoalsOffTrack(db: AppDatabase) {
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
            if (recentlyNotified(db, NotificationType.GOAL_OFF_TRACK, cooldownKey)) continue

            notify(
                type = NotificationType.GOAL_OFF_TRACK,
                title = "\"${goal.title}\" might need a closer look",
                body = "At your current saving pace, this could be tight to reach by its target date - worth a check-in, no pressure.",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /** Delegates to the shared NotificationSender (see its kdoc) - kept as thin wrappers here so
     * every existing check function below didn't need to change its call site. */
    private suspend fun recentlyNotified(db: AppDatabase, type: NotificationType, cooldownKey: String): Boolean =
        NotificationSender.recentlyNotified(db, type, cooldownKey)

    private suspend fun notify(
        type: NotificationType,
        title: String,
        body: String,
        route: String,
        cooldownKey: String,
        channel: String = NotificationChannels.REMINDERS
    ) = NotificationSender.notify(applicationContext, type, title, body, route, cooldownKey, channel)
}
