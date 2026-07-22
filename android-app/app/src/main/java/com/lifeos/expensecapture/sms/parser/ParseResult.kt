package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

sealed class ParseResult {
    data class Parsed(
        val amount: Double,
        val direction: TransactionDirection,
        val merchantRaw: String,
        val confidence: Float,
        val bankTemplateName: String
    ) : ParseResult()

    /**
     * Deliberately not silently dropped (architecture doc Section 6): an unexplained missing
     * transaction is worse for trust than an explicit "couldn't parse this, please confirm."
     * Wiring this into a manual-review queue in the UI is the next increment - see the
     * android-app/README.md known-gaps list.
     */
    data class Unparsed(val rawText: String, val reason: String) : ParseResult()
}
