package com.lifeos.expensecapture.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.UnparsedMessageDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import com.lifeos.expensecapture.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnparsedReviewViewModel(
    private val unparsedMessageDao: UnparsedMessageDao,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val unresolved: StateFlow<List<UnparsedMessageEntity>> = unparsedMessageDao.observeUnresolved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun dismiss(message: UnparsedMessageEntity) {
        viewModelScope.launch { unparsedMessageDao.markResolved(message.id) }
    }

    fun convertToTransaction(
        message: UnparsedMessageEntity,
        amount: Double,
        merchant: String,
        direction: TransactionDirection,
        categoryId: Long,
        date: Long
    ) {
        viewModelScope.launch {
            transactionRepository.addManualTransaction(amount, direction, merchant, categoryId, date)
            unparsedMessageDao.markResolved(message.id)
        }
    }
}
