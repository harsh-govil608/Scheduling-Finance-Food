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
    // captured under 10% of real transactions). The old single-regex genericTransactionAlert
    // template required a "to/towards/at"/"from/by" merchant clause to match at all, so a plain
    // ATM withdrawal or EMI debit with no such clause silently failed to parse. It's been
    // replaced entirely by GenericTransactionExtractor - see its kdoc for the full root cause
    // (narrow keyword vocabulary, rigid word order, mandatory currency prefix).

    @Test
    fun `generic debit SMS with a merchant clause still parses correctly`() {
        val body = "Rs.250.00 debited from A/c XX1234 on 12-07-26 to merchant@upi. Ref No 123456789"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(250.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("merchant@upi", result.merchantRaw)
        assertEquals("generic_v2", result.bankTemplateName)
    }

    @Test
    fun `generic debit SMS with no merchant clause still parses, as Unknown`() {
        val body = "Rs.500.00 debited from A/c XX1234 on 12-07-26. Avl Bal Rs.5000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(500.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("Unknown", result.merchantRaw)
        assertEquals("generic_v2", result.bankTemplateName)
    }

    @Test
    fun `generic credit SMS with no merchant clause still parses, as Unknown`() {
        val body = "Rs.1000.00 credited to A/c XX1234 on 12-07-26. Avl Bal Rs.6000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(1000.00, result.amount, 0.001)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("Unknown", result.merchantRaw)
        assertEquals("generic_v2", result.bankTemplateName)
    }

    // Broader keyword vocabulary coverage (real founder request, 2026-08): the old generic
    // template only ever recognized "debited"/"dr." and "credited"/"cr." - real bank SMS
    // routinely use other real words for the same action, in either word order relative to the
    // amount, and sometimes with no currency symbol at all before it.

    @Test
    fun `ATM withdrawal with no currency-before-keyword order still parses`() {
        val body = "Rs.2000.00 withdrawn from A/c XX4321 on 12-07-26. Avl Bal Rs.8000.00"

        val result = parser.parse(sender = "AX-AXISBK-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(2000.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("generic_v2", result.bankTemplateName)
    }

    @Test
    fun `a credit alert using 'received' instead of 'credited' still parses, with its reference id`() {
        val body = "Rs.3000.00 received from A/c XX9999 on 12-07-26. Ref No 987654321"

        val result = parser.parse(sender = "AX-AXISBK-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(3000.00, result.amount, 0.001)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("987654321", result.referenceId)
    }

    @Test
    fun `a debit alert using 'spent' instead of 'debited' still parses`() {
        val body = "You have spent Rs.799.00 on your card ending 4321 at bigbasket. Avl Bal Rs.15000.00"

        val result = parser.parse(sender = "AD-ICICIT-S", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(799.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    // UPI DR/CR compact reference format (real founder request, 2026-08): several banks/PSPs emit
    // a structured "UPI/DR/<ref>/<merchant>/<provider>" segment alongside the prose sentence -
    // e.g. UPI/DR/D127456139556/Zepto/ybl means channel=UPI, type=debit, merchant=Zepto,
    // provider=ybl. This is an unambiguous, higher-confidence signal than keyword proximity.

    @Test
    fun `UPI DR reference format is extracted for direction, merchant, and reference id`() {
        val body = "Rs.199.00 debited from A/c XX1234 via UPI/DR/D127456139556/Zepto/ybl on 12-07-26"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(199.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals("Zepto", result.merchantRaw)
        assertEquals("D127456139556", result.referenceId)
        assertEquals("generic_upi_ref", result.bankTemplateName)
    }

    @Test
    fun `UPI CR reference format is recognized as a credit`() {
        val body = "Rs.500.00 credited to A/c XX1234 via UPI/CR/R987654321000/SohomJana/oksbi on 12-07-26"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(TransactionDirection.CREDIT, result.direction)
        assertEquals("SohomJana", result.merchantRaw)
    }

    // False-positive guard (the flip side of the capture-rate fix): broadening the keyword
    // vocabulary must not turn an ordinary personal text about money into a fabricated
    // transaction just because it happens to contain an amount and a word like "paid"/"sent".

    @Test
    fun `a personal text mentioning an amount and a debit-like word is not parsed as a transaction`() {
        val body = "I paid Rs.500 for the cab, can you send it back?"

        val result = parser.parse(sender = "MOM", body = body)

        assertTrue(result is ParseResult.Unparsed)
    }

    // Real production bug (real founder report, 2026-08: "sohom ko 2 cr ka transaction dikha rha
    // hai, 5 lakh ka" - fabricated multi-lakh/crore transactions from bank/fintech PROMOTIONAL
    // SMS, sent from real bank-sounding sender IDs, satisfying every signal the broadened
    // extractor checks). Fixed via TransactionParser.looksLikePromotionalOrMarketing (an Ignored
    // gate, same treatment as OTP) plus GenericTransactionExtractor's amount sanity cap as
    // defense in depth.

    @Test
    fun `an insurance cover promo mentioning crores is not parsed as a transaction`() {
        val body = "Get life insurance cover of Rs.2,00,00,000 starting at just Rs.500/month. " +
            "Apply now! T&C apply."

        val result = parser.parse(sender = "AD-HDFCLI-S", body = body)

        assertTrue(result is ParseResult.Ignored)
    }

    @Test
    fun `a pre-approved loan promo mentioning lakhs is not parsed as a transaction`() {
        val body = "Congratulations! You are eligible for a pre-approved loan of Rs.5,00,000 " +
            "credited to your account instantly. Avail now."

        val result = parser.parse(sender = "AD-SBICRD-S", body = body)

        assertTrue(result is ParseResult.Ignored)
    }

    @Test
    fun `an implausibly large amount is not auto-inserted even without promotional wording`() {
        // Defense-in-depth check: even if a message somehow avoids every promotional phrase,
        // the generic (unverified) path should never silently record a transaction this large.
        val body = "Rs.25,00,000 debited from A/c XX1234 on 12-07-26. Avl Bal Rs.30,00,000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        assertTrue(result is ParseResult.Unparsed)
    }

    @Test
    fun `a real EMI auto-debit still parses - bare 'EMI' and 'loan' are not treated as promotional`() {
        val body = "Rs.5000.00 debited towards EMI for loan account XX7890 on 12-07-26. Avl Bal Rs.20000.00"

        val result = parser.parse(sender = "VM-HDFCBK", body = body)

        require(result is ParseResult.Parsed)
        assertEquals(5000.00, result.amount, 0.001)
        assertEquals(TransactionDirection.DEBIT, result.direction)
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
