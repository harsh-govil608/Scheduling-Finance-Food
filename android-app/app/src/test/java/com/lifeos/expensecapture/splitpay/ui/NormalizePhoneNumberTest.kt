package com.lifeos.expensecapture.splitpay.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression coverage for the phone-number normalization every Smart Split identity match
 * depends on (SplitPayRepository.findUserByPhone does an exact-string match against this
 * output) - a mismatch here silently means "doesn't have the app" even when they do, which was
 * effectively the whole 2026-08-15 identity-fragmentation investigation's starting point.
 */
class NormalizePhoneNumberTest {

    @Test
    fun `plain 10-digit number is returned unchanged`() {
        assertEquals("9876543210", normalizePhoneNumber("9876543210"))
    }

    @Test
    fun `leading plus-91 is stripped`() {
        assertEquals("9876543210", normalizePhoneNumber("+919876543210"))
    }

    @Test
    fun `leading 91 without plus is stripped`() {
        assertEquals("9876543210", normalizePhoneNumber("919876543210"))
    }

    @Test
    fun `spaces and dashes are stripped`() {
        assertEquals("9876543210", normalizePhoneNumber("+91 98765 43210"))
        assertEquals("9876543210", normalizePhoneNumber("91-98765-43210"))
    }

    @Test
    fun `same number in every common format normalizes identically`() {
        val variants = listOf(
            "9876543210",
            "+919876543210",
            "919876543210",
            "+91 98765 43210",
            "91-98765-43210"
        )
        val normalized = variants.map { normalizePhoneNumber(it) }.toSet()
        assertEquals("All variants of the same number must normalize to one value", 1, normalized.size)
        assertEquals("9876543210", normalized.first())
    }
}
