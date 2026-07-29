package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

/**
 * Layered rule engine (architecture doc Section 6):
 *   1) sender-ID / keyword match narrows candidate templates
 *   2) debit/credit regex match against each candidate
 *   3) confidence scored from whether a plausible amount was extracted
 *
 * Deliberately NOT ML. Bank/UPI SMS formats are regular enough for rules to work at pilot
 * scale, and this keeps parsing fully on-device - the raw SMS text is never transmitted
 * (see architecture doc Section 3 and Section 9's data-minimization principle).
 */
class TransactionParser(
    private val templates: List<BankTemplate> = BankTemplates.all
) {
    fun parse(sender: String, body: String): ParseResult {
        if (looksLikeOtpOrVerification(body)) {
            return ParseResult.Ignored(reason = "otp_or_verification")
        }

        val candidateTemplates = templates.filter { template ->
            template.senderPatterns.any { it.containsMatchIn(sender) } || looksLikeTransactionSms(body)
        }

        if (candidateTemplates.isEmpty()) {
            return ParseResult.Unparsed(body, reason = "no_matching_template")
        }

        for (template in candidateTemplates) {
            template.debitPattern.find(body)?.let { match ->
                return buildParsed(match, template, TransactionDirection.DEBIT)
            }
            template.creditPattern.find(body)?.let { match ->
                return buildParsed(match, template, TransactionDirection.CREDIT)
            }
        }

        return ParseResult.Unparsed(body, reason = "template_matched_but_no_regex_hit")
    }

    private fun buildParsed(
        match: MatchResult,
        template: BankTemplate,
        direction: TransactionDirection
    ): ParseResult.Parsed {
        val amountRaw = match.groupValues.getOrElse(1) { "0" }.replace(",", "")
        val amount = amountRaw.toDoubleOrNull() ?: 0.0
        val merchant = template.merchantExtractor(match)
        // Deliberately simple scoring for the pilot - refine once real failure-rate data
        // exists (architecture doc Section 11).
        val confidence = if (amount > 0.0) 0.9f else 0.4f

        return ParseResult.Parsed(
            amount = amount,
            direction = direction,
            merchantRaw = merchant,
            confidence = confidence,
            bankTemplateName = template.name
        )
    }

    private fun looksLikeTransactionSms(body: String): Boolean {
        val keywords = listOf("debited", "credited", "debit", "credit", "upi", "a/c", "account")
        return keywords.any { body.contains(it, ignoreCase = true) }
    }

    /** See ParseResult.Ignored's kdoc. Checked before any candidate matching so an OTP SMS from
     * a real bank sender (which often shares its sender ID with genuine debit/credit alerts)
     * can't be misread as a transaction just because it mentions the same amount/merchant. */
    private fun looksLikeOtpOrVerification(body: String): Boolean {
        val signals = listOf("otp", "one time password", "one-time password", "verification code", "do not share", "don't share")
        return signals.any { body.contains(it, ignoreCase = true) }
    }

    companion object {
        /**
         * Bank/payment sender-code fragments this pass has real evidence for - extend this list
         * as new banks/payment apps show up in a real user's Needs Review queue. Deliberately a
         * plain substring check against the whole sender code (not a dedicated regex per bank),
         * since Indian bank DLT codes reliably embed a recognizable abbreviation of the
         * institution's name (ICICIT/ICICIB for ICICI, SBIUPI/SBIINB for SBI, AXISBK for Axis,
         * etc.) - this list is the actual bank-recognition mechanism, not the DLT-shape check
         * below, which by itself doesn't distinguish a bank from any other registered business.
         */
        private val BANK_OR_PAYMENT_SENDER_FRAGMENTS = listOf(
            "SBI", "ICICI", "HDFC", "AXIS", "KOTAK", "PNB", "BOB", "BOI", "CANBK", "CANARA",
            "UNION", "UBI", "IDFC", "YESBK", "YESBANK", "INDUS", "FEDBNK", "FEDERAL", "RBL",
            "IDBI", "UCO", "CENTBK", "INDIAN", "KVB", "KARUR", "SIB", "KARB", "DCB", "BANDHAN",
            "AUBANK", "EQUITAS", "JANA", "PAYTM", "PHONPE", "PHONEPE", "GPAY", "AMAZONPAY",
            "MOBIKWIK", "FREECHARGE", "CRED", "SLICE",
            // Found in a real user's actual data, 2026-07: CRIBIN (a rent-payment/property
            // service, seen alongside "Urbanroomz") sends genuine "Payment Successful"/
            // "Payment...has failed" confirmations for real recurring rent payments - real
            // money moving, not noise, even though it isn't a bank itself.
            "CRIBIN"
        )

        /**
         * Product-scale fix (found via a real user report, 2026-07 - "we can't keep showing the
         * person all the sms... this is going to overload my app"): looksLikeTransactionSms
         * above is a deliberately loose net - merely CONTAINING a word like "credit" or
         * "account" was enough for a message to become an Unparsed candidate regardless of who
         * sent it. The DLT-shape check alone (an earlier version of this function) wasn't a
         * strong enough fix either - confirmed against a real user's actual Needs Review data,
         * where a telecom balance alert ("VK-ViCARE-S"), an e-commerce order notice
         * ("CP-blnkit-S"), and course-marketing spam ("VA-UPGRAD-P") all share the exact same
         * DLT-registered sender shape real banks use, since DLT registration is required of
         * every business sending bulk SMS, not just banks. Requiring a recognizable bank/payment
         * name fragment in the sender code as well is what actually separates "a bank" from
         * "any registered business" - confirmed against that same real data (only the genuine
         * Axis/ICICI messages passed both checks; the telecom/e-commerce/edtech noise did not).
         *
         * A plain phone number or a saved contact name still never passes either check. Every
         * SMS is still parsed exactly as before (nothing here restricts what gets READ) - only
         * what gets SHOWN in Needs Review changes. This is a known, intentionally incomplete
         * whitelist, not a claim of covering every Indian bank - missing one means that bank's
         * unparseable messages are silently skipped rather than reviewed, the same class of
         * tradeoff as the OTP-detection heuristic above. Extend the list as new banks appear.
         */
        fun looksLikeInstitutionalSender(sender: String): Boolean {
            val trimmed = sender.trim()
            if (!Regex("(?i)^[A-Z]{2}-[A-Z0-9]{3,10}(-[A-Z])?$").matches(trimmed)) return false
            return BANK_OR_PAYMENT_SENDER_FRAGMENTS.any { trimmed.contains(it, ignoreCase = true) }
        }
    }
}
