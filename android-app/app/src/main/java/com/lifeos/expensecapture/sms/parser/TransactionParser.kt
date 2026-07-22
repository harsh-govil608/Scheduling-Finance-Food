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
}
