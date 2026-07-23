package com.lifeos.expensecapture.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillsViewModel(
    private val insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    val bills: StateFlow<List<FinanceInsightsRepository.BillWithComputedStatus>> =
        insightsRepository.observeBills()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun confirm(item: FinanceInsightsRepository.BillWithComputedStatus) {
        viewModelScope.launch { insightsRepository.confirmBill(item.bill) }
    }

    fun dismiss(item: FinanceInsightsRepository.BillWithComputedStatus) {
        viewModelScope.launch { insightsRepository.dismissBill(item.bill) }
    }

    fun addManual(payeeDisplay: String, typicalAmount: Double, dueDayOfMonth: Int) {
        viewModelScope.launch { insightsRepository.addManualBill(payeeDisplay, typicalAmount, dueDayOfMonth) }
    }
}
