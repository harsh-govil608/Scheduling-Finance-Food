package com.lifeos.expensecapture.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.BudgetDao
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.finance.FinancialHealthScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Time-range chips (2026-08 reference mockups, `ui2/` folder) - each maps to a real lower
 * bound on transaction date; ALL has none. */
enum class AnalyticsTimeRange(val label: String) {
    D7("7D"), D30("30D"), M3("3M"), M6("6M"), Y1("1Y"), ALL("All")
}

data class AnalyticsUiState(
    val hasData: Boolean = false,
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.D30,
    val totalSpentThisMonth: Double = 0.0,
    val totalIncomeThisMonth: Double = 0.0,
    val categoryBreakdown: List<CategorySlice> = emptyList(),
    val monthLabels: List<String> = emptyList(),
    val monthlyExpenses: List<Float> = emptyList(),
    val monthlyIncome: List<Float> = emptyList(),
    val topMerchants: List<Pair<String, Double>> = emptyList(),
    /** Share of income not spent, over the selected range - null when the range has no income
     * to divide by (nothing fabricated in its place). */
    val savingsRatePercent: Double? = null,
    val avgDailySpend: Double = 0.0,
    /** Deterministic 0-100 score - see FinancialHealthScore's kdoc for why this isn't AI. */
    val healthScore: Int = 50
)

data class CategorySlice(val categoryName: String, val amount: Double)

/**
 * Analytics pillar landing surface (real user request: "ek analytics bhi hona chahiye jaise
 * home and finance hai... dashboards, charts aur analysis curve") - a third pillar alongside
 * Finance and Home, not folded into either, per that request. Every figure here is a real
 * aggregation over TransactionDao's own data (the same source Home/Budget/Review already read),
 * not a separate computation that could drift from what those screens show.
 *
 * Time-range chips (2026-08, `ui2/` mockups) filter everything below the Spending Overview
 * header off one selected range; totalSpentThisMonth/totalIncomeThisMonth stay calendar-month
 * regardless of range (they're the same figures Finance's Home already shows, kept consistent
 * rather than silently redefined by whatever range chip happens to be selected).
 */
