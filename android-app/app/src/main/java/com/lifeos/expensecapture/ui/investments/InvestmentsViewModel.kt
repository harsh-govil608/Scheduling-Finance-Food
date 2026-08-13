package com.lifeos.expensecapture.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.InvestmentDao
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import com.lifeos.expensecapture.data.db.entity.InvestmentType
import com.lifeos.expensecapture.finance.AmfiScheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvestmentsViewModel(private val dao: InvestmentDao) : ViewModel() {

    val investments: StateFlow<List<InvestmentEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addManual(name: String, currentValue: Double) {
        viewModelScope.launch { dao.insert(InvestmentEntity(name = name, currentValue = currentValue)) }
    }

    /** Mutual-fund NAV sync (2026-08) - currentValue is computed here just once, at add time
     * (units * the scheme's NAV as of the search that found it); InvestmentSyncTracker keeps it
     * current afterward via periodic sync. */
    fun addMutualFund(scheme: AmfiScheme, units: Double) {
        viewModelScope.launch {
            dao.insert(
                InvestmentEntity(
                    name = scheme.schemeName,
                    currentValue = units * scheme.nav,
                    type = InvestmentType.MUTUAL_FUND,
                    schemeCode = scheme.schemeCode,
                    units = units,
                    lastNavUpdatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun delete(investment: InvestmentEntity) {
        viewModelScope.launch { dao.delete(investment) }
    }
}
