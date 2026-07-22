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
        "Uncategorized"
    )

    fun asEntities(): List<CategoryEntity> = names.map { CategoryEntity(name = it, isSystemDefault = true) }
}
