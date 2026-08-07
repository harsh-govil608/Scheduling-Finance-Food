package com.lifeos.expensecapture.sms.parser

import com.google.gson.Gson
import com.lifeos.expensecapture.assistant.AiClient
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.logging.AppLogger

/**
 * AI fallback for banks TransactionParser's regex templates don't cover (2026-08, real founder
 * request: "hum saare sms saare bank ka track kaise karenge" - writing a verified regex template
 * per bank, the way ICICI/SBI already have one, doesn't scale to the ~50+ banks/UPI apps a real
 * user base will actually have between them).
 *
 * Only ever reached AFTER the deterministic templates already failed (see
 * TransactionIngestor.ingest), and only for senders that already look institutional - the same
 * gate Needs Review already uses (TransactionParser.looksLikeInstitutionalSender) - so this never
 * spends an AI call on personal texts or generic promotional noise. The regex templates stay
 * primary: instant, free, fully offline, and (for ICICI/SBI) verified against real captured SMS -
 * this is strictly a second-chance path, not a replacement.
 *
 * Grounded strictly, same discipline as every other AI feature in this app: the model extracts
 * fields from the ONE real SMS text given, never invents one, and is explicitly told to say
 * "not a transaction" rather than guess when uncertain. Returns null on any failure, blank key,
 * network error, malformed response, or the model's own "not confident" - the caller's existing
 * Needs Review fallback handles all of those identically to today, so a missing/invalid AI result
 * never produces a wrong or fabricated transaction.
 */
object AiSmsParser {
    private val gson = Gson()

    suspend fun tryParse(body: String): ParseResult.Parsed? {
        val content = try {
            AiClient.generateText(prompt = body, systemInstruction = SYSTEM_PROMPT, jsonMode = true)
        } catch (e: Exception) {
            AppLogger.e("AiSmsParser", "tryParse failed", e)
            null
        } ?: return null

        val dto: ExtractionDto? = try {
            gson.fromJson(content, ExtractionDto::class.java)
        } catch (e: Exception) {
            null
        }
        if (dto == null || !dto.isTransaction) return null
        val amount = dto.amount?.takeIf { it > 0.0 } ?: return null
        val direction = when (dto.direction?.trim()?.uppercase()) {
            "DEBIT" -> TransactionDirection.DEBIT
            "CREDIT" -> TransactionDirection.CREDIT
            else -> return null
        }
        val merchant = dto.merchant?.trim().takeUnless { it.isNullOrBlank() } ?: "Unknown"

        return ParseResult.Parsed(
            amount = amount,
            direction = direction,
            merchantRaw = merchant,
            // Scored below a regex template match (0.9) - AI-extracted, not verified against a
            // known-correct pattern. Still well above the 0.4 "amount not found" floor, since a
            // real amount+direction were confidently extracted.
            confidence = 0.7f,
            bankTemplateName = "ai_fallback"
        )
    }

    private data class ExtractionDto(
        val isTransaction: Boolean = false,
        val amount: Double? = null,
        val direction: String? = null,
        val merchant: String? = null
    )

    private const val SYSTEM_PROMPT = """
        You read one SMS message from a bank or UPI payment app and decide whether it's a genuine
        account debit or credit transaction alert - NOT an OTP, verification code, balance
        inquiry, promotional offer, or anything else. Reply with ONLY a JSON object, no markdown
        fence, no explanation:
        - If it IS a genuine transaction: {"isTransaction": true, "amount": 1234.56, "direction":
          "DEBIT" or "CREDIT", "merchant": "the merchant/person/UPI ID actually named in the
          message, or empty string if none is present"}
        - If it is NOT a transaction, or you are not confident it is: {"isTransaction": false}
        Never invent an amount or merchant that isn't actually written in the message. When in
        doubt, respond isTransaction: false rather than guessing.
    """
}
