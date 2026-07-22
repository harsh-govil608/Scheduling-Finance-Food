package com.lifeos.expensecapture.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LedgerUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

/**
 * Constructed manually (see LedgerScreen) rather than via a ViewModelProvider.Factory -
 * a deliberate simplification for the pilot scaffold. Practical effect: on process death /
 * config change without retained state, this VM is recreated rather than restored, but all
 * state derives from the Room DB via Flow anyway, so nothing is actually lost - it just
 * re-collects from the same persisted source of truth.
 */
class LedgerViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeLedger(),
        repository.observeCategories()
    ) { transactions, categories ->
        LedgerUiState(transactions = transactions, categories = categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState()
    )

    fun recategorize(transaction: TransactionEntity, newCategoryId: Long) {
        viewModelScope.launch {
            repository.recategorize(transaction, newCategoryId)
        }
    }

    fun addManual(amount: Double, merchant: String, direction: TransactionDirection, categoryId: Long) {
        viewModelScope.launch {
            repository.addManualTransaction(
                amount = amount,
                direction = direction,
                merchant = merchant,
                categoryId = categoryId,
                date = System.currentTimeMillis()
            )
        }
    }
}
