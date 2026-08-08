package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection

/**
 * Bank-agnostic fallback used only after every verified per-bank BankTemplate has already failed
 * to match (see TransactionParser.parse). Replaces the old single-regex genericTransactionAlert
 * template, which required a fixed word order ("Rs.500 debited...") and recognized only
 * "debited"/"dr." and "credited"/"cr." - real SMS from banks other than the two verified ones
 * (ICICI, SBI) routinely use "withdrawn", "spent", "paid", "deducted", "purchase", "sent",
 * "transferred" (debit) or "deposited", "received", "refund", "reversed", "added" (credit)
 * instead, in either word order, sometimes with no currency symbol at all - none of which the old
 * template could ever match. Confirmed root cause of a real report (2026-08): a second phone's
 * SMS history scan captured under 10% of real transactions.
 *
 * Three independent signals, all required, instead of one rigid sentence shape:
 *  1. a plausible currency amount, found anywhere in the message (not tied to a fixed position),
 *     with amounts appearing after a balance-context phrase ("Avl Bal", "Available balance")
 *     excluded so a balance figure is never mistaken for the transaction amount itself
 *  2. a debit/credit keyword from the full vocabulary, found anywhere - direction is decided by
 *     whichever keyword sits CLOSEST to the chosen amount, since real bank phrasing always puts
 *     the actual action word immediately next to the amount even when the two can appear in
 *     either order
 *  3. at least one banking-boilerplate signal (account/UPI/reference/balance wording) OR a sender
 *     that already looks institutional (TransactionParser.looksLikeInstitutionalSender) - without
 *     this, a personal text like "I paid Rs 500 for the cab, send it back?" would have both an
 *     amount and a debit keyword and get wrongly recorded as a real transaction. This is the same
 *     false-positive risk the old looksLikeTransactionSms/looksLikeInstitutionalSender split was
 *     built to prevent, just re-applied here now that keyword matching is much broader.
 */
object GenericTransactionExtractor {

    private val DEBIT_KEYWORDS = listOf(
        "debited", "debit", "deducted", "withdrawn", "withdrawal",
        "spent", "paid", "payment", "purchase", "sent", "transferred"
    )
    private val CREDIT_KEYWORDS = listOf(
        "credited", "credit", "deposited", "received", "refund", "reversed", "added"
    )

    /** Perf fix (real founder report, 2026-08: a full-history scan "taking a lot of time" on a
     * real device): findKeywordHits used to build a brand-new Regex (a fresh
     * java.util.regex.Pattern.compile call) for every one of these 18 keywords, on EVERY message
     * this extractor runs against - thousands of redundant compilations across a real inbox scan.
     * Compiled once here instead of inside the per-message loop. */
    private val KEYWORD_PATTERNS: List<Pair<Regex, TransactionDirection>> =
        DEBIT_KEYWORDS.map { Regex("(?i)\\b${Regex.escape(it)}\\b") to TransactionDirection.DEBIT } +
            CREDIT_KEYWORDS.map { Regex("(?i)\\b${Regex.escape(it)}\\b") to TransactionDirection.CREDIT }

    /** Defense-in-depth against the same failure class as
     * TransactionParser.looksLikePromotionalOrMarketing (real founder report, 2026-08:
     * fabricated multi-lakh/crore "transactions" from promotional SMS that slipped past keyword
     * matching) - in case a marketing message doesn't happen to contain any of that function's
     * phrases but still satisfies every other signal this extractor checks. A personal SMS-
     * tracked expense app essentially never sees a single legitimate transaction this large; if
     * one genuinely occurs, Needs Review (not silent auto-insert) is the safe place for it to
     * land - unlike the promotional filter above, this is specific to the unverified generic
     * path, not the structurally-precise verified per-bank templates, which don't need it. */
    private const val MAX_PLAUSIBLE_AMOUNT = 500_000.0

    private val AMOUNT_PATTERN = Regex("(?i)(?:rs\\.?|inr|₹)\\s?([0-9][0-9,]{0,14}(?:\\.\\d{1,2})?)")
    private val BALANCE_CONTEXT = Regex("(?i)(?:avl|available|closing|current)\\.?\\s*bal(?:ance)?|\\bbal\\b\\s*[:.]?\\s*$")
    private val BOILERPLATE_SIGNAL = Regex(
        "(?i)\\b(a/?c|acct|account|upi|ref\\.?\\s*no|reference|txn|transaction|avl\\s*bal|available\\s*bal|imps|neft|rtgs)\\b"
    )

    /** UPI/DR/<ref>/<merchant>/<provider> or UPI/CR/... - a compact reference-line shape some
     * banks/PSPs emit alongside (not instead of) a prose sentence elsewhere in the same SMS.
     * DR/CR here is authoritative for direction and merchant when present - stronger signal than
     * the keyword-proximity heuristic below, since it's an unambiguous structured field rather
     * than free text. */
    private val UPI_REF_PATTERN = Regex(
        "(?i)UPI[/:]?(DR|CR)/([A-Za-z0-9]+)/([^/\\s]+)(?:/([^/\\s]+))?"
    )

