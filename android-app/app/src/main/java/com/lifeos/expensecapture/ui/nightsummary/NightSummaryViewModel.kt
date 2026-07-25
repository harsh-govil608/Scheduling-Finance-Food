package com.lifeos.expensecapture.ui.nightsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class NightSummaryUiState(
    val todayCount: Int = 0,
    val todaySpend: Double = 0.0,
    val yesterdaySpend: Double = 0.0,
    val autoCapturedCount: Int = 0,
    val billsDueTomorrow: List<String> = emptyList(),
    val tasksCompletedToday: Int = 0,
    val habitsMaintainedToday: Int = 0,
    val totalHabits: Int = 0
)

/**
 * Night Summary PRD, Phase 3 Doc 02. Was Finance-only content until the Home pillar existed
 * (see day-2.md) - now includes tasks completed and habits maintained today too, the same
 * "make Home proactive, don't leave it silent" fix applied to Morning Briefing. The
 * "tomorrow-prep handoff" the PRD calls for is implemented as shared data (bills due tomorrow,
 * read directly from the same tables Home reads) rather than a separate payload object passed to
 * a Morning Dashboard screen. "AI was watching" proof point: the auto-captured count, distinct
 * from manual entries.
 */
class NightSummaryViewModel(
    transactionDao: TransactionDao,
    insightsRepository: FinanceInsightsRepository,
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao
) : ViewModel() {

    val uiState: StateFlow<NightSummaryUiState> = combine(
        transactionDao.observeAll(),
        insightsRepository.observeBills(),
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll()
    ) { transactions, bills, tasks, habits, completions ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfYesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val todayTxns = transactions.filter { it.date >= startOfToday && it.direction == TransactionDirection.DEBIT }
        val yesterdayTxns = transactions.filter {
            it.date in startOfYesterday until startOfToday && it.direction == TransactionDirection.DEBIT
        }
        val tomorrow = today.plusDays(1).dayOfMonth

        val tasksCompletedToday = tasks.count { it.completed && (it.completedAt ?: 0) >= startOfToday }
        val todayEpochDay = today.toEpochDay()
        val habitsMaintainedToday = completions.count { it.dateEpochDay == todayEpochDay }

        NightSummaryUiState(
            todayCount = todayTxns.size,
            todaySpend = todayTxns.sumOf { it.amount },
            yesterdaySpend = yesterdayTxns.sumOf { it.amount },
            autoCapturedCount = todayTxns.count { it.source == com.lifeos.expensecapture.data.db.entity.TransactionSource.SMS_AUTO },
            billsDueTomorrow = bills
                .filter { it.bill.dueDayOfMonth == tomorrow && it.displayStatus != FinanceInsightsRepository.BillDisplayStatus.CANCELLED }
                .map { it.bill.payeeDisplay },
            tasksCompletedToday = tasksCompletedToday,
            habitsMaintainedToday = habitsMaintainedToday,
            totalHabits = habits.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NightSummaryUiState())
}
