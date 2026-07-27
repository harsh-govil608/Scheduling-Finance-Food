package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pre-beta hardening: this detector feeds both Subscriptions and Bills, and was shipped with zero
 * test coverage despite being core to the app's second-biggest automatic-capture story (after SMS
 * parsing itself). Every case below is a real boundary the code branches on.
 */
class RecurringPatternDetectorTest {

    private val dayMillis = 86_400_000L

    private fun debit(merchant: String, amount: Double, daysAgo: Long) = TransactionEntity(
        amount = amount,
        direction = TransactionDirection.DEBIT,
        merchantRaw = merchant,
        merchantNormalized = merchant.lowercase(),
        categoryId = 1,
        date = 10_000L * dayMillis - daysAgo * dayMillis, // a large fixed origin so nothing goes negative
        source = TransactionSource.SMS_AUTO,
        confidenceScore = 1f
    )

    private fun credit(merchant: String, amount: Double, daysAgo: Long) = TransactionEntity(
        amount = amount,
        direction = TransactionDirection.CREDIT,
        merchantRaw = merchant,
        merchantNormalized = merchant.lowercase(),
        categoryId = 1,
        date = 10_000L * dayMillis - daysAgo * dayMillis,
        source = TransactionSource.SMS_AUTO,
        confidenceScore = 1f
    )

    @Test
    fun `ignores credits entirely`() {
        val transactions = listOf(
            credit("Netflix", 500.0, 60),
            credit("Netflix", 500.0, 30)
        )
        assertTrue(RecurringPatternDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `a single occurrence is never recurring`() {
        val transactions = listOf(debit("Netflix", 500.0, 30))
        assertTrue(RecurringPatternDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `an interval shorter than the minimum is not recurring`() {
        val transactions = listOf(
            debit("Swiggy", 300.0, 5),
            debit("Swiggy", 300.0, 0)
        )
        assertTrue(RecurringPatternDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `an interval longer than the maximum is not recurring`() {
        val transactions = listOf(
            debit("Rent", 10000.0, 90),
            debit("Rent", 10000.0, 0)
        )
        assertTrue(RecurringPatternDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `a monthly-ish interval with a stable amount is detected with correct stats`() {
        val transactions = listOf(
            debit("Netflix", 500.0, 60),
            debit("Netflix", 500.0, 30),
            debit("Netflix", 500.0, 0)
        )

        val groups = RecurringPatternDetector.detect(transactions)

        assertEquals(1, groups.size)
        val group = groups.first()
        assertEquals("netflix", group.merchantNormalized)
        assertEquals("Netflix", group.merchantDisplay)
        assertEquals(500.0, group.averageAmount, 0.01)
        assertEquals(30.0, group.averageIntervalDays, 0.01)
        assertEquals(0.0, group.amountVariancePercent, 0.01)
    }

    @Test
    fun `computes amount variance percent correctly for a varying amount`() {
        val transactions = listOf(
            debit("Electricity", 100.0, 30),
            debit("Electricity", 150.0, 0)
        )

        val group = RecurringPatternDetector.detect(transactions).single()
        // (max - min) / average * 100 = (150 - 100) / 125 * 100 = 40
        assertEquals(40.0, group.amountVariancePercent, 0.01)
    }

    @Test
    fun `keeps different merchants in separate groups`() {
        val transactions = listOf(
            debit("Netflix", 500.0, 30),
            debit("Netflix", 500.0, 0),
            debit("Spotify", 199.0, 30),
            debit("Spotify", 199.0, 0)
        )

        val groups = RecurringPatternDetector.detect(transactions)

        assertEquals(2, groups.size)
        assertTrue(groups.any { it.merchantNormalized == "netflix" })
        assertTrue(groups.any { it.merchantNormalized == "spotify" })
    }

    @Test
    fun `a low-variance group is classified as subscription-like`() {
        val transactions = listOf(debit("Netflix", 500.0, 30), debit("Netflix", 500.0, 0))
        val group = RecurringPatternDetector.detect(transactions).single()
        assertTrue(RecurringPatternDetector.isSubscriptionLike(group))
    }

    @Test
    fun `a high-variance group is not classified as subscription-like`() {
        val transactions = listOf(debit("Rent", 8000.0, 30), debit("Rent", 12000.0, 0))
        val group = RecurringPatternDetector.detect(transactions).single()
        assertFalse(RecurringPatternDetector.isSubscriptionLike(group))
    }
}
