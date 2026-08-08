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
     * The credit pattern was originally an unverified guess. It's now confirmed against a real
     * incoming-credit SMS captured the same day the SBI template was added - a UPI transfer out
     * of an SBI account landed as this ICICI credit on the receiving end (same UPI reference
     * number on both messages, which is how the match was found):
     * "Dear Customer, Acct XX910 is credited with Rs 1.00 on 25-Jul-26 from Sohom Jana.
     *  UPI:620647267681-ICICI Bank." - parses correctly with no changes needed.
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
            "(?i)credited\\s+with\\s+(?:Rs\\.?|INR)\\s?([0-9,]+(?:\\.\\d{1,2})?)\\s+.*?(?:from|by)\\s+" +
                "([A-Za-z0-9@._\\-\\s]+?)(?:\\.|,|\\son\\s|$)"
        ),
        merchantExtractor = { match -> match.groupValues.getOrElse(2) { "Unknown" }.trim() }
    )

    /**
     * Verified against a real SBI UPI-debit SMS captured during pilot testing:
     * "Dear UPI user A/C X5359 debited by 1.00 on date 25Jul26 trf to harshgovil460@ok Refno
     *  620647267681 If not u? call-1800111109 for other services-18001234-SBI"
     *
     * Two things that mattered, found only by matching against this real sample: (1) there is
     * no "Rs"/"INR" prefix before the amount at all here - just "debited by 1.00" - unlike both
     * other templates, which assume a currency prefix always exists; (2) the "merchant" SBI's
     * UPI alert gives you is a raw VPA ("harshgovil460@ok"), not a resolved friendly name the
     * way ICICI's format resolves "Blinkit". That's an honest limitation of this template, not
     * a bug: SBI transactions will show the raw UPI ID in the ledger and land in
     * "Uncategorized" until a user corrects one once, after which a MerchantRule keyed on that
     * exact VPA auto-categorizes it going forward.
     *
     * senderPatterns below are an UNVERIFIED guess at common SBI DLT sender-ID conventions
     * (SBIUPI/SBIINB/etc.) - only the message body was available to verify against, not the
     * actual sender ID. This does not block correct parsing today: TransactionParser's
     * candidate filter also matches on body keywords ("debited"/"upi"/"a/c"), which this
     * message satisfies regardless of sender - see TransactionParser.looksLikeTransactionSms.
     *
     * The credit pattern is NOT verified - no real incoming-UPI-credit SBI SMS was available -
     * it mirrors the debit structure as a best guess and should be corrected against a real
     * sample the next time one arrives, the same way ICICI's credit pattern is flagged.
     */
    val sbiBank = BankTemplate(
        name = "sbi",
        senderPatterns = listOf(
            Regex("(?i)-SBIUPI-?S?$"),
            Regex("(?i)-SBIINB-?S?$"),
            Regex("(?i)sbi")
        ),
        debitPattern = Regex(
            "(?i)debited\\s+by\\s+([0-9,]+(?:\\.\\d{1,2})?)\\s+on\\s+date\\s+\\S+\\s+trf\\s+to\\s+(.+?)\\s+Refno"
        ),
        creditPattern = Regex(
            // UNVERIFIED - update against a real sample once one is captured.
            "(?i)credited\\s+by\\s+([0-9,]+(?:\\.\\d{1,2})?)\\s+on\\s+date\\s+\\S+\\s+by\\s+(.+?)\\s+Refno"
        ),
        merchantExtractor = { match -> match.groupValues.getOrElse(2) { "Unknown" }.trim() }
    )

    // Order matters: more specific/verified templates first, so a message matching both a
    // specific bank's shape and a looser fallback resolves to the verified one. Any bank not
    // covered by a specific template above falls through to GenericTransactionExtractor (see
    // TransactionParser.parse) rather than one more single-regex guess - a fixed sentence shape
    // (the old genericTransactionAlert here) could never cover the real variety of Indian bank/
    // UPI SMS wording; see GenericTransactionExtractor's kdoc for why.
    val all = listOf(iciciBank, sbiBank)
}
