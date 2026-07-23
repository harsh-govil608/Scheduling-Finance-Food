package com.lifeos.expensecapture.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lifeos.expensecapture.MainActivity
import com.lifeos.expensecapture.R
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Widgets PRD, Phase 3 Doc 04, scoped to exactly one widget type: a single-pillar (Finance)
 * summary showing "spent this month" - the PRD's "single-pillar summary" type. No lock-screen
 * variant, no interactivity tiers: there's no financial action safe to expose without a full
 * screen and confirmation, so this widget is glance-only, tap-through to the app. Refreshes on
 * Android's minimum periodic interval (30 min, declared in widget_spend_info.xml) plus
 * manually whenever a transaction changes (see updateAll callers). See day-2.md.
 */
class SpendWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SpendWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val zone = ZoneId.systemDefault()
                val monthStart = LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val transactions = db.transactionDao().getSince(monthStart)
                val spent = transactions.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }

                val views = RemoteViews(context.packageName, R.layout.widget_spend_summary)
                views.setTextViewText(R.id.widget_amount, "₹${"%.2f".format(spent)}")
                views.setTextViewText(R.id.widget_label, "Spent this month")

                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
