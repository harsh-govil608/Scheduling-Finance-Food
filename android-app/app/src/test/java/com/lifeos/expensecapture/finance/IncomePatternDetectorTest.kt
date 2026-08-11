package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomePatternDetectorTest {

    private val dayMillis = 86_400_000L

    private fun credit(
        payer: String,
        amount: Double,
        daysAgo: Long,
        isRefund: Boolean = false,
        isTransfer: Boolean = false
    ) = TransactionEntity(
        amount = amount,
        direction = TransactionDirection.CREDIT,
        merchantRaw = payer,
        merchantNormalized = payer.lowercase(),
        categoryId = 1,
        date = 10_000L * dayMillis - daysAgo * dayMillis,
        source = TransactionSource.SMS_AUTO,
        confidenceScore = 1f,
        isRefund = isRefund,
        isTransfer = isTransfer
    )

    @Test
    fun `three regular consistent-amount payments from the same payer are CONFIRMED`() {
        val transactions = listOf(
            credit("Acme Corp", 50000.0, 60),
            credit("Acme Corp", 50000.0, 30),
            credit("Acme Corp", 50000.0, 0)
        )

        val result = IncomePatternDetector.detect(transactions)

        assertEquals(1, result.sources.size)
        val source = result.sources.single()
        assertEquals("acme corp", source.payerNormalized)
        assertEquals(IncomePatternDetector.IncomeConfidence.CONFIRMED, source.confidence)
        assertEquals(0.0, result.variableIncomeTotal, 0.01)
    }

    @Test
    fun `two payments from the same payer are ESTIMATED, not CONFIRMED`() {
        val transactions = listOf(
            credit("Freelance Client", 20000.0, 30),
            credit("Freelance Client", 20000.0, 0)
        )

        val source = IncomePatternDetector.detect(transactions).sources.single()

        assertEquals(IncomePatternDetector.IncomeConfidence.ESTIMATED, source.confidence)
    }

    @Test
    fun `high amount variance keeps a 3-occurrence group at ESTIMATED, not CONFIRMED`() {
        val transactions = listOf(
            credit("Gig Payer", 10000.0, 60),
            credit("Gig Payer", 5000.0, 30),
            credit("Gig Payer", 25000.0, 0)
        )

        val source = IncomePatternDetector.detect(transactions).sources.single()

        assertEquals(IncomePatternDetector.IncomeConfidence.ESTIMATED, source.confidence)
    }

    @Test
    fun `one-off payers with no repeating pattern are variable income, not a source`() {
        val transactions = listOf(
            credit("Client A", 5000.0, 20),
            credit("Client B", 8000.0, 10),
            credit("Client C", 3000.0, 0)
        )

        val result = IncomePatternDetector.detect(transactions)

        assertTrue(result.sources.isEmpty())
        assertEquals(16000.0, result.variableIncomeTotal, 0.01)
        assertEquals(3, result.variableIncomeTransactionCount)
    }

    @Test
    fun `refunds are excluded from income pattern detection entirely`() {
        val transactions = listOf(
            credit("Amazon", 1000.0, 60, isRefund = true),
            credit("Amazon", 1000.0, 30, isRefund = true),
            credit("Amazon", 1000.0, 0, isRefund = true)
        )

        val result = IncomePatternDetector.detect(transactions)

        assertTrue(result.sources.isEmpty())
        assertEquals(0.0, result.variableIncomeTotal, 0.01)
    }

    @Test
    fun `transfers are excluded from income pattern detection entirely`() {
        val transactions = listOf(
            credit("My Other Account", 50000.0, 60, isTransfer = true),
            credit("My Other Account", 50000.0, 30, isTransfer = true),
            credit("My Other Account", 50000.0, 0, isTransfer = true)
        )

        val result = IncomePatternDetector.detect(transactions)

        assertTrue(result.sources.isEmpty())
        assertEquals(0.0, result.variableIncomeTotal, 0.01)
    }
}
