package com.lifeos.expensecapture.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.AppDatabase

class ParseIncomingSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()

        val db = AppDatabase.getInstance(applicationContext)
        TransactionIngestor.ingest(db, sender, body, System.currentTimeMillis())

        return Result.success()
    }
}