    private val TO_MERCHANT = Regex(
        "(?i)(?:\\bto\\b|\\btowards\\b|\\bat\\b)\\s+([A-Za-z0-9@._&\\-\\s]{2,40}?)(?:[.,]|\\bon\\b|\\bref\\b|\\bfor\\b|$)"
    )
    private val FROM_MERCHANT = Regex(
        "(?i)(?:\\bfrom\\b|\\bby\\b)\\s+([A-Za-z0-9@._&\\-\\s]{2,40}?)(?:[.,]|\\bon\\b|\\bref\\b|$)"
    )
    private val VPA_PATTERN = Regex("[\\w.\\-]{2,}@[a-zA-Z]{2,}")

    private val REFERENCE_ID_PATTERN = Regex(
        "(?i)(?:ref(?:erence)?\\.?\\s*(?:no\\.?|id)?|txn\\s*id|upi\\s*ref(?:erence)?\\.?\\s*(?:no\\.?)?)\\s*[:\\-]?\\s*([A-Za-z0-9]{6,})"
    )

    /** Shared with TransactionParser.buildParsed so the verified per-bank templates (ICICI, SBI)
     * benefit from the same reference-id-based dedup signal as this fallback, not just messages
     * that reach this extractor. */
    fun extractReferenceId(body: String): String? =
        UPI_REF_PATTERN.find(body)?.groupValues?.get(2)?.takeIf { it.isNotBlank() }
            ?: REFERENCE_ID_PATTERN.find(body)?.groupValues?.get(1)

    fun extract(sender: String, body: String): ParseResult.Parsed? {
        val upiMatch = UPI_REF_PATTERN.find(body)
        val amountMatches = AMOUNT_PATTERN.findAll(body).filterNot { isBalanceAmount(body, it) }.toList()
        if (amountMatches.isEmpty()) return null

        val hasBoilerplate = BOILERPLATE_SIGNAL.containsMatchIn(body) ||
            TransactionParser.looksLikeInstitutionalSender(sender) ||
            upiMatch != null
        if (!hasBoilerplate) return null

        val direction: TransactionDirection
        val amount: Double
        val merchant: String

        if (upiMatch != null) {
            direction = if (upiMatch.groupValues[1].equals("DR", ignoreCase = true)) {
                TransactionDirection.DEBIT
            } else {
                TransactionDirection.CREDIT
            }
            // Prefer the amount nearest the UPI reference line itself when more than one
            // non-balance amount is present in the message.
            amount = amountMatches.minByOrNull { distance(it.range, upiMatch.range) }
                ?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
            merchant = upiMatch.groupValues[3].ifBlank { "Unknown" }
        } else {
            val keywordHits = findKeywordHits(body)
            if (keywordHits.isEmpty()) return null

            val best = amountMatches
                .flatMap { amt -> keywordHits.map { kw -> Triple(amt, kw.direction, distance(amt.range, kw.range)) } }
                .minByOrNull { it.third } ?: return null

            amount = best.first.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
            direction = best.second
            merchant = extractMerchant(body, direction)
        }

        if (amount <= 0.0 || amount > MAX_PLAUSIBLE_AMOUNT) return null

        return ParseResult.Parsed(
            amount = amount,
            direction = direction,
            merchantRaw = merchant,
            // Below a verified per-bank template match (0.9) since this is pattern-matched across
            // arbitrary bank formats rather than confirmed against a real captured sample - see
            // BankTemplate.kt's own confidence-scoring note.
            confidence = 0.6f,
            bankTemplateName = if (upiMatch != null) "generic_upi_ref" else "generic_v2",
            referenceId = upiMatch?.groupValues?.get(2)?.takeIf { it.isNotBlank() }
                ?: REFERENCE_ID_PATTERN.find(body)?.groupValues?.get(1)
        )
    }

    private fun isBalanceAmount(body: String, match: MatchResult): Boolean {
        val windowStart = (match.range.first - 25).coerceAtLeast(0)
        val context = body.substring(windowStart, match.range.first)
        return BALANCE_CONTEXT.containsMatchIn(context)
    }

    private data class KeywordHit(val range: IntRange, val direction: TransactionDirection)

    private fun findKeywordHits(body: String): List<KeywordHit> {
        val hits = mutableListOf<KeywordHit>()
        KEYWORD_PATTERNS.forEach { (pattern, direction) ->
            pattern.findAll(body).forEach { hits += KeywordHit(it.range, direction) }
        }
        return hits
    }

    private fun distance(a: IntRange, b: IntRange): Int = when {
        a.last < b.first -> b.first - a.last
        b.last < a.first -> a.first - b.last
        else -> 0
    }

    private fun extractMerchant(body: String, direction: TransactionDirection): String {
        val primary = if (direction == TransactionDirection.DEBIT) TO_MERCHANT else FROM_MERCHANT
        primary.find(body)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        val secondary = if (direction == TransactionDirection.DEBIT) FROM_MERCHANT else TO_MERCHANT
        secondary.find(body)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        VPA_PATTERN.find(body)?.value?.let { return it }
        return "Unknown"
    }
}
