package com.lifeos.expensecapture.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class AnalyticsUiState(
    val hasData: Boolean = false,
    val totalSpentThisMonth: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val categoryBreakdown: List<CategorySlice> = emptyList(),
    val monthLabels: List<String> = emptyList(),
    val monthlyExpenses: List<Float> = emptyList(),
    val monthlyIncome: List<Float> = emptyList(),
    val topMerchants: List<Pair<String, Double>> = emptyList()
)

data class CategorySlice(val categoryName: String, val amount: Double)

/**
 * Analytics pillar landing surface (real user request: "ek analytics bhi hona chahiye jaise
 * home and finance hai... dashboards, charts aur analysis curve") - a third pillar alongside
 * Finance and Home, not folded into either, per that request. Every figure here is a real
 * aggregation over TransactionDao's own data (the same source Home/Budget/Review already read),
 * not a separate computation that could drift from what those screens show.
 */
class AnalyticsViewModel(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        transactionDao.observeAll(),
        categoryDao.observeAll()
    ) { transactions, categories ->
        if (transactions.isEmpty()) return@combine AnalyticsUiState()

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val categoryNameById = categories.associate { it.id to it.name }

        val thisMonthTxns = transactions.filter { it.date >= monthStart }
        val spentThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        val incomeThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }

        // Top 6 categories by spend this month, real amounts - no "Other" bucket invented from
        // categories that don't exist; a 7th+ category's spend is genuinely just not broken out
        // in the chart, same tradeoff every "top N" list in this app already makes elsewhere.
        val categoryBreakdown = thisMonthTxns
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { categoryNameById[it.categoryId] ?: "Uncategorized" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(6)
            .map { CategorySlice(it.key, it.value) }

        val topMerchants = thisMonthTxns
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { it.merchantRaw }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        // Last 6 calendar months including the current one, oldest first - a real monthly
        // aggregation, not month-over-month percent deltas (SpendingInsightEngine already owns
        // that framing elsewhere); this is the plain totals a trend chart needs.
        val monthLabels = mutableListOf<String>()
        val monthlyExpenses = mutableListOf<Float>()
        val monthlyIncome = mutableListOf<Float>()
        for (offset in 5 downTo 0) {
            val monthDate = today.minusMonths(offset.toLong()).withDayOfMonth(1)
            val start = monthDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = monthDate.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val monthTxns = transactions.filter { it.date in start until end }
            monthLabels.add(monthDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
            monthlyExpenses.add(monthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }.toFloat())
            monthlyIncome.add(monthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }.toFloat())
        }

        AnalyticsUiState(
            hasData = true,
            totalSpentThisMonth = spentThisMonth,
            totalIncomeThisMonth = incomeThisMonth,
            categoryBreakdown = categoryBreakdown,
            monthLabels = monthLabels,
            monthlyExpenses = monthlyExpenses,
            monthlyIncome = monthlyIncome,
            topMerchants = topMerchants
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())
}
