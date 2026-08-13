package com.lifeos.expensecapture.finance

import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.InvestmentType
import com.lifeos.expensecapture.logging.AppLogger

/**
 * Mutual-fund NAV sync (2026-08, real user request). A standalone object, not a
 * FinanceInsightsRepository method - matches this package's existing convention
 * (ForecastAccuracyTracker, RecurringPatternDetector, etc. all take an AppDatabase/raw data
 * directly rather than being repository methods).
 *
 * Only MUTUAL_FUND holdings are touched - a MANUAL holding's `currentValue` stays exactly what
 * the user typed, untouched by this. Stock/equity holdings aren't supported at all yet: there's
 * no free public API for NSE/BSE prices the way AMFI provides one for mutual funds, so stock
 * sync would need a paid market-data vendor - a real future blocker, not something this can
 * quietly approximate.
 */
object InvestmentSyncTracker {

    suspend fun syncAll(db: AppDatabase) {
        val holdings = db.investmentDao().getByType(InvestmentType.MUTUAL_FUND)
        if (holdings.isEmpty()) return

        val schemes = AmfiNavRepository.fetchAll()
        if (schemes.isEmpty()) return // offline or AMFI unreachable - leave existing values as-is, try again next sync
        val schemeByCode = schemes.associateBy { it.schemeCode }

        for (holding in holdings) {
            val schemeCode = holding.schemeCode
            val units = holding.units
            if (schemeCode == null || units == null) continue

            val scheme = schemeByCode[schemeCode]
            if (scheme == null) {
                AppLogger.w("InvestmentSyncTracker", "no AMFI match for schemeCode=$schemeCode (\"${holding.name}\")")
                continue
            }
            db.investmentDao().update(
                holding.copy(
                    currentValue = units * scheme.nav,
                    lastNavUpdatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
