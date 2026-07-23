package com.lifeos.expensecapture.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.util.TransactionSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(transactionDao: TransactionDao) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val results: StateFlow<List<TransactionEntity>> = combine(_query, transactionDao.observeAll()) { q, transactions ->
        if (q.isBlank()) emptyList() else TransactionSearch.search(q, transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
