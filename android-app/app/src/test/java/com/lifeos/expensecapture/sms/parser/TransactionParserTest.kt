package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks in behavior against real captured SMS samples (not fabricated text) - see
 * BankTemplate.kt's doc comments for where each sample came from. The point of this test is to
 * catch a future regex edit silently breaking a format that was already verified working,
 * which is exactly the class of bug that cost real debugging time on Day 1 (a generic template
 * that looked reasonable but didn't match real ICICI text).
 */
class TransactionParserTest {

    private val parser = TransactionParser()

    @Test
    fun `real ICICI debit SMS parses correctly`() {
        val body = "ICICI Bank Acct XX910 debited for Rs 1.00 on 23-Jul-26; Blinkit credited. " +
            "UPI:657096314469. Call 18002662 for dispute. SMS BLOCK 910 to 9215676766."

        val result = parser.parse(sender = "AD-ICICIT-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(1.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("Blinkit", result.merchantRaw)
        assertEquals("icici_bank", result.bankTemplateName)
    }

    @Test
    fun `real ICICI credit SMS parses correctly`() {
        val body = "Dear Customer, Acct XX910 is credited with Rs 1.00 on 25-Jul-26 from Sohom " +
            "Jana. UPI:620647267681-ICICI Bank."

        val result = parser.parse(sender = "AX-ICICIT-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(1.00, result.amount, 0.001)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("Sohom Jana", result.merchantRaw)
        assertEquals("icici_bank", result.bankTemplateName)
    }

    @Test
    fun `real SBI UPI debit SMS parses correctly`() {
        val body = "Dear UPI user A/C X5359 debited by 1.00 on date 25Jul26 trf to " +
            "harshgovil460@ok Refno 620647267681 If not u? call-1800111109 for other " +
            "services-18001234-SBI"

        val result = parser.parse(sender = "AD-SBIUPI-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(1.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("harshgovil460@ok", result.merchantRaw)
        assertEquals("sbi", result.bankTemplateName)
    }

    @Test
    fun `unrelated personal SMS is not parsed as a transaction`() {
        val result = parser.parse(sender = "MOM", body = "Call me when you're free")

        assertTrue(result is ParseResult.Unparsed)
    }

    @Test
    fun `bank OTP SMS is ignored, not flagged for review`() {
        // Bug fix (found via a real user report, 2026-07): before this, an OTP SMS - even from
        // a real bank sender, even mentioning the same amount/merchant a genuine debit alert
        // would - fell through to Unparsed and piled up in the Needs Review queue as noise.
        val body = "1234 is the OTP for txn of INR 500.00 at Amazon. Valid for 5 mins. " +
            "Do not share this OTP with anyone. -SBI"

        val result = parser.parse(sender = "AD-SBIUPI-S", body = body)

        assertTrue(result is ParseResult.Ignored)
    }
}
