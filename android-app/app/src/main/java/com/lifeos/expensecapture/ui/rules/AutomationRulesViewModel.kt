package com.lifeos.expensecapture.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RuleRow(val rule: MerchantRuleEntity, val categoryName: String)

/**
 * Automation Rules PRD, Phase 3 Doc 34. Two origins shown here (see MerchantRuleEntity kdoc):
 * rules the AI silently learned from a correction (createdFromUserCorrection), and rules the
 * user explicitly authored here (isManuallyAuthored) - the latter is what this PRD is actually
 * about; the former is the Automation Philosophy's own learning, just made visible for trust.
 */
class AutomationRulesViewModel(
    private val merchantRuleDao: MerchantRuleDao,
    categoryDaoFlow: CategoryDao
) : ViewModel() {

    val rules: StateFlow<List<RuleRow>> = combine(
        merchantRuleDao.observeAll(),
        categoryDaoFlow.observeAll()
    ) { rules, categories ->
        rules.map { rule ->
            RuleRow(rule, categories.firstOrNull { it.id == rule.categoryId }?.name ?: "Uncategorized")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryDaoFlow.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createRule(merchantPattern: String, categoryId: Long) {
        viewModelScope.launch {
            merchantRuleDao.upsert(
                MerchantRuleEntity(
                    merchantPattern = merchantPattern.trim().lowercase(),
                    categoryId = categoryId,
                    createdFromUserCorrection = false,
                    isManuallyAuthored = true
                )
            )
        }
    }

    fun togglePause(rule: MerchantRuleEntity) {
        viewModelScope.launch { merchantRuleDao.update(rule.copy(isPaused = !rule.isPaused)) }
    }

    fun changeCategory(rule: MerchantRuleEntity, categoryId: Long) {
        viewModelScope.launch { merchantRuleDao.update(rule.copy(categoryId = categoryId)) }
    }

    fun delete(rule: MerchantRuleEntity) {
        viewModelScope.launch { merchantRuleDao.delete(rule) }
    }
}