class AnalyticsViewModel(
    transactionDao: TransactionDao,
    categoryDao: CategoryDao,
    budgetDao: BudgetDao
) : ViewModel() {

    private val selectedRange = MutableStateFlow(AnalyticsTimeRange.D30)

    fun selectRange(range: AnalyticsTimeRange) {
        selectedRange.value = range
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        transactionDao.observeAll(),
        categoryDao.observeAll(),
        budgetDao.observeAll(),
        selectedRange
    ) { transactions, categories, budgets, range ->
        if (transactions.isEmpty()) return@combine AnalyticsUiState(selectedRange = range)

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val prevMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val categoryNameById = categories.associate { it.id to it.name }

        val thisMonthTxns = transactions.filter { it.date >= monthStart }
        val spentThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }
        val incomeThisMonth = thisMonthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }
        val prevMonthSpent = transactions
            .filter { it.date in prevMonthStart until monthStart && it.direction == TransactionDirection.DEBIT }
            .sumOf { it.amount }

        val earliestTxnDate = transactions.minOf { it.date }
        val rangeStart = when (range) {
            AnalyticsTimeRange.D7 -> today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalyticsTimeRange.D30 -> today.minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalyticsTimeRange.M3 -> today.minusMonths(3).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalyticsTimeRange.M6 -> today.minusMonths(6).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalyticsTimeRange.Y1 -> today.minusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalyticsTimeRange.ALL -> earliestTxnDate
        }
        val rangeTxns = transactions.filter { it.date >= rangeStart }
        val rangeDebits = rangeTxns.filter { it.direction == TransactionDirection.DEBIT }
        val rangeCredits = rangeTxns.filter { it.direction == TransactionDirection.CREDIT }
        val rangeSpent = rangeDebits.sumOf { it.amount }
        val rangeIncome = rangeCredits.sumOf { it.amount }

        // Top 6 categories by spend in the selected range, real amounts - no "Other" bucket
        // invented from categories that don't exist; a 7th+ category's spend is genuinely just
        // not broken out in the chart, same tradeoff every "top N" list in this app makes.
        val categoryBreakdown = rangeDebits
            .groupBy { categoryNameById[it.categoryId] ?: "Uncategorized" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(6)
            .map { CategorySlice(it.key, it.value) }

        val topMerchants = rangeDebits
            .groupBy { it.merchantRaw }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        val todayEndMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val rangeDays = ((todayEndMillis - rangeStart) / 86_400_000.0).coerceAtLeast(1.0)
        val avgDailySpend = rangeSpent / rangeDays

        // Income vs Expense trend - bucketed so the label row never exceeds 7 entries
        // regardless of range (see AnalyticsViewModel's kdoc on why bucket width scales with
        // range instead of always being "1 month").
        val bucketCount = when (range) {
            AnalyticsTimeRange.D7 -> 7
            AnalyticsTimeRange.D30 -> 6
            AnalyticsTimeRange.M3 -> 3
            AnalyticsTimeRange.M6 -> 6
            AnalyticsTimeRange.Y1 -> 6
            AnalyticsTimeRange.ALL -> 6
        }
        val bucketSpanMillis = (todayEndMillis - rangeStart).coerceAtLeast(bucketCount * 86_400_000L) / bucketCount

        val monthLabels = mutableListOf<String>()
        val monthlyExpenses = mutableListOf<Float>()
        val monthlyIncome = mutableListOf<Float>()
        for (i in 0 until bucketCount) {
            val bucketStart = rangeStart + i * bucketSpanMillis
            val bucketEnd = if (i == bucketCount - 1) Long.MAX_VALUE else rangeStart + (i + 1) * bucketSpanMillis
            val bucketTxns = transactions.filter { it.date in bucketStart until bucketEnd }
            val bucketDate = epochMillisToLocalDate(bucketStart, zone)
            monthLabels.add(
                when (range) {
                    AnalyticsTimeRange.D7, AnalyticsTimeRange.D30 ->
                        "${bucketDate.dayOfMonth}/${bucketDate.monthValue}"
                    else -> bucketDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
            )
            monthlyExpenses.add(bucketTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }.toFloat())
            monthlyIncome.add(bucketTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount }.toFloat())
        }

        val budgetRatios = budgets.mapNotNull { budget ->
            if (budget.monthlyLimit <= 0) return@mapNotNull null
            val relevant = thisMonthTxns.filter {
                it.direction == TransactionDirection.DEBIT &&
                    (budget.categoryId == null || it.categoryId == budget.categoryId)
            }
            relevant.sumOf { it.amount } / budget.monthlyLimit
        }
        val healthScore = FinancialHealthScore.compute(
            FinancialHealthScore.Inputs(
                incomeThisMonth = incomeThisMonth,
                spentThisMonth = spentThisMonth,
                prevMonthSpent = prevMonthSpent,
                budgetUtilizationRatios = budgetRatios
            )
        )

        AnalyticsUiState(
            hasData = true,
            selectedRange = range,
            totalSpentThisMonth = spentThisMonth,
            totalIncomeThisMonth = incomeThisMonth,
            categoryBreakdown = categoryBreakdown,
            monthLabels = monthLabels,
            monthlyExpenses = monthlyExpenses,
            monthlyIncome = monthlyIncome,
            topMerchants = topMerchants,
            savingsRatePercent = if (rangeIncome > 0) ((rangeIncome - rangeSpent) / rangeIncome * 100) else null,
            avgDailySpend = avgDailySpend,
            healthScore = healthScore
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())
}

private fun epochMillisToLocalDate(epochMillis: Long, zone: ZoneId): LocalDate =
    java.time.Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
