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
import com.lifeos.expensecapture.MainActivity
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import com.lifeos.expensecapture.data.db.entity.NotificationType

/**
 * The insert-record + system-push implementation, extracted out of NotificationCheckWorker so
 * ParseIncomingSmsWorker (the live SMS path) can use the exact same mechanism for a genuinely
 * event-driven alert (UNUSUAL_TRANSACTION) - that one can't wait for the next periodic check
 * without losing the entire point of being real-time.
 */
object NotificationSender {
    private const val COOLDOWN_MILLIS = 20L * 60 * 60 * 1000 // ~20h: at most once/day per item

    suspend fun recentlyNotified(db: AppDatabase, type: NotificationType, cooldownKey: String): Boolean {
        val since = System.currentTimeMillis() - COOLDOWN_MILLIS
        return db.notificationDao().countRecent(type, cooldownKey, since) > 0
    }

    suspend fun notify(
        context: Context,
        type: NotificationType,
        title: String,
        body: String,
        route: String,
        cooldownKey: String,
        channel: String = NotificationChannels.REMINDERS
    ) {
        val db = AppDatabase.getInstance(context)
        db.notificationDao().insert(
            NotificationEntity(type = type, title = title, body = body, deepLinkRoute = route, sourceKey = cooldownKey)
        )

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return // Recorded in the Center regardless; system push just skipped.

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, cooldownKey.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(cooldownKey.hashCode(), notification)
    }
}
