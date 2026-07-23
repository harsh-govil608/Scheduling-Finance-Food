package com.lifeos.expensecapture.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS = "reminders"
    const val SUMMARY = "summary"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(REMINDERS, "Bill & subscription reminders", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(SUMMARY, "Daily summary", NotificationManager.IMPORTANCE_LOW)
        )
    }
}
