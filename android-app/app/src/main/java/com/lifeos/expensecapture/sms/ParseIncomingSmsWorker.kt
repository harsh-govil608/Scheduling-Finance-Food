package com.lifeos.expensecapture.sms

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeos.expensecapture.categorization.CategorizationEngine
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import com.lifeos.expensecapture.sms.parser.ParseResult
import com.lifeos.expensecapture.sms.parser.TransactionParser

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
        val parser = TransactionParser()
        val categorizationEngine = CategorizationEngine(db.merchantRuleDao(), db.categoryDao())

        when (val result = parser.parse(sender, body)) {
            is ParseResult.Parsed -> {
                val categoryId = categorizationEngine.categorize(result.merchantRaw)
                db.transactionDao().insert(
                    TransactionEntity(
                        amount = result.amount,
                        direction = result.direction,
                        merchantRaw = result.merchantRaw,
                        merchantNormalized = result.merchantRaw.trim().lowercase(),
                        categoryId = categoryId,
                        date = System.currentTimeMillis(),
                        source = TransactionSource.SMS_AUTO,
                        confidenceScore = result.confidence
                    )
                )
            }
            is ParseResult.Unparsed -> {
                // Known gap: not yet persisted to a manual-review queue - see
                // android-app/README.md. The parser deliberately doesn't guess here rather
                // than silently dropping the message.
            }
        }

        return Result.success()
    }
}
