package com.lifeos.expensecapture.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.categorization.AiCategorySuggester
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** All/Income/Expense filter chips (2026-08 reference mockups, `ui2/` folder). */
enum class LedgerDirectionFilter { ALL, INCOME, EXPENSE }

/** One AI-suggested category for one still-Uncategorized transaction (2026-08) - see
 * AiCategorySuggester's kdoc for why this is a one-time cold-start assist, not a recurring
 * dependency: accepting one seeds a real merchant_rule via TransactionRepository.recategorize,
 * so that merchant categorizes deterministically from then on. */
data class CategorySuggestion(
    val transaction: TransactionEntity,
    val suggestedCategoryId: Long,
    val suggestedCategoryName: String
)

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

    private val _suggestions = MutableStateFlow<List<CategorySuggestion>>(emptyList())
    val suggestions: StateFlow<List<CategorySuggestion>> = _suggestions.asStateFlow()
    private val _suggestingInProgress = MutableStateFlow(false)
    val suggestingInProgress: StateFlow<Boolean> = _suggestingInProgress.asStateFlow()
    private val _suggestionError = MutableStateFlow<String?>(null)
    val suggestionError: StateFlow<String?> = _suggestionError.asStateFlow()

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

    /** Cold-start AI category assist (2026-08) - see AiCategorySuggester's kdoc. Batches every
     * currently-Uncategorized transaction's merchant into one AI call; the user reviews and
     * accepts/rejects each suggestion individually (see [acceptSuggestion]/[rejectSuggestion]) -
     * nothing is recategorized without explicit confirmation. */
    fun requestAiCategorySuggestions() {
        val state = uiState.value
        val uncategorizedId = state.categories.firstOrNull { it.name == "Uncategorized" }?.id
        val uncategorizedTransactions = state.allTransactions.filter { it.categoryId == uncategorizedId }
        if (uncategorizedTransactions.isEmpty()) return

        viewModelScope.launch {
            _suggestingInProgress.value = true
            _suggestionError.value = null
            val merchantToCategory = AiCategorySuggester.suggest(
                merchants = uncategorizedTransactions.map { it.merchantRaw },
                categories = state.categories
            )
            _suggestingInProgress.value = false
            if (merchantToCategory.isEmpty()) {
                _suggestionError.value = "Couldn't get AI suggestions right now - check your connection and try again."
                return@launch
            }
            _suggestions.value = uncategorizedTransactions.mapNotNull { transaction ->
                val categoryName = merchantToCategory[transaction.merchantRaw] ?: return@mapNotNull null
                val categoryId = state.categories.firstOrNull { it.name == categoryName }?.id ?: return@mapNotNull null
                if (categoryId == uncategorizedId) return@mapNotNull null
                CategorySuggestion(transaction, categoryId, categoryName)
            }
            if (_suggestions.value.isEmpty()) {
                _suggestionError.value = "AI didn't find a confident category for any of these merchants."
            }
        }
    }

    fun acceptSuggestion(suggestion: CategorySuggestion) {
        viewModelScope.launch {
            repository.recategorize(suggestion.transaction, suggestion.suggestedCategoryId)
        }
        _suggestions.value = _suggestions.value - suggestion
    }

    fun rejectSuggestion(suggestion: CategorySuggestion) {
        _suggestions.value = _suggestions.value - suggestion
    }

    fun dismissSuggestions() {
        _suggestions.value = emptyList()
        _suggestionError.value = null
    }
}
