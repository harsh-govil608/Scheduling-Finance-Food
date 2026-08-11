package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.BillEntity
import com.lifeos.expensecapture.data.db.entity.BillStatus
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import com.lifeos.expensecapture.data.db.entity.SubscriptionStatus
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unlike RecurringPatternDetectorTest's fixed-origin trick (safe there since only intervals
 * BETWEEN transactions matter), historyWindowDays measures distance from the real "now" - dates
 * here have to be genuinely relative to System.currentTimeMillis(). */
class ForecastEngineTest {

    private val dayMillis = 86_400_000L
    private fun daysAgo(days: Long) = System.currentTimeMillis() - days * dayMillis

    private fun txn(
        merchant: String,
        amount: Double,
        daysAgo: Long,
        direction: TransactionDirection,
        isTransfer: Boolean = false
    ) = TransactionEntity(
        amount = amount,
        direction = direction,
        merchantRaw = merchant,
        merchantNormalized = merchant.lowercase(),
        categoryId = 1,
        date = daysAgo(daysAgo),
        source = TransactionSource.SMS_AUTO,
        confidenceScore = 1f,
        isTransfer = isTransfer
    )

    @Test
    fun `under a month of history is flagged as not enough to forecast`() {
        val transactions = listOf(txn("Acme Corp", 50000.0, 10, TransactionDirection.CREDIT))

        val forecast = ForecastEngine.compute(transactions, emptyList(), emptyList())

        assertFalse(forecast.hasEnoughHistoryToForecast)
    }

    @Test
    fun `confirmed recurring income is monthly-normalized from a non-30-day cadence`() {
        // 24-day cadence (within RecurringPatternDetector's 20-40 day window, but not 30) -
        // Rs.40000 every 24 days should normalize to Rs.50000/month (40000 * 30/24).
        val transactions = listOf(
            txn("Acme Corp", 40000.0, 96, TransactionDirection.CREDIT),
            txn("Acme Corp", 40000.0, 72, TransactionDirection.CREDIT),
            txn("Acme Corp", 40000.0, 48, TransactionDirection.CREDIT),
            txn("Acme Corp", 40000.0, 24, TransactionDirection.CREDIT),
            txn("Acme Corp", 40000.0, 0, TransactionDirection.CREDIT)
        )

        val forecast = ForecastEngine.compute(transactions, emptyList(), emptyList())

        assertTrue(forecast.hasEnoughHistoryToForecast)
        assertEquals(50000.0, forecast.confirmedMonthlyIncome, 500.0)
    }

    @Test
    fun `confirmed subscriptions and bills feed confirmed monthly expenses, cancelled ones are excluded`() {
        val transactions = listOf(txn("Acme Corp", 50000.0, 45, TransactionDirection.CREDIT))
        val subscriptions = listOf(
            SubscriptionEntity(
                merchantNormalized = "netflix", merchantDisplay = "Netflix", amount = 500.0,
                cadenceDays = 30, lastTransactionDate = daysAgo(0), status = SubscriptionStatus.CONFIRMED_TRACKED
            ),
            SubscriptionEntity(
                merchantNormalized = "old service", merchantDisplay = "Old Service", amount = 999.0,
                cadenceDays = 30, lastTransactionDate = daysAgo(0), status = SubscriptionStatus.CANCELLED
            )
        )
        val bills = listOf(
            BillEntity(
                payeeNormalized = "rent", payeeDisplay = "Rent", typicalAmount = 15000.0,
                dueDayOfMonth = 1, lastPaidDate = daysAgo(0), status = BillStatus.CONFIRMED_TRACKED
            )
        )

        val forecast = ForecastEngine.compute(transactions, subscriptions, bills)

        assertEquals(15500.0, forecast.confirmedMonthlyExpenses, 0.01)
    }

    @Test
    fun `conservative net excludes estimated and variable income, full net includes them`() {
        val transactions = listOf(
            // Confirmed income: 3 regular payments
            txn("Acme Corp", 50000.0, 60, TransactionDirection.CREDIT),
            txn("Acme Corp", 50000.0, 30, TransactionDirection.CREDIT),
            txn("Acme Corp", 50000.0, 0, TransactionDirection.CREDIT),
            // Variable income: one-off payer
            txn("Random Client", 10000.0, 5, TransactionDirection.CREDIT),
            // Discretionary spend
            txn("Zomato", 2000.0, 10, TransactionDirection.DEBIT)
        )

        val forecast = ForecastEngine.compute(transactions, emptyList(), emptyList())

        assertTrue(forecast.hasEnoughHistoryToForecast)
        assertTrue(
            "full net should exceed conservative net once variable income is added",
            forecast.fullNetMonthly > forecast.conservativeNetMonthly
        )
        assertEquals(forecast.confirmedMonthlyIncome - forecast.discretionaryMonthlyAverage, forecast.conservativeNetMonthly, 0.01)
    }

    @Test
    fun `a transfer is excluded from discretionary spend`() {
        val transactions = listOf(
            txn("Acme Corp", 50000.0, 45, TransactionDirection.CREDIT),
            txn("My Other Account", 20000.0, 10, TransactionDirection.DEBIT, isTransfer = true)
        )

        val forecast = ForecastEngine.compute(transactions, emptyList(), emptyList())

        assertEquals(0.0, forecast.discretionaryMonthlyAverage, 0.01)
    }
}
