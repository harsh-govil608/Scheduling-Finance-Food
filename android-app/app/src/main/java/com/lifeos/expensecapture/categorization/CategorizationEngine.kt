package com.lifeos.expensecapture.categorization

import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao

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
    suspend fun categorize(merchantRaw: String): Long {
        val normalized = merchantRaw.trim().lowercase()
        val existingRule = merchantRuleDao.getAll().firstOrNull { rule ->
            normalized.contains(rule.merchantPattern.lowercase())
        }
        if (existingRule != null) return existingRule.categoryId

        return categoryDao.getUncategorized()?.id
            ?: error("Uncategorized category missing - seeding did not run (see App.onCreate)")
    }
}
