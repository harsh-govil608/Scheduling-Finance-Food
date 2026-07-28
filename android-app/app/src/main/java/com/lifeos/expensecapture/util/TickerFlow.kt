package com.lifeos.expensecapture.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Bug fix (found via a real user report, 2026-07 - "month wise not updating"): several
 * month/day-boundary figures (budget progress, Home's spent-this-month/today) are derived inside
 * a combine() block that only re-runs when one of its *data* Flows (transactions/budgets/etc.)
 * emits - it does NOT re-run just because real time passed. If the app is left open (process/
 * ViewModel retained) across a month or day boundary with no new transaction arriving in the
 * meantime, those figures kept showing the old period's values until some unrelated write
 * happened to trigger a recompute. Merging this into the combine() as an extra input forces a
 * periodic recompute so the boundary is picked up on its own, not only when new data arrives.
 *
 * 60s is deliberately not tied to the app's actual boundary precision (a month/day boundary is a
 * rare event) - it's simply frequent enough that a user sitting on the app right at midnight
 * sees it correct within a minute, without spinning meaningfully more CPU/battery than a single
 * periodic emit costs.
 */
fun tickerFlow(intervalMillis: Long = 60_000L): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(intervalMillis)
    }
}
