package com.lifeos.expensecapture.sms

import com.lifeos.expensecapture.categorization.CategorizationEngine
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import com.lifeos.expensecapture.sms.parser.ParseResult
import com.lifeos.expensecapture.sms.parser.TransactionParser

/**
 * Shared parse -> categorize -> insert pipeline used by both the live SMS receiver
 * (ParseIncomingSmsWorker, for messages arriving after install) and the one-time inbox
 * history scan (SmsHistoryScanner, for messages already on the device before install) -
 * so the two capture paths can never silently drift apart.
 */
object TransactionIngestor {

    suspend fun ingest(
        db: AppDatabase,
        sender: String,
        body: String,
        timestamp: Long,
        parser: TransactionParser = TransactionParser()
    ) {
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
                        date = timestamp,
                        source = TransactionSource.SMS_AUTO,
                        confidenceScore = result.confidence,
                        sourceHash = "$sender::$body"
                    )
                )
            }
            is ParseResult.Unparsed -> {
                // Fixed Day 2 (see docs/coders-documentation/day-2.md): previously discarded,
                // making "parse failed" indistinguishable from "nothing happened." Now
                // surfaced in the Needs Review queue for manual conversion.
                db.unparsedMessageDao().insert(
                    UnparsedMessageEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        reason = result.reason,
                        sourceHash = "$sender::$body"
                    )
                )
            }
        }
    }
}
