package com.lifeos.expensecapture.sms.parser

import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // Bug fix regression coverage (real user report, 2026-08: a second phone's SMS history scan
    // captured under 10% of real transactions) - see BankTemplate.kt's genericTransactionAlert
    // kdoc. Any bank other than ICICI/SBI falls to this template; it used to require a "to/
    // towards/at"/"from/by" merchant clause to match at all, so a plain ATM withdrawal or EMI
    // debit with no such clause silently failed to parse.

    @Test
    fun `generic debit SMS with a merchant clause still parses correctly`() {
        val body = "Rs.250.00 debited from A/c XX1234 on 12-07-26 to merchant@upi. Ref No 123456789"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(250.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("merchant@upi", result.merchantRaw)
        assertEquals("generic_transaction_alert", result.bankTemplateName)
    }

    @Test
    fun `generic debit SMS with no merchant clause still parses, as Unknown`() {
        val body = "Rs.500.00 debited from A/c XX1234 on 12-07-26. Avl Bal Rs.5000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(500.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("Unknown", result.merchantRaw)
        assertEquals("generic_transaction_alert", result.bankTemplateName)
    }

    @Test
    fun `generic credit SMS with no merchant clause still parses, as Unknown`() {
        val body = "Rs.1000.00 credited to A/c XX1234 on 12-07-26. Avl Bal Rs.6000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(1000.00, result.amount, 0.001)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("Unknown", result.merchantRaw)
        assertEquals("generic_transaction_alert", result.bankTemplateName)
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

    // Product-scale fix (found via a real user report, 2026-07 - "we can't keep showing the
    // person all the sms, this is going to overload my app"): TransactionIngestor only surfaces
    // an Unparsed result to the Needs Review queue when the sender itself passes this check -
    // see looksLikeInstitutionalSender's own kdoc for the DLT-sender-ID reasoning.

    @Test
    fun `real bank sender IDs are recognized as institutional`() {
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AD-ICICIT-S"))
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AX-ICICIT-S"))
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AX-ICICIT-T"))
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AD-SBIUPI-S"))
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AX-AXISBK-S"))
        assertTrue(TransactionParser.looksLikeInstitutionalSender("VM-HDFCBK"))
    }

    @Test
    fun `a saved contact name is not institutional`() {
        assertFalse(TransactionParser.looksLikeInstitutionalSender("MOM"))
        assertFalse(TransactionParser.looksLikeInstitutionalSender("Harsh Govil"))
    }

    @Test
    fun `a plain phone number is not institutional`() {
        assertFalse(TransactionParser.looksLikeInstitutionalSender("9876543210"))
        assertFalse(TransactionParser.looksLikeInstitutionalSender("+919876543210"))
    }

    @Test
    fun `DLT-registered non-bank businesses are not institutional, even in the same sender shape`() {
        // Regression check against real captured noise (2026-07): these are real DLT-registered
        // senders (telecom, e-commerce, edtech) that share the exact "XX-YYYYYY-Z" shape real
        // bank senders use - a DLT-shape check alone would have kept all of these in Needs
        // Review, which is exactly the noise this fix exists to prevent.
        assertFalse(TransactionParser.looksLikeInstitutionalSender("VK-ViCARE-S")) // Vi telecom balance alert
        assertFalse(TransactionParser.looksLikeInstitutionalSender("CP-blnkit-S")) // Blinkit order notice
        assertFalse(TransactionParser.looksLikeInstitutionalSender("VA-UPGRAD-P")) // upGrad course marketing
        assertFalse(TransactionParser.looksLikeInstitutionalSender("VK-611123-P")) // telecom/data-pack promo
    }

    @Test
    fun `a real rent-payment service sender is recognized as institutional`() {
        // Found in a real user's actual Needs Review data (2026-07): CRIBIN sends genuine
        // "Payment Successful"/"Payment...has failed" confirmations for real recurring rent
        // payments (via Urbanroomz) - real money moving, so it belongs in the whitelist even
        // though it isn't a bank itself.
        assertTrue(TransactionParser.looksLikeInstitutionalSender("AX-CRIBIN-S"))
    }
}
