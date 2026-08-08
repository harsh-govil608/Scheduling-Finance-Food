package com.lifeos.expensecapture.sms

import android.content.Context
import android.provider.Telephony
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.sms.parser.ParseResult
import com.lifeos.expensecapture.sms.parser.TransactionParser

/**
 * Read-only accounting of what SmsHistoryScanner/TransactionIngestor actually do with the whole
 * SMS inbox, without inserting anything - built per real founder request (2026-08) after a
 * confirmed report that a second phone captured under 10% of real transactions, and there was no
 * way to see WHY short of pulling the raw database file. Answers exactly what was asked: how many
 * SMS exist, how many look financial, how many parsed, how many didn't and why, how many would be
 * silently skipped, and how many are already-captured duplicates.
 *
 * Deliberately does not call AiSmsParser - this is a fast, free, fully local dry run, and burning
 * a real AI call per unparsed message just to produce a diagnostics count isn't worth the cost or
 * the wait. `wouldTryAiFallback` reports how many messages are eligible for it (institutional
 * sender, still unparsed) without spending it.
 */
data class SmsScanDiagnostics(
    val totalSmsFound: Int,
    val financialCandidates: Int,
    val parsed: Int,
    val duplicatesAlreadyCaptured: Int,
    val ignoredOtpOrPromo: Int,
    val needsReview: Int,
    val wouldTryAiFallback: Int,
    val silentlySkipped: Int,
    val silentlySkippedSenders: List<String>
)

object SmsDiagnosticsScanner {

    suspend fun scan(context: Context): SmsScanDiagnostics {
        val db = AppDatabase.getInstance(context)
        val parser = TransactionParser()

        val existingSourceHashes = db.transactionDao().getAllSourceHashes().toHashSet()
        val existingReferenceIds = db.transactionDao().getAllReferenceIds().toHashSet()

        var total = 0
        var financialCandidates = 0
        var parsed = 0
        var duplicates = 0
        var ignored = 0
        var needsReview = 0
        var wouldTryAi = 0
        var skipped = 0
        val skippedSenders = LinkedHashSet<String>()

        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY)
        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)

            while (cursor.moveToNext()) {
                total++
                val sender = cursor.getString(addressIndex) ?: continue
                val body = cursor.getString(bodyIndex) ?: continue

                when (val result = parser.parse(sender, body)) {
                    is ParseResult.Parsed -> {
                        financialCandidates++
                        val sourceHash = "$sender::$body"
                        val isDuplicate = existingSourceHashes.contains(sourceHash) ||
                            (result.referenceId != null && existingReferenceIds.contains(result.referenceId))
                        if (isDuplicate) duplicates++ else parsed++
                    }
                    is ParseResult.Ignored -> {
                        ignored++
                    }
                    is ParseResult.Unparsed -> {
                        financialCandidates++
                        if (TransactionParser.looksLikeInstitutionalSender(sender)) {
                            needsReview++
                            wouldTryAi++
                        } else {
                            skipped++
                            skippedSenders += sender
                        }
                    }
                }
            }
        }

        return SmsScanDiagnostics(
            totalSmsFound = total,
            financialCandidates = financialCandidates,
            parsed = parsed,
            duplicatesAlreadyCaptured = duplicates,
            ignoredOtpOrPromo = ignored,
            needsReview = needsReview,
            wouldTryAiFallback = wouldTryAi,
            silentlySkipped = skipped,
            silentlySkippedSenders = skippedSenders.toList()
        )
    }
}
