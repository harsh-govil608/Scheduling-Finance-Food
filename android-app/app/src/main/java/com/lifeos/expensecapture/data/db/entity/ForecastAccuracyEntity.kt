package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * "Learn and Adapt" (2026-08, real user feedback: "it predicts based on history, but if AI
 * learns then it can give more accurate predictions") - the PRD's own Spend Prediction doc
 * (docs/phase-3-product-specifications/03-finance-suite/21-spend-prediction-prd.md) specs
 * exactly this: check prediction accuracy against actuals at period end. One row per completed
 * calendar month, written once by ForecastAccuracyTracker and never updated afterward - a
 * permanent record of what ForecastEngine would honestly have predicted before that month began
 * (using only transaction history available at that point), versus what actually happened.
 *
 * `predictedConservativeNet` is deliberately pessimistic by design (confirmed income only, minus
 * every expense) - actual net exceeding it is expected and not itself evidence of a bad forecast.
 * `predictedFullNet` (adds estimated + variable income) is the more directly comparable figure
 * against `actualNet`. Both are kept so FinanceQaEngine can show the gap for each rather than
 * collapsing to one "the forecast was off by X%" number that would blur which kind of estimate
 * was actually wrong.
 */
@Entity(tableName = "forecast_accuracy", indices = [Index(value = ["monthKey"], unique = true)])
data class ForecastAccuracyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** "yyyy-MM" - the completed month this row is about, not when it was recorded. */
    val monthKey: String,
    val predictedConservativeNet: Double,
    val predictedFullNet: Double,
    val actualNet: Double,
    val recordedAt: Long = System.currentTimeMillis()
)
