package com.lifeos.expensecapture.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.util.Prefs
import com.lifeos.expensecapture.widget.SpendWidgetProvider

class ParseIncomingSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
    }

    override suspend fun doWork(): Result {
        // Account & Profile Management PRD (Doc 44) "pause automatic capture" preference.
        // Only gates the live path - the one-time history scan is an explicit user action,
        // not ongoing automation, so it's unaffected by this flag.
        if (Prefs.isCapturePaused(applicationContext)) return Result.success()

        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()

        val db = AppDatabase.getInstance(applicationContext)
        TransactionIngestor.ingest(db, sender, body, System.currentTimeMillis())
        SpendWidgetProvider.updateAll(applicationContext)

        return Result.success()
    }
}
