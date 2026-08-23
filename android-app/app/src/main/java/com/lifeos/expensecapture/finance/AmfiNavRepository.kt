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
 * `Scheme Code;ISIN Div Payout/ISIN Growth;ISIN Div Reinvestment;Scheme Name;Plan;Option;Net Asset
 * Value;Date` (AMFI added the Plan/Option columns at some point after this was first built - see
 * parseLine's kdoc) interspersed with blank lines and fund-house/category header lines (e.g.
 * "Aditya Birla Sun Life Mutual Fund" or "Open Ended Schemes(...)") that have to be skipped, not
 * parsed as schemes.
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
        // Real bug fix (2026-08, user report - "AMFI waala nhi chal raha", every single fetch
        // returning empty): AMFI added two new columns ("Plan" and "Option") between Scheme Name
        // and Net Asset Value at some point - every real row now has 8 fields
        // (code;isinPayout;isinReinvestment;name;plan;option;nav;date), not 6, so the old strict
        // `!= 6` check silently rejected every single row, for every user, unconditionally -
        // not the transient network/AMFI-downtime issue the error message implied.
        if (fields.size != 8) return null

        val schemeCode = fields[0].trim()
        val baseName = fields[3].trim()
        val plan = fields[4].trim()
        val option = fields[5].trim()
        val nav = fields[6].trim().toDoubleOrNull() ?: return null
        val date = fields[7].trim()
        if (schemeCode.isEmpty() || baseName.isEmpty()) return null

        // The same fund name repeats once per Plan/Option combination (e.g. "Direct Plan" vs
        // "Regular Plan", "Growth" vs "IDCW Payout" vs "IDCW Reinvestment") - each is a genuinely
        // different NAV, so folding plan/option into the display name is what lets someone
        // searching this list actually tell the rows apart instead of seeing several
        // identical-looking entries with no way to pick the right one.
        val schemeName = if (plan.isNotEmpty() || option.isNotEmpty()) {
            "$baseName (${listOf(plan, option).filter { it.isNotEmpty() && it != "-" }.joinToString(" - ")})"
        } else {
            baseName
        }

        return AmfiScheme(schemeCode = schemeCode, schemeName = schemeName, nav = nav, date = date)
    }
}
