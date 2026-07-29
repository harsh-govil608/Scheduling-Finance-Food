package com.lifeos.expensecapture.assistant

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RuleBasedCommandInterpreterTest {

    @Test
    fun `spent phrasing produces a debit transaction intent`() {
        val result = RuleBasedCommandInterpreter.interpret("spent 200 on lunch")

        require(result is CommandIntent.AddTransaction)
        assertEquals(200.0, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("lunch", result.merchant)
    }

    @Test
    fun `received phrasing produces a credit transaction intent`() {
        val result = RuleBasedCommandInterpreter.interpret("received 500 from Sohom")

        require(result is CommandIntent.AddTransaction)
        assertEquals(500.0, result.amount, 0.001)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("Sohom", result.merchant)
    }

    @Test
    fun `add task with tomorrow strips the date word and sets a due date`() {
        val result = RuleBasedCommandInterpreter.interpret("add task call mom tomorrow")

        require(result is CommandIntent.AddTask)
        assertEquals("call mom", result.title)
        val expected = LocalDate.now(ZoneId.systemDefault()).plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, result.dueDate)
    }

    @Test
    fun `add task with today strips the date word correctly - no off-by-one`() {
        // Regression check: "today" and " today" are different lengths (5 vs 6 chars) - an
        // earlier version of this parser hand-counted dropLast(5) for both cases, which would
        // have silently mangled the title's last character.
        val result = RuleBasedCommandInterpreter.interpret("add task water the plants today")

        require(result is CommandIntent.AddTask)
        assertEquals("water the plants", result.title)
    }

    @Test
    fun `add task without a date phrase has no due date`() {
        val result = RuleBasedCommandInterpreter.interpret("add task read a book")

        require(result is CommandIntent.AddTask)
        assertEquals("read a book", result.title)
        assertNull(result.dueDate)
    }

    @Test
    fun `add habit phrasing produces a habit intent`() {
        val result = RuleBasedCommandInterpreter.interpret("add habit meditate")

        require(result is CommandIntent.AddHabit)
        assertEquals("meditate", result.name)
    }

    @Test
    fun `shopping list phrasing produces a shopping intent`() {
        val result = RuleBasedCommandInterpreter.interpret("add milk to shopping list")

        require(result is CommandIntent.AddShoppingItem)
        assertEquals("milk", result.name)
    }

    @Test
    fun `set budget phrasing is not misread as a spend`() {
        val result = RuleBasedCommandInterpreter.interpret("set food budget to 5000")

        require(result is CommandIntent.SetBudget)
        assertEquals("food", result.categoryHint)
        assertEquals(5000.0, result.monthlyLimit, 0.001)
    }

    @Test
    fun `an unrecognized phrase is reported, not guessed at`() {
        val result = RuleBasedCommandInterpreter.interpret("what's the weather like")

        assertTrue(result is CommandIntent.Unrecognized)
    }
}
