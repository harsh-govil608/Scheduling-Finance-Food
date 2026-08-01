package com.lifeos.expensecapture.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** All/Income/Expense filter chips (2026-08 reference mockups, `ui2/` folder). */
enum class LedgerDirectionFilter { ALL, INCOME, EXPENSE }

data class LedgerUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val directionFilter: LedgerDirectionFilter = LedgerDirectionFilter.ALL
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

    private val searchQuery = MutableStateFlow("")
    private val directionFilter = MutableStateFlow(LedgerDirectionFilter.ALL)

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeLedger(),
        repository.observeCategories(),
        searchQuery,
        directionFilter
    ) { transactions, categories, query, filter ->
        val filtered = transactions
            .filter {
                when (filter) {
                    LedgerDirectionFilter.ALL -> true
                    LedgerDirectionFilter.INCOME -> it.direction == TransactionDirection.CREDIT
                    LedgerDirectionFilter.EXPENSE -> it.direction == TransactionDirection.DEBIT
                }
            }
            .filter { query.isBlank() || it.merchantRaw.contains(query, ignoreCase = true) }
        LedgerUiState(
            allTransactions = transactions,
            transactions = filtered,
            categories = categories,
            searchQuery = query,
            directionFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState()
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDirectionFilter(filter: LedgerDirectionFilter) {
        directionFilter.value = filter
    }

    fun recategorize(transaction: TransactionEntity, newCategoryId: Long) {
        viewModelScope.launch {
            repository.recategorize(transaction, newCategoryId)
        }
    }

    fun addManual(
        amount: Double,
        merchant: String,
        direction: TransactionDirection,
        categoryId: Long,
        date: Long
    ) {
        viewModelScope.launch {
            repository.addManualTransaction(
                amount = amount,
                direction = direction,
                merchant = merchant,
                categoryId = categoryId,
                date = date
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
