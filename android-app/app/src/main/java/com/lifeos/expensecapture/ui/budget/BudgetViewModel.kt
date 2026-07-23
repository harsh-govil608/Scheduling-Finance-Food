package com.lifeos.expensecapture.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val insightsRepository: FinanceInsightsRepository,
    categoriesFlow: Flow<List<CategoryEntity>>
) : ViewModel() {

    val budgets: StateFlow<List<FinanceInsightsRepository.BudgetProgress>> =
        insightsRepository.observeBudgetProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> =
        categoriesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBudget(categoryId: Long?, monthlyLimit: Double) {
        viewModelScope.launch { insightsRepository.setBudget(categoryId, monthlyLimit) }
    }

    fun deleteBudget(progress: FinanceInsightsRepository.BudgetProgress) {
        viewModelScope.launch { insightsRepository.deleteBudget(progress.budget) }
    }

    suspend fun suggestedDefault(categoryId: Long?): Double =
        insightsRepository.suggestedDefaultForCategory(categoryId)
}
