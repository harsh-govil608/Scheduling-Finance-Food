package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Staff-engineer hardening pass (pre-beta): this engine drives the single most visible "AI"
 * surface in the app - it needs to be right, and it needs to stay right as the codebase changes.
 * Every fixture uses a fixed `now` so the tests are deterministic regardless of when they run.
 */
class SpendingInsightEngineTest {

    private val zone = ZoneId.systemDefault()
    private val foodCategory = CategoryEntity(id = 1, name = "Food")
    private val shoppingCategory = CategoryEntity(id = 2, name = "Shopping")

    /** now = 15 March - 14 days elapsed this month, a stable comparison window. */
    private val now = millisOf(2026, 3, 15)

    private fun millisOf(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        LocalDate.of(year, month, day).atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli()

    private fun debit(
        id: Long,
        amount: Double,
        merchant: String,
        categoryId: Long,
        date: Long
    ) = TransactionEntity(
        id = id,
        amount = amount,
        direction = TransactionDirection.DEBIT,
        merchantRaw = merchant,
        merchantNormalized = merchant.lowercase(),
        categoryId = categoryId,
        date = date,
        source = com.lifeos.expensecapture.data.db.entity.TransactionSource.SMS_AUTO,
        confidenceScore = 1f
    )

    private fun credit(id: Long, amount: Double, date: Long) = TransactionEntity(
        id = id,
        amount = amount,
        direction = TransactionDirection.CREDIT,
        merchantRaw = "Salary",
        merchantNormalized = "salary",
        categoryId = foodCategory.id,
        date = date,
        source = com.lifeos.expensecapture.data.db.entity.TransactionSource.SMS_AUTO,
        confidenceScore = 1f
    )

    @Test
    fun `returns null when there are no transactions`() {
        val result = SpendingInsightEngine.compute(emptyList(), listOf(foodCategory), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `returns null when every category's increase is below the minimum threshold`() {
        val transactions = listOf(
            debit(1, 100.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 10)),
            debit(2, 90.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 10))
        )
        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), emptyList(), now)
        assertNull(result)
    }

    @Test
    fun `flags the category with the largest real increase, ignoring one below threshold`() {
        val transactions = listOf(
            // Food: this month 4500 (Swiggy 3000 + Zomato 1500), last comparable window 2400 (Swiggy 1000 + Zomato 1400)
            debit(1, 3000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 1500.0, "Zomato", foodCategory.id, millisOf(2026, 3, 8)),
            debit(3, 1000.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 5)),
            debit(4, 1400.0, "Zomato", foodCategory.id, millisOf(2026, 2, 8)),
            // Shopping: increase of only 20 - below MIN_INCREASE_AMOUNT (300), must not be picked
            debit(5, 500.0, "Amazon", shoppingCategory.id, millisOf(2026, 3, 6)),
            debit(6, 480.0, "Amazon", shoppingCategory.id, millisOf(2026, 2, 6))
        )

        val result = SpendingInsightEngine.compute(
            transactions, listOf(foodCategory, shoppingCategory), emptyList(), now
        )

        requireNotNull(result)
        assertEquals("Food", result.categoryName)
        assertEquals(2100.0, result.increaseAmount, 0.01)
        assertEquals(87.5, result.increasePercent, 0.01)
    }

    @Test
    fun `ranks top merchants by contribution to the increase, not by total spend`() {
        val transactions = listOf(
            // Swiggy: huge total (5000) but flat vs last month (no increase) - must NOT be the top merchant
            debit(1, 5000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 5000.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 5)),
            // Zomato: smaller total but a real, large increase - must be the top merchant
            debit(3, 2000.0, "Zomato", foodCategory.id, millisOf(2026, 3, 6)),
            debit(4, 200.0, "Zomato", foodCategory.id, millisOf(2026, 2, 6))
        )

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), emptyList(), now)

        requireNotNull(result)
        assertEquals(listOf("Zomato"), result.topMerchants)
    }

    @Test
    fun `caps top merchants at two, sorted by largest increase first`() {
        val transactions = listOf(
            debit(1, 1000.0, "A", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 2000.0, "B", foodCategory.id, millisOf(2026, 3, 5)),
            debit(3, 3000.0, "C", foodCategory.id, millisOf(2026, 3, 5))
            // No prior-month occurrences for A/B/C, so each one's full amount counts as its "increase".
        )

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), emptyList(), now)

        requireNotNull(result)
        assertEquals(2, result.topMerchants.size)
        assertEquals(listOf("C", "B"), result.topMerchants)
    }

    @Test
    fun `falls back to Uncategorized when the category id has no matching name`() {
        val transactions = listOf(
            debit(1, 1000.0, "Swiggy", categoryId = 999L, date = millisOf(2026, 3, 5))
        )
        val result = SpendingInsightEngine.compute(transactions, emptyList(), emptyList(), now)

        requireNotNull(result)
        assertEquals("Uncategorized", result.categoryName)
    }

    @Test
    fun `goal acceleration is absent when no goal has a target amount`() {
        val transactions = listOf(
            debit(1, 3000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            credit(2, 10000.0, millisOf(2026, 3, 3))
        )
        val goals = listOf(GoalEntity(id = 1, title = "No target", targetAmount = null))

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), goals, now)

        requireNotNull(result)
        assertNull(result.goalAcceleration)
    }

    @Test
    fun `goal acceleration is absent when current pace is zero or negative`() {
        val transactions = listOf(
            debit(1, 3000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 1000.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 5))
            // No credits at all this month - net cash flow is negative.
        )
        val goals = listOf(GoalEntity(id = 1, title = "House", targetAmount = 50_000.0))

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), goals, now)

        requireNotNull(result)
        assertNull(result.goalAcceleration)
    }

    @Test
    fun `goal acceleration returns a real months-sooner estimate on a genuine positive pace`() {
        val transactions = listOf(
            debit(1, 3000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 1500.0, "Zomato", foodCategory.id, millisOf(2026, 3, 8)),
            debit(3, 1000.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 5)),
            debit(4, 1400.0, "Zomato", foodCategory.id, millisOf(2026, 2, 8)),
            credit(5, 10_000.0, millisOf(2026, 3, 3))
        )
        val goals = listOf(GoalEntity(id = 1, title = "House down payment", targetAmount = 50_000.0))

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), goals, now)

        requireNotNull(result)
        val acceleration = requireNotNull(result.goalAcceleration)
        assertEquals("House down payment", acceleration.goalTitle)
        assertTrue("expected a positive months-sooner estimate", acceleration.monthsSooner > 0.0)
    }

    @Test
    fun `ignores a completed goal even if it has a target amount`() {
        val transactions = listOf(
            debit(1, 3000.0, "Swiggy", foodCategory.id, millisOf(2026, 3, 5)),
            debit(2, 1000.0, "Swiggy", foodCategory.id, millisOf(2026, 2, 5)),
            credit(3, 10_000.0, millisOf(2026, 3, 3))
        )
        val goals = listOf(GoalEntity(id = 1, title = "Done already", targetAmount = 50_000.0, completed = true))

        val result = SpendingInsightEngine.compute(transactions, listOf(foodCategory), goals, now)

        requireNotNull(result)
        assertNull(result.goalAcceleration)
    }
}
