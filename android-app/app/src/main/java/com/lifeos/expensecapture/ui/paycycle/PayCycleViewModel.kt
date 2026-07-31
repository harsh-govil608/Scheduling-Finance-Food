package com.lifeos.expensecapture.ui.paycycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PayCycleUiState(
    /** True once the "Salary/Income" category has at least one real CREDIT transaction in it -
     * everything else here is null/empty until then, since there's no cycle to anchor on. */
    val hasSalaryData: Boolean = false,
    val currentCycleStart: Long? = null,
    val currentCycleIncome: Double = 0.0,
    val currentCycleExpenses: Double = 0.0,
    /** Only set once a second, earlier salary credit exists - a fully bounded [lastCycleStart,
     * lastCycleEnd) range, unlike the current cycle which is still ongoing. */
    val lastCycleStart: Long? = null,
    val lastCycleEnd: Long? = null,
    val lastCycleIncome: Double = 0.0,
    val lastCycleExpenses: Double = 0.0
)

/**
 * Pay Cycle analysis (real user review: "sometimes salary get credited on 28th of month and
 * sometimes 30th day of month... instead of analyzing 1 month... we can analyze income-expense
 * between 2 salary credits"). Deliberately additive, not a replacement for Home's existing
 * calendar-month figures - this reuses the same real transaction data, anchored on the
 * "Salary/Income" category (already a default category, see DefaultCategories.kt) rather than
 * any new ML/heuristic salary-detection, so it only ever "just works" for a user whose salary
 * credits actually land in that category already, and stays honestly empty otherwise rather than
 * guessing at what counts as a salary credit.
 */
class PayCycleViewModel(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao
) : ViewModel() {

    val uiState: StateFlow<PayCycleUiState> = combine(
        transactionDao.observeAll(),
        categoryDao.observeAll()
    ) { transactions, categories ->
        val salaryCategoryId = categories.firstOrNull { it.name == "Salary/Income" }?.id
            ?: return@combine PayCycleUiState()

        val salaryDates = transactions
            .filter { it.categoryId == salaryCategoryId && it.direction == TransactionDirection.CREDIT }
            .map { it.date }
            .sortedDescending()
        if (salaryDates.isEmpty()) return@combine PayCycleUiState()

        val currentCycleStart = salaryDates[0]
        val currentIncome = transactions.filter { it.date >= currentCycleStart && it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
        val currentExpenses = transactions.filter { it.date >= currentCycleStart && it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }

        var lastCycleStart: Long? = null
        var lastCycleEnd: Long? = null
        var lastIncome = 0.0
        var lastExpenses = 0.0
        if (salaryDates.size >= 2) {
            lastCycleStart = salaryDates[1]
            lastCycleEnd = salaryDates[0]
            lastIncome = transactions.filter { it.date in lastCycleStart until lastCycleEnd && it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
            lastExpenses = transactions.filter { it.date in lastCycleStart until lastCycleEnd && it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        }

        PayCycleUiState(
            hasSalaryData = true,
            currentCycleStart = currentCycleStart,
            currentCycleIncome = currentIncome,
            currentCycleExpenses = currentExpenses,
            lastCycleStart = lastCycleStart,
            lastCycleEnd = lastCycleEnd,
            lastCycleIncome = lastIncome,
            lastCycleExpenses = lastExpenses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PayCycleUiState())
}
