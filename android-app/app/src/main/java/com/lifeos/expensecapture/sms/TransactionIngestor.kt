package com.lifeos.expensecapture.sms

import com.lifeos.expensecapture.categorization.CategorizationEngine
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import com.lifeos.expensecapture.sms.parser.AiSmsParser
import com.lifeos.expensecapture.sms.parser.ParseResult
import com.lifeos.expensecapture.sms.parser.TransactionParser

/**
 * Shared parse -> categorize -> insert pipeline used by both the live SMS receiver
 * (ParseIncomingSmsWorker, for messages arriving after install) and the one-time inbox
 * history scan (SmsHistoryScanner, for messages already on the device before install) -
 * so the two capture paths can never silently drift apart.
 *
 * Returns the inserted TransactionEntity (or null if unparsed/a duplicate) so a caller can react
 * to it - deliberately NOT doing that reaction here, since this same function backfills months of
 * history on first install; anomaly detection or a notification belongs only on the live path
 * (see ParseIncomingSmsWorker), or the very first scan would fire dozens of stale alerts at once.
 */
object TransactionIngestor {

    suspend fun ingest(
        db: AppDatabase,
        sender: String,
        body: String,
        timestamp: Long,
        parser: TransactionParser = TransactionParser()
    ): TransactionEntity? {
        val categorizationEngine = CategorizationEngine(db.merchantRuleDao(), db.categoryDao())

        suspend fun insertParsed(result: ParseResult.Parsed): TransactionEntity? {
            // Secondary dedup signal (see TransactionEntity.referenceId's kdoc): the exact
            // sender+body sourceHash below only catches a literal re-scan of the identical SMS.
            // A real reference/transaction number repeating means the same money movement was
            // already recorded, even if a different SMS (e.g. the bank's vs. a UPI app's own
            // notification) described it in different words.
            if (!result.referenceId.isNullOrBlank() && db.transactionDao().countByReferenceId(result.referenceId) > 0) {
                return null
            }

            val categoryId = categorizationEngine.categorize(result.merchantRaw, result.amount, result.direction)
            val entity = TransactionEntity(
                amount = result.amount,
                direction = result.direction,
                merchantRaw = result.merchantRaw,
                merchantNormalized = result.merchantRaw.trim().lowercase(),
                categoryId = categoryId,
                date = timestamp,
                source = TransactionSource.SMS_AUTO,
                confidenceScore = result.confidence,
                sourceHash = "$sender::$body",
                referenceId = result.referenceId,
                isRefund = result.isRefund
            )
            val insertedId = db.transactionDao().insert(entity)
            return if (insertedId > 0) entity.copy(id = insertedId) else null // 0 means the unique-index IGNORE rejected a duplicate
        }

        when (val result = parser.parse(sender, body)) {
            is ParseResult.Parsed -> return insertParsed(result)
            is ParseResult.Unparsed -> {
                // Fixed Day 2 (see docs/coders-documentation/day-2.md): previously discarded,
                // making "parse failed" indistinguishable from "nothing happened." Surfaced in
                // the Needs Review queue for manual conversion - but only when the sender
                // itself looks like a real bank/institutional sender (found via a real user
                // report, 2026-07 - see TransactionParser.looksLikeInstitutionalSender's kdoc
                // for why body-keyword matches alone aren't enough reason to interrupt a human).
                // A promotional text or delivery update that merely mentions "credit" or
                // "account" is still parsed and still correctly fails to become a transaction -
                // it's just not worth surfacing as something to review.
                if (!TransactionParser.looksLikeInstitutionalSender(sender)) return null

                // AI fallback (2026-08, real founder request - see AiSmsParser's kdoc) - only
                // reached once the deterministic templates already failed, for a sender that
                // already looks like a real bank/payment institution. A blank/invalid key or any
                // AI failure just returns null here, falling through to the exact same Needs
                // Review behavior as before this existed - never a regression, only upside.
                AiSmsParser.tryParse(body)?.let { aiResult -> return insertParsed(aiResult) }

                db.unparsedMessageDao().insert(
                    UnparsedMessageEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        reason = result.reason,
                        sourceHash = "$sender::$body"
                    )
                )
                return null
            }
            is ParseResult.Ignored -> {
                // Bug fix (found via a real user report, 2026-07): unlike Unparsed, this means
                // the parser is confident this was never a transaction attempt at all (OTP,
                // promotional text) - see ParseResult.Ignored's kdoc. Correctly not landing in
                // the Needs Review queue, same as any other non-financial SMS never reaching
                // this pipeline's output.
                return null
            }
        }
    }
}
