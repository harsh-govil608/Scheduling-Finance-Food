package com.lifeos.expensecapture.sms

import android.content.Context
import android.provider.Telephony
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.sms.parser.TransactionParser
import com.lifeos.expensecapture.widget.SpendWidgetProvider

/**
 * Catch-up scan of the SMS inbox: on the very first run it covers the user's full history: on
 * every later run (app open, or the periodic NotificationCheckWorker pass) it only processes
 * whatever's new since the last successful run. Without this, the app would only ever see NEW
 * messages arriving after install (via SmsReceiver) - which isn't what "automatic expense
 * capture" means to a real user who already has months of transaction history sitting in their
 * inbox. This was a real gap between the architecture doc (Section 2's user flow explicitly
 * says "App scans existing transaction SMS + listens for new ones") and the first pass of the
 * code, which only implemented the second half.
 *
 * This used to be gated by a single one-time boolean flag ("has this device ever been scanned"),
 * set only after the entire inbox had been processed. That was a real, confirmed bug: a pilot
 * tester's ledger stopped updating after a specific date and never recovered, because whatever
 * interrupted the scan partway (a slow device working through months of history, the OS
 * suspending/freezing the app mid-scan - the same class of background-reliability risk already
 * flagged for live capture in docs/coders-documentation/day-1.md Section 7) still left the flag
 * unset, so `scanIfNeeded` correctly kept retrying - but each retry re-scanned the ENTIRE inbox
 * from scratch, which is slow and, on some devices, seemingly never got to actually finish
 * before being interrupted again. The fix: track a `last_scanned_date` watermark and persist it
 * after every single message, not just at the end. An interruption now only costs the messages
 * since the last watermark update, not the whole history, and every call (whether it's the
 * first-ever scan or the 500th periodic catch-up) is cheap and safe to run unconditionally.
 */
object SmsHistoryScanner {

    private const val PREFS_NAME = "sms_history_scan"
    private const val KEY_LAST_SCANNED_DATE = "last_scanned_date"

    /**
     * A destructive migration (fallbackToDestructiveMigration) wipes the transactions table but
     * leaves this watermark untouched in its own SharedPreferences file - without resetting it,
     * scanIfNeeded would believe everything before "now" was already handled and silently strand
     * the user with an empty ledger after any schema bump. AppDatabase's onDestructiveMigration
     * callback calls this to keep the two in sync.
     */
    fun resetScanFlag(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_SCANNED_DATE, 0L).apply()
    }

    suspend fun scanIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastScannedDate = prefs.getLong(KEY_LAST_SCANNED_DATE, 0L)

        val db = AppDatabase.getInstance(context)
        val parser = TransactionParser()

        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            "${Telephony.Sms.DATE} > ?",
            arrayOf(lastScannedDate.toString()),
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
                // Persisted after EVERY message, not once at the end - this is the actual fix.
                // If anything interrupts the scan right here, the next call resumes from this
                // exact point instead of either being stuck forever or restarting from zero.
                prefs.edit().putLong(KEY_LAST_SCANNED_DATE, date).apply()
            }
        }

        SpendWidgetProvider.updateAll(context)
    }
}
