package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Notification Center PRD (Phase 3 Doc 03): the durable, reviewable record of every
 * notification this app has ever surfaced. Deliberately single-device scope - the full PRD's
 * cross-device read-state sync and arbitration-engine-fed digest/batching don't apply here,
 * since there's no backend and no arbitration engine (Phase 2 Notification System was never
 * implemented as code). See docs/coders-documentation/day-2.md for that scope boundary.
 */
enum class NotificationType {
    BILL_DUE, SUBSCRIPTION_RENEWAL, BUDGET_OVER_LIMIT, NIGHT_SUMMARY_READY, TASK_DUE, HABIT_REMINDER,
    /** "Reminders everywhere" push: a task's due date is a few hours away, not yet overdue -
     * distinct from TASK_DUE (which only fires once it's actually late) so both can coexist with
     * their own cooldowns instead of one suppressing the other. */
    TASK_DUE_SOON,
    /** A single proactive push in the early morning summarizing the day ahead (due tasks, open
     * habits, bills due soon) - unlike the in-app Morning Briefing card, this fires whether or not
     * the user ever opens the app that day. */
    MORNING_HEADSUP,
    /** Forward-looking prediction, not a retrospective "you're over budget": current spend pace
     * projects hitting the limit before the month ends. */
    BUDGET_PACE_WARNING,
    /** Forward-looking, supportive - a habit's own typical rhythm suggests it's due for a
     * check-in, phrased the same non-punitive way as HABIT_REMINDER, never "you're failing." */
    HABIT_AT_RISK,
    /** Event-driven, not periodic: fires the moment a captured transaction is a real statistical
     * outlier versus that category's own history - the one genuinely real-time proactive signal
     * in this app, everything else only runs on the periodic worker's schedule. */
    UNUSUAL_TRANSACTION,
    /** A Goal with both a rupee target and a target date, whose current savings pace (the same
     * proxy SpendingInsightEngine's goal-acceleration line uses) won't realistically get there in
     * time - supportive framing, a check-in prompt, not a countdown to failure. */
    GOAL_OFF_TRACK,
    /** Found via a real user report (2026-07): categorization here is deliberately learn-by-
     * correction with no default merchant rules (see CategorizationEngine's kdoc) - accurate
     * until the user has actually corrected a few transactions, and totally silent about that
     * requirement in the meantime. A pile of Uncategorized spend quietly means every category
     * budget and insight that depends on it (Spending Insight, category budgets) can't fire.
     * This closes that silence: a nudge toward Ledger once enough uncategorized spend has
     * accumulated this month, not a hard requirement. */
    UNCATEGORIZED_SPEND
}

@Entity(tableName = "app_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: NotificationType,
    val title: String,
    val body: String,
    /** Clean nav route this notification opens when tapped, e.g. "bills", "budgets". */
    val deepLinkRoute: String,
    /** Per-instance dedup key (e.g. "bills42") used only for the cooldown check - distinct
     * from deepLinkRoute since a route like "bills" is shared across many individual bills. */
    val sourceKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    /** Bug fix (found via a real user report, 2026-07): the Notification Center had no way to
     * remove anything, so it only ever grew. This is a soft delete, not a real DELETE - the row
     * (and its sourceKey/createdAt) still has to exist for NotificationSender.recentlyNotified's
     * cooldown check to see it, or clearing your inbox would make an alert you just dismissed
     * eligible to fire again immediately instead of respecting its normal ~20h cooldown. */
    val isDismissed: Boolean = false
)
