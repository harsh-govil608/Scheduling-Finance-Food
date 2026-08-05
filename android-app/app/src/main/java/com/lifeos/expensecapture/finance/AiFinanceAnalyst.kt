package com.lifeos.expensecapture.finance

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lifeos.expensecapture.assistant.GeminiClient
import com.lifeos.expensecapture.logging.AppLogger

/**
 * AI-augmentation layer on top of [RecurringPatternDetector], not a replacement for it (2026-08) -
 * that detector keeps reliably, instantly, and offline creating every Bill/Subscription row it
 * always has, unchanged. This only reviews merchants the *deterministic* heuristic could never
 * catch by design - [RecurringPatternDetector] requires a 20-40 day average interval, so a real
 * annual insurance premium or a quarterly fee never gets grouped as recurring at all. Every result
 * here is surfaced as a dismissible suggestion the user explicitly confirms before it becomes a
 * real tracked Bill (see FinanceInsightsRepository.trackAiSuggestedBill) - nothing is ever
 * auto-created, and a blank key/network failure/malformed response just means no suggestions this
 * time, never a missing or wrong deterministic number.
 */
object AiFinanceAnalyst {

    /** One merchant's aggregated real transaction history - never raw transaction dumps sent to
     * the model, to keep the prompt small regardless of how many transactions a merchant has. */
    data class MerchantSummary(
        val merchantNormalized: String,
        val merchantDisplay: String,
        val occurrenceCount: Int,
        val amounts: List<Double>,
        val averageIntervalDays: Double
    )

    data class SuggestedBill(
        val merchantNormalized: String,
        val merchantDisplay: String,
        val typicalAmount: Double,
        val reasoning: String
    )

    private val gson = Gson()

    suspend fun findMissedRecurringBills(candidates: List<MerchantSummary>): List<SuggestedBill> {
        if (candidates.isEmpty()) return emptyList()

        val byNormalized = candidates.associateBy { it.merchantNormalized }
        val prompt = buildString {
            append("Merchants (name, how many charges, amounts, average days between charges):\n")
            candidates.forEach { c ->
                append("- ${c.merchantNormalized} | ${c.merchantDisplay} | ${c.occurrenceCount}x | ")
                append(c.amounts.joinToString(", ") { "%.2f".format(it) })
                append(" | avg ${"%.0f".format(c.averageIntervalDays)} days apart\n")
            }
        }

        val content = try {
            GeminiClient.generateText(prompt = prompt, systemInstruction = SYSTEM_PROMPT, jsonMode = true)
        } catch (e: Exception) {
            AppLogger.e("AiFinanceAnalyst", "findMissedRecurringBills failed", e)
            null
        } ?: return emptyList()

        val parsed: List<SuggestionDto>? = try {
            gson.fromJson(content, object : TypeToken<List<SuggestionDto>>() {}.type)
        } catch (e: Exception) {
            null
        }
        val dtoList = parsed ?: return emptyList()

        return dtoList.mapNotNull { dto ->
            val summary = byNormalized[dto.merchantNormalized?.trim()] ?: return@mapNotNull null
            val amount = dto.typicalAmount ?: summary.amounts.average()
            SuggestedBill(
                merchantNormalized = summary.merchantNormalized,
                merchantDisplay = summary.merchantDisplay,
                typicalAmount = amount,
                reasoning = dto.reasoning?.trim().takeUnless { it.isNullOrBlank() } ?: "Looks like a recurring charge"
            )
        }
    }

    private data class SuggestionDto(
        val merchantNormalized: String? = null,
        val typicalAmount: Double? = null,
        val reasoning: String? = null
    )

    private const val SYSTEM_PROMPT = """
        You review a personal finance app's list of merchants that were NOT automatically detected
        as recurring bills (the automatic detector only catches ~20-40 day intervals). Given each
        merchant's name, charge count, amounts, and average days between charges, identify which
        ones genuinely look like a recurring bill the automatic detector would miss - e.g. an
        annual/semi-annual/quarterly charge with a fairly consistent amount, or a clearly periodic
        irregular-interval charge. Do not include one-off purchases, groceries, or anything that
        doesn't look genuinely recurring. Reply with only a JSON array, each element
        {"merchantNormalized": "...", "typicalAmount": 1234.0, "reasoning": "one short sentence"} -
        merchantNormalized must exactly match one of the given merchant names. Empty array if none
        qualify. No explanation, no markdown fence.
    """
}
