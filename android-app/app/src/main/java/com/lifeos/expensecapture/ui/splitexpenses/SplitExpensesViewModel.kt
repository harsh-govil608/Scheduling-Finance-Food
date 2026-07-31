package com.lifeos.expensecapture.ui.splitexpenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.SplitExpenseDao
import com.lifeos.expensecapture.data.db.dao.SplitParticipantDao
import com.lifeos.expensecapture.data.db.entity.SplitExpenseEntity
import com.lifeos.expensecapture.data.db.entity.SplitParticipantEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SplitExpenseRow(
    val expense: SplitExpenseEntity,
    val participants: List<SplitParticipantEntity>
) {
    val owedToYou: Double get() = participants.filter { !it.settled }.sumOf { it.shareAmount }
    val allSettled: Boolean get() = participants.isNotEmpty() && participants.all { it.settled }
}

/**
 * Split Expenses list - see SplitExpenseEntity's kdoc for the single-device scope. Each row's
 * "owed to you" figure is a live sum over that expense's own participants, not a stored total -
 * it always reflects whichever participants have actually been marked paid so far.
 */
class SplitExpensesViewModel(
    private val splitExpenseDao: SplitExpenseDao,
    private val splitParticipantDao: SplitParticipantDao
) : ViewModel() {

    val rows: StateFlow<List<SplitExpenseRow>> = combine(
        splitExpenseDao.observeAll(),
        splitParticipantDao.observeAll()
    ) { expenses, allParticipants ->
        val byExpense = allParticipants.groupBy { it.splitExpenseId }
        expenses.map { expense -> SplitExpenseRow(expense, byExpense[expense.id].orEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(description: String, totalAmount: Double, date: Long, participants: List<Pair<String, Double>>) {
        if (description.isBlank() || totalAmount <= 0.0 || participants.isEmpty()) return
        viewModelScope.launch {
            val expenseId = splitExpenseDao.insert(
                SplitExpenseEntity(description = description.trim(), totalAmount = totalAmount, date = date)
            )
            splitParticipantDao.insertAll(
                participants.map { (name, share) ->
                    SplitParticipantEntity(splitExpenseId = expenseId, name = name.trim(), shareAmount = share)
                }
            )
        }
    }

    fun deleteExpense(row: SplitExpenseRow) {
        viewModelScope.launch {
            splitParticipantDao.deleteForExpense(row.expense.id)
            splitExpenseDao.delete(row.expense)
        }
    }

    /** The only real "action" this feature has day to day: the device owner telling the app a
     * specific person paid their share back. Nothing automatic detects this - see
     * SplitParticipantEntity's kdoc. */
    fun toggleSettled(participant: SplitParticipantEntity) {
        viewModelScope.launch {
            splitParticipantDao.update(
                participant.copy(
                    settled = !participant.settled,
                    settledAt = if (!participant.settled) System.currentTimeMillis() else null
                )
            )
        }
    }
}
