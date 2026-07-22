package com.lifeos.expensecapture.sms.parser

/**
 * One entry per known SMS format from a bank or UPI provider.
 *
 * IMPORTANT: only one deliberately generic template ships in this scaffold. Real bank/UPI
 * SMS formats vary enough (and change often enough) that fabricating specific per-bank
 * regexes without real sample messages would be guessing, not engineering. Per the
 * architecture doc Section 6: add templates here based on actual SMS samples from your
 * pilot cohort's 3-5 banks/UPI apps, not broad speculative coverage.
 */
data class BankTemplate(
    val name: String,
    val senderPatterns: List<Regex>,
    val debitPattern: Regex,
    val creditPattern: Regex,
    val merchantExtractor: (MatchResult) -> String
)

object BankTemplates {

    /**
     * Loosely matches the common shape of Indian bank/UPI transaction alerts, e.g.
     * "Rs.250.00 debited from A/c XX1234 on 12-07-26 to merchant@upi. Ref No 123456789".
     * Treat this as a starting point to validate the pipeline end-to-end, not as
     * production-accurate parsing - replace/extend with real templates once you have
     * sample SMS text from actual pilot users' banks.
     */
    val genericTransactionAlert = BankTemplate(
        name = "generic_transaction_alert",
        senderPatterns = listOf(
            Regex("(?i)^[A-Z]{2}-[A-Z0-9]+$"), // typical DLT sender-ID shape, e.g. "VM-HDFCBK"
            Regex("(?i)bank|upi|hdfc|icici|sbi|axis|kotak", RegexOption.IGNORE_CASE)
        ),
        debitPattern = Regex(
            "(?i)(?:Rs\\.?|INR)\\s?([0-9,]+(?:\\.\\d{1,2})?)\\s*" +
                "(?:has been debited|debited|dr\\.?)\\b.*?" +
                "(?:to|towards|at)\\s+([A-Za-z0-9@._\\-\\s]+?)(?:\\.|,|\\son\\s|\\sRef|\\sref|$)"
        ),
        creditPattern = Regex(
            "(?i)(?:Rs\\.?|INR)\\s?([0-9,]+(?:\\.\\d{1,2})?)\\s*" +
                "(?:has been credited|credited|cr\\.?)\\b.*?" +
                "(?:from|by)\\s+([A-Za-z0-9@._\\-\\s]+?)(?:\\.|,|\\son\\s|\\sRef|\\sref|$)"
        ),
        merchantExtractor = { match -> match.groupValues.getOrElse(2) { "Unknown" }.trim() }
    )

    val all = listOf(genericTransactionAlert)
}
