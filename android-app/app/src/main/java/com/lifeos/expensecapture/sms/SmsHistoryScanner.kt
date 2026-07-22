package com.lifeos.expensecapture.sms

import android.content.Context
import android.provider.Telephony
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.sms.parser.TransactionParser

/**
 * One-time scan of the existing SMS inbox, run right after the user grants SMS permission.
 *
 * Without this, the app only ever sees NEW messages arriving after install (via SmsReceiver) -
 * which isn't what "automatic expense capture" means to a real user who already has months of
 * transaction history sitting in their inbox. This was a real gap between the architecture doc
 * (Section 2's user flow explicitly says "App scans existing transaction SMS + listens for
 * new ones") and the first pass of the code, which only implemented the second half.
 *
 * Gated by a persisted flag so re-opening the app doesn't re-scan and re-insert every time.
 */
object SmsHistoryScanner {

    private const val PREFS_NAME = "sms_history_scan"
    private const val KEY_HAS_SCANNED = "has_scanned"

    suspend fun scanIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_HAS_SCANNED, false)) return

        val db = AppDatabase.getInstance(context)
        val parser = TransactionParser()

        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                val sender = cursor.getString(addressIndex) ?: continue
                val body = cursor.getString(bodyIndex) ?: continue
                val date = cursor.getLong(dateIndex)
                TransactionIngestor.ingest(db, sender, body, date, parser)
            }
        }

        prefs.edit().putBoolean(KEY_HAS_SCANNED, true).apply()
    }
}
