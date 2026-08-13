package com.lifeos.expensecapture.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifeos.expensecapture.data.db.entity.NotificationType

/**
 * UI polish pass (found via a real user report, 2026-07 - "the UI is looking too basic"): the
 * Notification Center had zero visual differentiation between notification types beyond a
 * read/unread background tint - just plain text. Same idea as CategoryVisuals: a per-type icon,
 * cycling through the theme's existing color-container pairs via CategoryVisuals.colorPairFor
 * (keyed on the type's own name), rather than inventing a second, parallel color scheme.
 */
object NotificationVisuals {
    fun iconFor(type: NotificationType): ImageVector = when (type) {
        NotificationType.BILL_DUE -> Icons.Filled.Payments
        NotificationType.SUBSCRIPTION_RENEWAL -> Icons.Filled.Autorenew
        NotificationType.BUDGET_OVER_LIMIT -> Icons.Filled.PriorityHigh
        NotificationType.BUDGET_PACE_WARNING -> Icons.AutoMirrored.Filled.TrendingUp
        NotificationType.NIGHT_SUMMARY_READY -> Icons.Filled.NightsStay
        NotificationType.MORNING_HEADSUP -> Icons.Filled.WbSunny
        NotificationType.TASK_DUE, NotificationType.TASK_DUE_SOON -> Icons.Filled.CheckCircle
        NotificationType.HABIT_REMINDER, NotificationType.HABIT_AT_RISK -> Icons.Filled.CheckCircle
        NotificationType.UNUSUAL_TRANSACTION -> Icons.Filled.PriorityHigh
        NotificationType.GOAL_OFF_TRACK -> Icons.Filled.Savings
        NotificationType.UNCATEGORIZED_SPEND -> Icons.AutoMirrored.Filled.ReceiptLong
        NotificationType.PERIODIC_CHECK_IN -> Icons.Filled.EditNote
        NotificationType.SMART_SPLIT_ADDED -> Icons.AutoMirrored.Filled.CallSplit
    }
}
