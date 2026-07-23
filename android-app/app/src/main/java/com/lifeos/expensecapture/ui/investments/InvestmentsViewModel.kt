package com.lifeos.expensecapture.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.InvestmentDao
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvestmentsViewModel(private val dao: InvestmentDao) : ViewModel() {

    val investments: StateFlow<List<InvestmentEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, currentValue: Double) {
        viewModelScope.launch { dao.insert(InvestmentEntity(name = name, currentValue = currentValue)) }
    }

    fun delete(investment: InvestmentEntity) {
        viewModelScope.launch { dao.delete(investment) }
    }
}
