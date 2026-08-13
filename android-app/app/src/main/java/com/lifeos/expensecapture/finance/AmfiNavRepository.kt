package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AmfiScheme(val schemeCode: String, val schemeName: String, val nav: Double, val date: String)

/**
 * Mutual-fund NAV sync (2026-08, real user request: "the investment option needs improvements,
 * mostly sync option"). AMFI (Association of Mutual Funds in India) publishes a free, public,
 * no-key-needed daily NAV file for every registered scheme - the same official source every
 * mutual-fund app/website in India ultimately reads from. No paid market-data vendor needed for
 * this, unlike stock/equity prices (no equivalent free public source exists for those - see
 * InvestmentSyncTracker's kdoc for why stock sync isn't attempted).
 *
 * File format is a plain semicolon-delimited text file, NOT CSV/JSON - one scheme per line:
 * `Scheme Code;ISIN Div Payout/ISIN Growth;ISIN Div Reinvestment;Scheme Name;Net Asset Value;Date`
 * interspersed with blank lines and fund-house/category header lines (e.g. "Aditya Birla Sun
 * Life Mutual Fund" or "Open Ended Schemes(...)") that have to be skipped, not parsed as schemes.
 */
object AmfiNavRepository {

    private const val NAV_URL = "https://www.amfiindia.com/spages/NAVAll.txt"
    private const val TIMEOUT_MILLIS = 15_000

    suspend fun fetchAll(): List<AmfiScheme> = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(NAV_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
            }
            connection.inputStream.bufferedReader().useLines { lines ->
                lines.mapNotNull { parseLine(it) }.toList()
            }
        } catch (e: Exception) {
            // Offline, AMFI unreachable, or the file format changed - never crashes the sync
            // caller over this, same "fail closed, log it" pattern UpdateChecker's manifest
            // fetch already uses.
            AppLogger.e("AmfiNavRepository", "fetchAll failed", e)
            emptyList()
        }
    }

    private fun parseLine(rawLine: String): AmfiScheme? {
        val line = rawLine.trim()
        if (line.isEmpty()) return null
        val fields = line.split(";")
        // A real scheme row always has exactly 6 semicolon-delimited fields; header/fund-house
        // lines and section titles don't contain any semicolons at all.
        if (fields.size != 6) return null

        val schemeCode = fields[0].trim()
        val schemeName = fields[3].trim()
        val nav = fields[4].trim().toDoubleOrNull() ?: return null
        val date = fields[5].trim()
        if (schemeCode.isEmpty() || schemeName.isEmpty()) return null

        return AmfiScheme(schemeCode = schemeCode, schemeName = schemeName, nav = nav, date = date)
    }
}
