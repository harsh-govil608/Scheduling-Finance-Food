package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.BillEntity
import com.lifeos.expensecapture.data.db.entity.ForecastAccuracyEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * "Learn and Adapt" (2026-08, real user feedback: "it predicts based on history, but if AI
 * learns then it can give more accurate predictions") - see ForecastAccuracyEntity's kdoc for
 * the full context. A standalone object, not a FinanceInsightsRepository method, matching this
 * package's existing convention (RecurringPatternDetector, IncomePatternDetector, ForecastEngine,
 * HabitSpendCorrelator all take raw data/DAOs directly) - avoids adding a constructor param to
 * FinanceInsightsRepository, which has a dozen call sites across the app for something only
 * HomeViewModel and NotificationCheckWorker need to trigger.
 *
 * Deliberately does NOT feed back into RecurringPatternDetector/IncomePatternDetector's fixed
 * thresholds - this phase only records the track record and surfaces it to the AI
 * (FinanceQaEngine) to adjust confidence framing, not to retune detection rules. See this
 * feature's plan doc for why that's a separate, later, higher-risk phase.
 */
object ForecastAccuracyTracker {

    /** Backfills every fully-completed calendar month that had enough prior history to have
     * produced a real forecast at the time, then stays idempotent (checks findByMonthKey) so
     * repeat calls (Home load, periodic worker) only ever add the newly-completed month. Never
     * touches the current in-progress month - there's no "actual" for a month that hasn't
     * finished yet. */
    suspend fun recordCompletedMonths(db: AppDatabase) {
        val allTransactions = db.transactionDao().getSince(0L)
        if (allTransactions.isEmpty()) return

        val subscriptions = db.subscriptionDao().observeAll().first()
        val bills = db.billDao().observeAll().first()
        val zone = ZoneId.systemDefault()
        val currentMonthStart = LocalDate.now(zone).withDayOfMonth(1)
        var month = Instant.ofEpochMilli(allTransactions.minOf { it.date })
            .atZone(zone).toLocalDate().withDayOfMonth(1)

        while (month.isBefore(currentMonthStart)) {
            val monthKey = "%04d-%02d".format(month.year, month.monthValue)
            if (db.forecastAccuracyDao().findByMonthKey(monthKey) == null) {
                recordOneMonth(db, month, monthKey, allTransactions, subscriptions, bills, zone)
            }
            month = month.plusMonths(1)
        }
    }

    private suspend fun recordOneMonth(
        db: AppDatabase,
        month: LocalDate,
        monthKey: String,
        allTransactions: List<TransactionEntity>,
        subscriptions: List<SubscriptionEntity>,
        bills: List<BillEntity>,
        zone: ZoneId
    ) {
        val monthStart = month.atStartOfDay(zone).toInstant().toEpochMilli()
        val monthEndExclusive = month.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()

        // Predict using ONLY data that existed before this month began - simulates what the
        // forecast would honestly have said at the time, not a look-ahead using this month's own
        // actuals. Subscription/bill status is current-state (no historical snapshot of past
        // status exists) - a known approximation, documented rather than hidden.
        val priorTransactions = allTransactions.filter { it.date < monthStart }
        val forecast = ForecastEngine.compute(priorTransactions, subscriptions, bills)
        if (!forecast.hasEnoughHistoryToForecast) return // nothing honest to compare - skip, don't record a fabricated baseline

        val monthTxns = allTransactions.filter { it.date in monthStart until monthEndExclusive && !it.isTransfer }
        val actualNet = monthTxns.filter { it.direction == TransactionDirection.CREDIT }.sumOf { it.amount } -
            monthTxns.filter { it.direction == TransactionDirection.DEBIT }.sumOf { it.amount }

        db.forecastAccuracyDao().insert(
            ForecastAccuracyEntity(
                monthKey = monthKey,
                predictedConservativeNet = forecast.conservativeNetMonthly,
                predictedFullNet = forecast.fullNetMonthly,
                actualNet = actualNet
            )
        )
    }
}
