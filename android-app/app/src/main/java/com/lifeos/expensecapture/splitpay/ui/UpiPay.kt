package com.lifeos.expensecapture.splitpay.ui

import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * Real Android UPI Intent flow (2026-08 Smart Split) - `upi://pay` is a standard scheme every
 * UPI app (GPay/PhonePe/Paytm/BHIM/...) registers an intent filter for; Android shows its own
 * app-chooser when more than one is installed, exactly the "magic moment" the feature request
 * describes. No payment gateway/merchant account needed for this direction (person-to-person
 * collect via intent) - that's only required for a business to auto-verify money actually
 * arrived, which is why [ParticipantStatus.CLAIMED_PAID] exists as a manual confirm step instead
 * of a real payment-success webhook (see SplitPayRepository's kdoc on that same limit).
 */
object UpiPay {
    /** `am` is formatted to exactly 2 decimals - some UPI apps reject an amount with more or
     * fewer digits after the decimal point. `tn` (transaction note) is URL-encoded since it's
     * free text (an expense description) that can contain spaces/punctuation. */
    fun buildPayUri(payeeVpa: String, payeeName: String, amount: Double, note: String): Uri {
        val encodedNote = URLEncoder.encode(note, "UTF-8")
        val encodedName = URLEncoder.encode(payeeName, "UTF-8")
        return Uri.parse(
            "upi://pay?pa=$payeeVpa&pn=$encodedName&am=${"%.2f".format(amount)}&cu=INR&tn=$encodedNote"
        )
    }

    fun payIntent(payeeVpa: String, payeeName: String, amount: Double, note: String): Intent =
        Intent(Intent.ACTION_VIEW, buildPayUri(payeeVpa, payeeName, amount, note))

    /**
     * UPI apps that cooperate with the "collect via intent" convention return a result string
     * like `Status=SUCCESS&txnId=...&...` (or FAILURE/SUBMITTED) in the launching Activity's
     * result Intent - but this isn't a guaranteed contract every UPI app on every Android version
     * honors, so this is treated as a best-effort signal only. Callers must still offer a manual
     * "Mark as paid" action regardless of what this returns - see SmartSplitDetailScreen.
     */
    fun isSuccessResult(resultData: Intent?): Boolean {
        val response = resultData?.getStringExtra("response") ?: return false
        return response.contains("Status=SUCCESS", ignoreCase = true)
    }

    /** A best-guess VPA format check (`name@bank`) - not exhaustive validation (UPI VPA rules
     * vary by PSP), just enough to catch an obviously-empty or malformed entry before it's saved
     * as someone's payee address. */
    fun looksLikeValidVpa(vpa: String): Boolean {
        val trimmed = vpa.trim()
        return trimmed.isNotEmpty() && trimmed.contains("@") && !trimmed.startsWith("@") && !trimmed.endsWith("@")
    }
}
