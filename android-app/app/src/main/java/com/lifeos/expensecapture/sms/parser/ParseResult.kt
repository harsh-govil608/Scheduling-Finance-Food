package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

sealed class ParseResult {
    data class Parsed(
        val amount: Double,
        val direction: TransactionDirection,
        val merchantRaw: String,
        val confidence: Float,
        val bankTemplateName: String,
        /** Bank/UPI reference or transaction ID extracted from the SMS body, when present. A
         * stronger duplicate signal than the exact sender+body hash alone - see
         * TransactionIngestor's dedup logic - because two different SMS (e.g. the bank's and a
         * UPI app's) can describe the exact same real transaction with different wording but the
         * same reference number. */
        val referenceId: String? = null
    ) : ParseResult()

    /**
     * Deliberately not silently dropped (architecture doc Section 6): an unexplained missing
     * transaction is worse for trust than an explicit "couldn't parse this, please confirm."
     * Wiring this into a manual-review queue in the UI is the next increment - see the
     * android-app/README.md known-gaps list.
     */
    data class Unparsed(val rawText: String, val reason: String) : ParseResult()

    /**
     * Bug fix (found via a real user report, 2026-07): SmsReceiver hands every SMS on the
     * device to this parser with no sender allowlist, and the old candidate-matching logic
     * flagged anything merely containing "debit"/"credit"/"upi"/"account" - which OTP and
     * promotional texts routinely do, without ever being an actual transaction. Those used to
     * fall through to Unparsed and pile up in the Needs Review queue as noise. This is a
     * distinct outcome from Unparsed: Unparsed means "looked like a transaction, couldn't read
     * it" (worth a human's attention); Ignored means "confidently never a transaction attempt at
     * all" (safe to drop silently, same as a personal text message never reaching this parser's
     * output). Also closes a subtler risk: a bank OTP SMS for a pending transaction often
     * repeats the same amount/merchant a completed debit alert would - without this check, a
     * false match here could double-count that pending amount as a real transaction. */
    data class Ignored(val reason: String) : ParseResult()
}
