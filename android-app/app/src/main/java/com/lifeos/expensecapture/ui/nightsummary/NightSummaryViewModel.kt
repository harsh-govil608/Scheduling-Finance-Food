package com.lifeos.expensecapture.ui.nightsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val billsDueTomorrow: List<String> = emptyList()
)

/**
 * Night Summary PRD, Phase 3 Doc 02, scoped to Finance-only content (Productivity/Health
 * pillars don't exist, so this can't yet be the cross-pillar recap the PRD envisions - see
 * day-2.md). The "tomorrow-prep handoff" the PRD calls for is implemented as shared data
 * (bills due tomorrow, read directly from the same tables Home reads) rather than a separate
 * payload object passed to a Morning Dashboard screen, since that screen doesn't exist either -
 * Home already serves as the daily landing surface. "AI was watching" proof point: the
 * auto-captured count, distinct from manual entries.
 */
class NightSummaryViewModel(
    transactionDao: TransactionDao,
    insightsRepository: FinanceInsightsRepository
) : ViewModel() {

    val uiState: StateFlow<NightSummaryUiState> = combine(
        transactionDao.observeAll(),
        insightsRepository.observeBills()
    ) { transactions, bills ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfYesterday = today.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val todayTxns = transactions.filter { it.date >= startOfToday && it.direction == TransactionDirection.DEBIT }
        val yesterdayTxns = transactions.filter {
            it.date in startOfYesterday until startOfToday && it.direction == TransactionDirection.DEBIT
        }
        val tomorrow = today.plusDays(1).dayOfMonth

        NightSummaryUiState(
            todayCount = todayTxns.size,
            todaySpend = todayTxns.sumOf { it.amount },
            yesterdaySpend = yesterdayTxns.sumOf { it.amount },
            autoCapturedCount = todayTxns.count { it.source == com.lifeos.expensecapture.data.db.entity.TransactionSource.SMS_AUTO },
            billsDueTomorrow = bills
                .filter { it.bill.dueDayOfMonth == tomorrow && it.displayStatus != FinanceInsightsRepository.BillDisplayStatus.CANCELLED }
                .map { it.bill.payeeDisplay }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NightSummaryUiState())
}
