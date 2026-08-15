package com.lifeos.expensecapture.splitpay.ui

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Regression coverage for two real user-reported bugs fixed 2026-08-15 (see UpiPay.buildPayUri's
 * own kdoc for the full story): a locale-dependent decimal separator that made GPay/PhonePe
 * decline payments with a generic "exceeded bank limit" error even at ₹1-2, and a `+`-for-space
 * encoding bug that could corrupt the payee name/note for anyone whose name has a space in it -
 * which is almost everyone.
 */
class UpiPayTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `amount always uses a period regardless of device locale`() {
        // Real bug: a comma-decimal device locale (e.g. Germany, several EU locales) silently
        // produced am=1,00 instead of am=1.00, which GPay/PhonePe couldn't parse as a valid
        // amount - manifesting as a bank-limit decline with zero indication of the real cause.
        Locale.setDefault(Locale.GERMANY)

        val uri = UpiPay.buildPayUri("sohom@okhdfcbank", "Sohom Jana", 1.0, "Test")

        assertTrue("Expected am=1.00 in $uri, got a locale-mangled amount instead", uri.toString().contains("am=1.00"))
        assertFalse("Amount must never contain a comma decimal separator", uri.toString().contains("am=1,00"))
    }

    @Test
    fun `amount always uses a period under an Indian locale too`() {
        // The common case, not just the pathological one above - confirms the fix doesn't only
        // work by accident under whatever locale happens to run the test suite.
        Locale.setDefault(Locale("en", "IN"))

        val uri = UpiPay.buildPayUri("sohom@okhdfcbank", "Sohom Jana", 250.5, "Lunch")

        assertTrue(uri.toString().contains("am=250.50"))
    }

    @Test
    fun `amount is always formatted to exactly two decimal places`() {
        val uri = UpiPay.buildPayUri("payee@bank", "Payee", 10.0, "note")
        assertTrue(uri.toString().contains("am=10.00"))
    }

    @Test
    fun `payee name with a space is percent-encoded not plus-encoded`() {
        // Real bug: URLEncoder.encode produces "+" for a space (form-encoding, not URI-component
        // encoding) - some UPI apps' parsers only understand %20 and mishandle or truncate at "+".
        val uri = UpiPay.buildPayUri("sohom@okhdfcbank", "Sohom Jana", 5.0, "note")

        assertTrue("Expected %20 in pn=, got: $uri", uri.toString().contains("pn=Sohom%20Jana"))
        assertFalse("pn= must never contain a raw + for a space", uri.toString().contains("pn=Sohom+Jana"))
    }

    @Test
    fun `transaction note with a space is percent-encoded not plus-encoded`() {
        val uri = UpiPay.buildPayUri("payee@bank", "Payee", 5.0, "Dinner split")

        assertTrue(uri.toString().contains("tn=Dinner%20split"))
        assertFalse(uri.toString().contains("tn=Dinner+split"))
    }

    @Test
    fun `payee VPA is trimmed of surrounding whitespace`() {
        val uri = UpiPay.buildPayUri("  sohom@okhdfcbank  ", "Sohom", 5.0, "note")

        assertTrue(uri.toString().contains("pa=sohom@okhdfcbank"))
    }

    @Test
    fun `looksLikeValidVpa accepts a well-formed VPA`() {
        assertTrue(UpiPay.looksLikeValidVpa("sohom@okhdfcbank"))
    }

    @Test
    fun `looksLikeValidVpa rejects blank, missing-at, or edge-at strings`() {
        assertFalse(UpiPay.looksLikeValidVpa(""))
        assertFalse(UpiPay.looksLikeValidVpa("   "))
        assertFalse(UpiPay.looksLikeValidVpa("sohomokhdfcbank"))
        assertFalse(UpiPay.looksLikeValidVpa("@okhdfcbank"))
        assertFalse(UpiPay.looksLikeValidVpa("sohom@"))
    }
}
