package com.lifeos.expensecapture.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * BroadcastReceiver.onReceive has a very short execution window and Room access needs a
 * background thread anyway, so this just hands each message off to WorkManager rather than
 * parsing inline.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in messages) {
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody ?: continue

            val work = OneTimeWorkRequestBuilder<ParseIncomingSmsWorker>()
                .setInputData(
                    workDataOf(
                        ParseIncomingSmsWorker.KEY_SENDER to sender,
                        ParseIncomingSmsWorker.KEY_BODY to body
                    )
                )
                .build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
