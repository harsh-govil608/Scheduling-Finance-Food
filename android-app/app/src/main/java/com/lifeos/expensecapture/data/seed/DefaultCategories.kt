package com.lifeos.expensecapture.data.seed

import com.lifeos.expensecapture.data.db.entity.CategoryEntity

/**
 * Seeded once on first launch (see App.onCreate). "Uncategorized" must always exist -
 * CategorizationEngine falls back to it when no merchant rule matches.
 */
object DefaultCategories {
    val names = listOf(
        "Food & Dining",
        "Groceries",
        "Transport",
        "Shopping",
        "Bills & Utilities",
        "Subscriptions",
        "Health",
        "Entertainment",
        "Rent",
        "Transfers",
        "ATM & Cash",
        "Salary/Income",
        // Added 2026-07-31, found via real user reviews of comparable apps asking for these
        // exact two categories. Existing installs get these backfilled once by
        // App.backfillNewDefaultCategoriesOnce - this list alone only reaches fresh installs.
        "Travel",
        "Loan & EMI",
        "Uncategorized"
    )

    fun asEntities(): List<CategoryEntity> = names.map { CategoryEntity(name = it, isSystemDefault = true) }
}
