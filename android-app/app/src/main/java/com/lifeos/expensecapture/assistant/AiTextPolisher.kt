package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.logging.AppLogger

/**
 * Shared "make this deterministic sentence sound warmer" layer (2026-08) - every existing
 * narrative/insight surface in this app (SpendingInsightEngine's "What changed", ProductivityInsightEngine's
 * "AI Suggestions", Family Dashboard's insight line, the onboarding first-scan summary) already
 * computes a correct, factual sentence from real numbers with plain rule-based logic; none of that
 * changes. This only rephrases the *finished* sentence via [AiClient] for personality/variety, and
 * always falls back to the original untouched text on a blank key, network failure, or a
 * suspicious-looking response - so a bad/missing API key or no connection never removes an insight
 * that would otherwise have shown, it just shows the plain version instead.
 */
object AiTextPolisher {
    suspend fun polish(factualText: String): String {
        if (factualText.isBlank()) return factualText
        val polished = try {
            AiClient.generateText(prompt = factualText, systemInstruction = SYSTEM_PROMPT)
        } catch (e: Exception) {
            AppLogger.e("AiTextPolisher", "polish failed, using original text", e)
            null
        }
        // A model can still ignore instructions - reject anything wildly longer than the input
        // (a sign it added preamble/invented extra content) rather than trust it blindly.
        return polished?.takeIf { it.isNotBlank() && it.length <= factualText.length * 3 } ?: factualText
    }

    private const val SYSTEM_PROMPT = """
        Rephrase the following factual sentence(s) from a personal finance/productivity app to
        sound warmer and more natural, in at most 2 short sentences. Do not invent, remove, or
        change any number, name, or fact present in the input - only improve the phrasing. Reply
        with only the rephrased text, no preamble, no quotes.
    """
}
