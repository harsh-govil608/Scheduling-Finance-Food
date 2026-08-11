package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HabitSpendCorrelatorTest {

    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)
    private val dayMillis = 86_400_000L

    private val categories = listOf(
        CategoryEntity(id = 1, name = "Food & Dining", isSystemDefault = true),
        CategoryEntity(id = 2, name = "Shopping", isSystemDefault = true),
        CategoryEntity(id = 3, name = "Health", isSystemDefault = true)
    )

    private fun debit(categoryId: Long, amount: Double, daysAgo: Long) = TransactionEntity(
        amount = amount,
        direction = TransactionDirection.DEBIT,
        merchantRaw = "Merchant",
        merchantNormalized = "merchant",
        categoryId = categoryId,
        date = System.currentTimeMillis() - daysAgo * dayMillis,
        source = TransactionSource.SMS_AUTO,
        confidenceScore = 1f
    )

    @Test
    fun `an alias-phrase habit name matches its category via the keyword alias list`() {
        val habit = HabitEntity(id = 1, name = "No eating out")
        val completions = listOf(HabitCompletionEntity(habitId = 1, dateEpochDay = today.toEpochDay()))
        val transactions = listOf(debit(1, 500.0, 2))

        val result = HabitSpendCorrelator.correlate(listOf(habit), completions, categories, transactions)

        assertEquals(1, result.size)
        assertEquals("Food & Dining", result.single().categoryName)
    }

    @Test
    fun `a habit name matching a category name directly is matched`() {
        val habit = HabitEntity(id = 1, name = "Cut down on Shopping")
        val completions = listOf(HabitCompletionEntity(habitId = 1, dateEpochDay = today.toEpochDay()))
        val transactions = listOf(debit(2, 1000.0, 1))

        val result = HabitSpendCorrelator.correlate(listOf(habit), completions, categories, transactions)

        assertEquals("Shopping", result.single().categoryName)
    }

    @Test
    fun `a habit with no matching category produces no correlation`() {
        val habit = HabitEntity(id = 1, name = "Meditate")
        val completions = listOf(HabitCompletionEntity(habitId = 1, dateEpochDay = today.toEpochDay()))

        val result = HabitSpendCorrelator.correlate(listOf(habit), completions, categories, emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `a matched habit with zero completions in the window produces no correlation`() {
        val habit = HabitEntity(id = 1, name = "No eating out")
        val transactions = listOf(debit(1, 500.0, 2))

        val result = HabitSpendCorrelator.correlate(listOf(habit), emptyList(), categories, transactions)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `an archived habit is excluded even if it would otherwise match`() {
        val habit = HabitEntity(id = 1, name = "No eating out", archived = true)
        val completions = listOf(HabitCompletionEntity(habitId = 1, dateEpochDay = today.toEpochDay()))
        val transactions = listOf(debit(1, 500.0, 2))

        val result = HabitSpendCorrelator.correlate(listOf(habit), completions, categories, transactions)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `spend totals only count the matched category, ignoring other categories and transfers`() {
        val habit = HabitEntity(id = 1, name = "No eating out")
        val completions = listOf(HabitCompletionEntity(habitId = 1, dateEpochDay = today.toEpochDay()))
        val thisMonthStart = today.withDayOfMonth(1)
        val daysIntoMonth = today.toEpochDay() - thisMonthStart.toEpochDay()
        val transactions = listOf(
            debit(1, 700.0, daysIntoMonth), // Food & Dining, this month
            debit(3, 5000.0, daysIntoMonth), // Health, this month - must not count
            TransactionEntity(
                amount = 2000.0, direction = TransactionDirection.DEBIT, merchantRaw = "Own account",
                merchantNormalized = "own account", categoryId = 1,
                date = System.currentTimeMillis() - daysIntoMonth * dayMillis,
                source = TransactionSource.SMS_AUTO, confidenceScore = 1f, isTransfer = true
            )
        )

        val result = HabitSpendCorrelator.correlate(listOf(habit), completions, categories, transactions)

        assertEquals(700.0, result.single().categorySpendThisMonth, 0.01)
    }
}
