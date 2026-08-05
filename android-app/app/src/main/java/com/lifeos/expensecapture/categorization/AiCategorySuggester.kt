package com.lifeos.expensecapture.categorization

import com.google.gson.Gson
import com.lifeos.expensecapture.assistant.AiClient
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.logging.AppLogger

/**
 * One-time cold-start assist (2026-08) for merchants [CategorizationEngine]'s deterministic
 * merchant_rules table has no rule for yet - never a recurring dependency. A single batched
 * AI call maps each uncategorized merchant name to the best-fit *existing* category (never
 * invents a new one); the caller is expected to let the user review/accept each suggestion via
 * [com.lifeos.expensecapture.data.repository.TransactionRepository.recategorize], which itself
 * seeds a real merchant_rule row - so accepting a suggestion once means that merchant categorizes
 * correctly, deterministically, and offline for every future transaction, exactly as if the user
 * had corrected it manually. Returns an empty map on a blank key, network failure, or a response
 * that doesn't parse - callers show "couldn't get suggestions right now" rather than crashing.
 */
object AiCategorySuggester {
    private val gson = Gson()

    suspend fun suggest(merchants: List<String>, categories: List<CategoryEntity>): Map<String, String> {
        val distinctMerchants = merchants.filter { it.isNotBlank() }.distinct()
        val categoryNames = categories.map { it.name }.distinct()
        if (distinctMerchants.isEmpty() || categoryNames.isEmpty()) return emptyMap()

        val prompt = buildString {
            append("Categories: ").append(categoryNames.joinToString(", ")).append("\n")
            append("Merchants:\n")
            distinctMerchants.forEach { append("- ").append(it).append("\n") }
        }

        val content = try {
            AiClient.generateText(prompt = prompt, systemInstruction = SYSTEM_PROMPT, jsonMode = true)
        } catch (e: Exception) {
            AppLogger.e("AiCategorySuggester", "suggest failed", e)
            null
        } ?: return emptyMap()

        val parsed = try {
            gson.fromJson(content, Map::class.java)
        } catch (e: Exception) {
            null
        } ?: return emptyMap()

        val categoryNameSet = categoryNames.toSet()
        return parsed.entries
            .mapNotNull { (merchant, category) ->
                val merchantKey = merchant as? String ?: return@mapNotNull null
                val categoryValue = category as? String ?: return@mapNotNull null
                if (merchantKey !in distinctMerchants || categoryValue !in categoryNameSet) return@mapNotNull null
                merchantKey to categoryValue
            }
            .toMap()
    }

    private const val SYSTEM_PROMPT = """
        You categorize merchant names for a personal finance app. Given a list of existing
        category names and a list of merchant names, reply with only a single JSON object mapping
        each merchant name (exactly as given) to the single best-fit category name from the given
        list - never invent a new category name, never omit a merchant, use "Uncategorized" (if
        present in the list) when nothing fits well. No explanation, no markdown fence, just the
        JSON object.
    """
}
