package com.lifeos.expensecapture.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.BudgetDao
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Manage Categories, reachable from Profile (real user review: "give users to add categories
 * they want or remove categories that they don't want"). Every category - system-default or
 * user-created - can be deleted except "Uncategorized" itself, which CategorizationEngine
 * depends on existing as its own fallback; that guard lives here (deleteCategory) rather than
 * only in the UI, so it holds regardless of what calls this ViewModel.
 */
class CategoriesViewModel(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val merchantRuleDao: MerchantRuleDao,
    private val budgetDao: BudgetDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            categoryDao.insert(CategoryEntity(name = trimmed, isSystemDefault = false))
        }
    }

    /** No-op (not an error) if asked to delete "Uncategorized" - the delete button for it is
     * already hidden in CategoriesScreen, this is the same guarantee at the data layer. */
    fun deleteCategory(category: CategoryEntity) {
        if (category.name == "Uncategorized") return
        viewModelScope.launch {
            val uncategorized = categoryDao.getUncategorized() ?: return@launch
            transactionDao.reassignCategoryToUncategorized(category.id, uncategorized.id)
            merchantRuleDao.reassignCategoryToUncategorized(category.id, uncategorized.id)
            budgetDao.deleteByCategory(category.id)
            categoryDao.delete(category)
        }
    }
}
