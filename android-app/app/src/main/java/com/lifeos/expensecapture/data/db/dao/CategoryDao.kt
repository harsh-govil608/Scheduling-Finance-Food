package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    /** User-created custom categories (real user review: "give users to add categories they
     * want or remove categories that they don't want"). */
    @Insert
    suspend fun insert(category: CategoryEntity): Long

    /** Deleting a category by itself would leave transactions/merchant rules/budgets pointing at
     * a categoryId that no longer resolves to anything - CategoriesViewModel.deleteCategory
     * reassigns/clears all three (via TransactionDao/MerchantRuleDao/BudgetDao's own
     * reassignCategoryToUncategorized/deleteByCategory) before calling this. "Uncategorized"
     * itself is guarded against deletion in the UI - it's CategorizationEngine's own fallback,
     * not just another category. */
    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories WHERE name = 'Uncategorized' LIMIT 1")
    suspend fun getUncategorized(): CategoryEntity?

    /** Lookup by exact category name - used by CategorizationEngine's amount-based fallback and
     * FinanceInsightsRepository's Bills-category-driven detection gate. Categories have no stable
     * public ID (autoGenerate), so a caller that needs "the Groceries category" or "the Bills &
     * Utilities category" specifically has to resolve it by its known seeded name at call time. */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): CategoryEntity?
}
