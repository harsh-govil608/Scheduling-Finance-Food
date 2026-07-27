package com.lifeos.expensecapture.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.NotificationType
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.notifications.NotificationSender
import com.lifeos.expensecapture.util.Prefs
import com.lifeos.expensecapture.widget.SpendWidgetProvider
import kotlinx.coroutines.flow.first

class ParseIncomingSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        private const val MIN_CATEGORY_HISTORY_FOR_ANOMALY = 5
        private const val ANOMALY_RATIO = 2.0 // must be at least double the category's own average
        private const val ANOMALY_MIN_ABSOLUTE_INCREASE = 500.0 // and a meaningful rupee jump, not just a small category doubling
    }

    override suspend fun doWork(): Result {
        // Account & Profile Management PRD (Doc 44) "pause automatic capture" preference.
        // Only gates the live path - the one-time history scan is an explicit user action,
        // not ongoing automation, so it's unaffected by this flag.
        if (Prefs.isCapturePaused(applicationContext)) return Result.success()

        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()

        val db = AppDatabase.getInstance(applicationContext)

        // Pre-beta hardening (Priority 4 - reliability): this is the single most important
        // capture path in the app, running on every incoming bank SMS, and it previously had no
        // error handling at all - an exception here (a genuine bug, not a malformed-SMS case,
        // since TransactionParser already handles those via ParseResult.Unparsed) would silently
        // drop that one transaction forever with zero record and no retry, the same class of
        // silent-data-loss bug this project has already hit twice before (see day-3.md). Now
        // logged with context and actually retried via WorkManager's own backoff instead of lost.
        val transaction = try {
            TransactionIngestor.ingest(db, sender, body, System.currentTimeMillis())
        } catch (e: Exception) {
            AppLogger.e("ParseIncomingSmsWorker", "ingest failed for sender=$sender", e)
            return Result.retry()
        }

        try {
            SpendWidgetProvider.updateAll(applicationContext)
        } catch (e: Exception) {
            // The transaction is already safely committed at this point - a widget refresh
            // failure is cosmetic and must never turn into a lost/retried transaction.
            AppLogger.e("ParseIncomingSmsWorker", "widget update failed", e)
        }

        // The one genuinely real-time proactive signal in this app: react to a transaction the
        // moment it's captured, not on the next 2-hour periodic check. Deliberately only reached
        // from this live path - see TransactionIngestor.ingest's kdoc for why the history-backfill
        // path must never trigger this. Wrapped separately so a failure here can never affect the
        // already-successful capture above.
        if (transaction != null) {
            try {
                checkAnomalyAndNotify(db, transaction)
            } catch (e: Exception) {
                AppLogger.e("ParseIncomingSmsWorker", "anomaly check failed for transaction=${transaction.id}", e)
            }
        }

        return Result.success()
    }

    private suspend fun checkAnomalyAndNotify(db: AppDatabase, transaction: TransactionEntity) {
        if (transaction.direction != TransactionDirection.DEBIT) return

        val categoryHistory = db.transactionDao().getSince(0L)
            .filter {
                it.direction == TransactionDirection.DEBIT &&
                    it.categoryId == transaction.categoryId &&
                    it.id != transaction.id
            }
        if (categoryHistory.size < MIN_CATEGORY_HISTORY_FOR_ANOMALY) return

        val average = categoryHistory.sumOf { it.amount } / categoryHistory.size
        val isAnomaly = transaction.amount >= average * ANOMALY_RATIO &&
            transaction.amount >= average + ANOMALY_MIN_ABSOLUTE_INCREASE
        if (!isAnomaly) return

        val categoryName = db.categoryDao().observeAll().first()
            .firstOrNull { it.id == transaction.categoryId }?.name ?: "this category"
        val cooldownKey = "anomaly" + transaction.id // unique per transaction - never repeats for the same charge

        NotificationSender.notify(
            context = applicationContext,
            type = NotificationType.UNUSUAL_TRANSACTION,
            title = "Unusually large charge",
            body = "${transaction.merchantRaw}: ₹${"%.2f".format(transaction.amount)} - " +
                "typical $categoryName spend is around ₹${"%.2f".format(average)}",
            route = "ledger",
            cooldownKey = cooldownKey
        )
    }
}
