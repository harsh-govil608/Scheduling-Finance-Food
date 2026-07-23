package com.lifeos.expensecapture.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscriptionsViewModel(
    private val insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    val subscriptions: StateFlow<List<FinanceInsightsRepository.SubscriptionWithComputedStatus>> =
        insightsRepository.observeSubscriptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun confirm(item: FinanceInsightsRepository.SubscriptionWithComputedStatus) {
        viewModelScope.launch { insightsRepository.confirmSubscription(item.subscription) }
    }

    fun dismiss(item: FinanceInsightsRepository.SubscriptionWithComputedStatus) {
        viewModelScope.launch { insightsRepository.dismissSubscription(item.subscription) }
    }

    fun addManual(merchantDisplay: String, amount: Double, cadenceDays: Int) {
        viewModelScope.launch { insightsRepository.addManualSubscription(merchantDisplay, amount, cadenceDays) }
    }
}
