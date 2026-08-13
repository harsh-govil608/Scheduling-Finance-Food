package com.lifeos.expensecapture.categorization

import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection

/**
 * Rule-based only, deliberately - no ML (architecture doc Section 6/10). The merchant_rules
 * table this reads from is seeded entirely by user corrections
 * (see TransactionRepository.recategorize), so accuracy improves the more a real user
 * corrects it - that correction data is the whole point of the pilot.
 */
class CategorizationEngine(
    private val merchantRuleDao: MerchantRuleDao,
    private val categoryDao: CategoryDao
) {
    suspend fun categorize(merchantRaw: String, amount: Double, direction: TransactionDirection): Long {
        val normalized = merchantRaw.trim().lowercase()
        // Paused rules (Automation Rules PRD, Doc 34) are skipped, not deleted.
        val existingRule = merchantRuleDao.getAll().firstOrNull { rule ->
            !rule.isPaused && normalized.contains(rule.merchantPattern.lowercase())
        }
        if (existingRule != null) return existingRule.categoryId

        // Default categorization by amount (2026-08, real user request) - a last-resort fallback
        // only once merchant-rule matching above has already failed, never overriding a learned
        // rule. DEBIT only: applying this to a CREDIT (e.g. a salary deposit or refund) would
        // misclassify income as a Groceries/Shopping expense.
        if (direction == TransactionDirection.DEBIT) {
            val fallbackName = if (amount < AMOUNT_FALLBACK_THRESHOLD) "Groceries" else "Shopping"
            categoryDao.findByName(fallbackName)?.let { return it.id }
        }

        return categoryDao.getUncategorized()?.id
            ?: error("Uncategorized category missing - seeding did not run (see App.onCreate)")
    }

    private companion object {
        const val AMOUNT_FALLBACK_THRESHOLD = 500.0
    }
}
