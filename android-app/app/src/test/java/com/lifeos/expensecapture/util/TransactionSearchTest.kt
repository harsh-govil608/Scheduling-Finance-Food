package com.lifeos.expensecapture.util

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Bug fix (found via a real user report, 2026-07): category name search and bare month-name
 * search ("july") never worked - see TransactionSearch's own kdoc for the full story.
 */
class TransactionSearchTest {

    private val zone = ZoneId.systemDefault()

    private fun millisFor(date: LocalDate) = date.atStartOfDay(zone).toInstant().toEpochMilli()

    private fun txn(merchant: String, categoryId: Long, date: LocalDate, amount: Double = 100.0) = TransactionEntity(
        amount = amount,
        direction = TransactionDirection.DEBIT,
        merchantRaw = merchant,
        merchantNormalized = merchant.trim().lowercase(),
        categoryId = categoryId,
        date = millisFor(date),
        source = TransactionSource.MANUAL,
        confidenceScore = 1.0f
    )

    private val categoryNames = mapOf(1L to "Food & Dining", 2L to "Transport")
    private val categoryNameFor: (Long) -> String = { categoryNames[it] ?: "Uncategorized" }

    @Test
    fun `searching by category name matches transactions in that category`() {
        val today = LocalDate.now(zone)
        val transactions = listOf(
            txn("Zomato", categoryId = 1L, date = today),
            txn("Uber", categoryId = 2L, date = today)
        )

        val results = TransactionSearch.search("food", transactions, categoryNameFor)

        assertEquals(1, results.size)
        assertEquals("Zomato", results.first().merchantRaw)
    }

    @Test
    fun `searching by a month name matches transactions in that month`() {
        val today = LocalDate.now(zone)
        val inTargetMonth = txn("Zomato", categoryId = 1L, date = today.withDayOfMonth(1))
        val monthAgo = txn("Uber", categoryId = 2L, date = today.minusMonths(2))

        val monthName = today.month.name.lowercase()
        val results = TransactionSearch.search(monthName, listOf(inTargetMonth, monthAgo), categoryNameFor)

        assertEquals(1, results.size)
        assertEquals("Zomato", results.first().merchantRaw)
    }

    @Test
    fun `a merchant name containing a month name as a substring is not misread as a date filter`() {
        // "Mayank" contains "may" - must not be treated as a search for the month of May.
        val today = LocalDate.now(zone)
        val transactions = listOf(txn("Mayank", categoryId = 1L, date = today.minusMonths(3)))

        val results = TransactionSearch.search("mayank", transactions, categoryNameFor)

        assertEquals(1, results.size)
        assertEquals("Mayank", results.first().merchantRaw)
    }

    @Test
    fun `existing merchant and amount search still works`() {
        val today = LocalDate.now(zone)
        val transactions = listOf(
            txn("Zomato", categoryId = 1L, date = today, amount = 500.0),
            txn("Zomato", categoryId = 1L, date = today, amount = 50.0)
        )

        val results = TransactionSearch.search("zomato over 200", transactions, categoryNameFor)

        assertEquals(1, results.size)
        assertTrue(results.first().amount > 200.0)
    }
}
