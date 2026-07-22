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
     * Verified against real ICICI Bank UPI-debit SMS captured during pilot testing, e.g.:
     * "ICICI Bank Acct XX910 debited for Rs 1.00 on 23-Jul-26; Blinkit credited. UPI:657096314469.
     *  Call 18002662 for dispute. SMS BLOCK 910 to 9215676766."
     *
     * This format is structurally different from a generic guess in two ways that mattered:
     * (1) "debited" appears BEFORE the amount, not after; (2) there's no "to/towards/at" before
     * the merchant - it's "; MERCHANT credited." (the SMS describes the recipient's side of the
     * UPI transfer with "credited", even though this message is about YOUR account being
     * debited). The genericTransactionAlert template below does not match this shape at all,
     * which is exactly why the first live-transaction test silently failed to parse.
     *
     * The credit pattern is NOT verified against a real sample yet (no incoming-credit SMS
     * captured during testing) - it's a reasonable guess based on common ICICI phrasing and
     * should be corrected against a real sample the next time one arrives.
     */
    val iciciBank = BankTemplate(
        name = "icici_bank",
        senderPatterns = listOf(
            Regex("(?i)-ICICIT-S$"), // observed: AD-ICICIT-S, AX-ICICIT-S (prefix varies by carrier)
            Regex("(?i)icici")
        ),
        debitPattern = Regex(
            "(?i)debited\\s+for\\s+(?:Rs\\.?|INR)\\s?([0-9,]+(?:\\.\\d{1,2})?)\\s+on\\s+[^;]+;\\s*" +
                "(.+?)\\s+credited"
        ),
        creditPattern = Regex(
            // UNVERIFIED - update against a real sample once one is captured.
            "(?i)credited\\s+with\\s+(?:Rs\\.?|INR)\\s?([0-9,]+(?:\\.\\d{1,2})?)\\s+.*?(?:from|by)\\s+" +
                "([A-Za-z0-9@._\\-\\s]+?)(?:\\.|,|\\son\\s|$)"
        ),
        merchantExtractor = { match -> match.groupValues.getOrElse(2) { "Unknown" }.trim() }
    )

    /**
     * Loosely matches the common shape of OTHER Indian bank/UPI transaction alerts, e.g.
     * "Rs.250.00 debited from A/c XX1234 on 12-07-26 to merchant@upi. Ref No 123456789".
     * This is a starting-point guess, NOT verified against a real sample - unlike iciciBank
     * above. Replace/extend with a verified template as soon as you have real SMS text from
     * whichever other banks your pilot cohort uses; do not assume this one actually works.
     */
    val genericTransactionAlert = BankTemplate(
        name = "generic_transaction_alert",
        senderPatterns = listOf(
            Regex("(?i)^[A-Z]{2}-[A-Z0-9]+$"), // typical DLT sender-ID shape, e.g. "VM-HDFCBK"
            Regex("(?i)bank|upi|hdfc|sbi|axis|kotak", RegexOption.IGNORE_CASE)
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

    // Order matters: more specific/verified templates first, so a message matching both a
    // specific bank's shape and the generic fallback's loose keyword match resolves correctly.
    val all = listOf(iciciBank, genericTransactionAlert)
}
