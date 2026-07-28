package com.lifeos.expensecapture.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.util.TransactionSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    /** Bug fix (found via a real user report, 2026-07): category name search never worked
     * because nothing here resolved a transaction's categoryId to a name for TransactionSearch
     * to match against - see its kdoc. */
    val categories: StateFlow<List<CategoryEntity>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<TransactionEntity>> = combine(
        _query, transactionDao.observeAll(), categories
    ) { q, transactions, categoryList ->
        if (q.isBlank()) {
            emptyList()
        } else {
            TransactionSearch.search(q, transactions) { categoryId ->
                categoryList.firstOrNull { it.id == categoryId }?.name ?: ""
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
