package com.lifeos.expensecapture.notifications

import android.content.Context
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pre-beta hardening (Priority 5 - architecture): split out of NotificationCheckWorker, which had
 * grown to 554 lines covering every pillar's checks in one file. This is the Finance-pillar
 * subset - Bills, Subscriptions, Budgets, and the bill-to-task bridge - grouped the same way the
 * app's own pillars are, so a change to one pillar's proactive logic doesn't require scrolling
 * past every other pillar's to find it. No behavior change from this split - every function body
 * is unchanged, just relocated.
 */
object FinanceNotificationChecks {

    private const val DUE_SOON_WINDOW_DAYS = 3L // matches checkBills' own DUE_TODAY/OVERDUE horizon
    private const val MIN_UNCATEGORIZED_COUNT_TO_NOTIFY = 10 // a stray one or two isn't worth a nudge
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d")

    suspend fun checkBills(context: Context, db: AppDatabase, insights: FinanceInsightsRepository) {
        val bills = insights.observeBills().first()
        for (item in bills) {
            val dueSoon = item.displayStatus == FinanceInsightsRepository.BillDisplayStatus.DUE_TODAY ||
                item.displayStatus == FinanceInsightsRepository.BillDisplayStatus.OVERDUE
            if (!dueSoon) continue
            val route = "bills"
            if (NotificationSender.recentlyNotified(db, NotificationType.BILL_DUE, route + item.bill.id)) continue

            NotificationSender.notify(
                context = context,
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
    suspend fun syncBillTasks(db: AppDatabase, insights: FinanceInsightsRepository) {
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

    suspend fun checkSubscriptions(context: Context, db: AppDatabase, insights: FinanceInsightsRepository) {
        val subs = insights.observeSubscriptions().first()
        for (item in subs) {
            if (item.displayStatus != FinanceInsightsRepository.SubscriptionDisplayStatus.RENEWAL_UPCOMING) continue
            val route = "subscriptions"
            val cooldownKey = route + item.subscription.id
            if (NotificationSender.recentlyNotified(db, NotificationType.SUBSCRIPTION_RENEWAL, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.SUBSCRIPTION_RENEWAL,
                title = "${item.subscription.merchantDisplay} renews soon",
                body = "₹${"%.2f".format(item.subscription.amount)} expected around this time",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /**
     * AI Transformation Plan, forward-looking predictions: checkBudgets below only ever reports
     * a category that's ALREADY over its limit - purely retrospective. This projects forward from
     * the current spend pace (the same linear run-rate FinanceInsightsRepository already uses for
     * BudgetProgress.projectedMonthEndSpend, not a new model) to warn *before* the limit is hit,
     * with an actual estimated date - "at this pace" framing, never stated as certain, same
     * honesty discipline Spend Prediction already applies elsewhere in this app.
     */
    suspend fun checkBudgetPace(context: Context, db: AppDatabase, insights: FinanceInsightsRepository) {
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
            if (NotificationSender.recentlyNotified(db, NotificationType.BUDGET_PACE_WARNING, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.BUDGET_PACE_WARNING,
                title = "${progress.categoryName} pace warning",
                body = "At this pace, you'll hit your ₹${"%.2f".format(progress.budget.monthlyLimit)} limit around " +
                    "${DATE_FORMATTER.format(exhaustionDate)} - about $daysEarly day${if (daysEarly == 1) "" else "s"} before month end",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    suspend fun checkBudgets(context: Context, db: AppDatabase, insights: FinanceInsightsRepository) {
        val budgets = insights.observeBudgetProgress().first()
        for (progress in budgets) {
            if (progress.spentThisMonth <= progress.budget.monthlyLimit) continue
            val route = "budgets"
            val cooldownKey = route + progress.budget.id
            if (NotificationSender.recentlyNotified(db, NotificationType.BUDGET_OVER_LIMIT, cooldownKey)) continue

            NotificationSender.notify(
                context = context,
                type = NotificationType.BUDGET_OVER_LIMIT,
                title = "${progress.categoryName} is over budget",
                body = "₹${"%.2f".format(progress.spentThisMonth)} spent of a ₹${"%.2f".format(progress.budget.monthlyLimit)} limit this month",
                route = route,
                cooldownKey = cooldownKey
            )
        }
    }

    /**
     * Found via a real user report (2026-07): CategorizationEngine is deliberately learn-by-
     * correction with no default merchant rules (see its own kdoc) - which means a category
     * budget or Spending Insight can silently never fire for a user who hasn't yet corrected any
     * transactions, with nothing in the app telling them that's why. This is a one-time-a-day
     * nudge once enough spend has piled up Uncategorized this month, pointing at the Ledger
     * screen - the one place a correction can be made. Count-based, not amount-based: a handful
     * of low-value stray transactions isn't worth a nudge, but MIN_UNCATEGORIZED_COUNT_TO_NOTIFY
     * of them, regardless of amount, is a genuine pattern worth surfacing.
     */
    suspend fun checkUncategorizedSpend(context: Context, db: AppDatabase) {
        val uncategorizedCategory = db.categoryDao().getUncategorized() ?: return
        val monthStart = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val uncategorized = db.transactionDao().getSince(monthStart).filter {
            it.direction == TransactionDirection.DEBIT && it.categoryId == uncategorizedCategory.id
        }
        if (uncategorized.size < MIN_UNCATEGORIZED_COUNT_TO_NOTIFY) return

        val route = "ledger"
        if (NotificationSender.recentlyNotified(db, NotificationType.UNCATEGORIZED_SPEND, route)) return

        val total = uncategorized.sumOf { it.amount }
        NotificationSender.notify(
            context = context,
            type = NotificationType.UNCATEGORIZED_SPEND,
            title = "A few transactions could use a category",
            body = "${uncategorized.size} transactions (~₹${"%.2f".format(total)}) this month aren't " +
                "categorized yet - a couple of taps in your Ledger makes budgets and insights more accurate.",
            route = route,
            cooldownKey = route
        )
    }
}
