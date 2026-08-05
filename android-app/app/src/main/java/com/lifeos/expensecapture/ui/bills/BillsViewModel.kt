package com.lifeos.expensecapture.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillsViewModel(
    private val insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    val bills: StateFlow<List<FinanceInsightsRepository.BillWithComputedStatus>> =
        insightsRepository.observeBills()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiReviewInProgress = MutableStateFlow(false)
    val aiReviewInProgress: StateFlow<Boolean> = _aiReviewInProgress.asStateFlow()
    private val _aiReviewResultMessage = MutableStateFlow<String?>(null)
    val aiReviewResultMessage: StateFlow<String?> = _aiReviewResultMessage.asStateFlow()

    /** AI-augmented bill review (2026-08) - see AiFinanceAnalyst's kdoc. Any merchant Gemini
     * flags lands as a normal DETECTED_UNCONFIRMED row, showing up in the existing "New" section
     * with the same "Yes, track this"/"Not a bill" buttons every deterministically-detected bill
     * already has - that tap is the real confirmation, this call never tracks anything by itself. */
    fun requestAiReview() {
        viewModelScope.launch {
            _aiReviewInProgress.value = true
            val suggestions = insightsRepository.findAiSuggestedBills()
            suggestions.forEach { insightsRepository.trackAiSuggestedBill(it) }
            _aiReviewInProgress.value = false
            _aiReviewResultMessage.value = if (suggestions.isEmpty()) {
                "AI didn't find any additional recurring bills beyond what's already tracked."
            } else {
                "AI found ${suggestions.size} possible bill${if (suggestions.size == 1) "" else "s"} - " +
                    "review ${if (suggestions.size == 1) "it" else "them"} below."
            }
        }
    }

    fun dismissAiReviewMessage() {
        _aiReviewResultMessage.value = null
    }

    fun confirm(item: FinanceInsightsRepository.BillWithComputedStatus) {
        viewModelScope.launch { insightsRepository.confirmBill(item.bill) }
    }

    fun dismiss(item: FinanceInsightsRepository.BillWithComputedStatus) {
        viewModelScope.launch { insightsRepository.dismissBill(item.bill) }
    }

    fun delete(item: FinanceInsightsRepository.BillWithComputedStatus) {
        viewModelScope.launch { insightsRepository.deleteBill(item.bill) }
    }

    fun addManual(payeeDisplay: String, typicalAmount: Double, dueDayOfMonth: Int) {
        viewModelScope.launch { insightsRepository.addManualBill(payeeDisplay, typicalAmount, dueDayOfMonth) }
    }
}
