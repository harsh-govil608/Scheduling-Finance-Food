package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.ForecastAccuracyEntity

@Dao
interface ForecastAccuracyDao {

    @Insert
    suspend fun insert(record: ForecastAccuracyEntity)

    /** Idempotency guard - ForecastAccuracyTracker checks this before recording a month so
     * repeat calls (Home load, periodic worker) never insert a duplicate for the same month. */
    @Query("SELECT * FROM forecast_accuracy WHERE monthKey = :monthKey LIMIT 1")
    suspend fun findByMonthKey(monthKey: String): ForecastAccuracyEntity?

    @Query("SELECT * FROM forecast_accuracy ORDER BY monthKey DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ForecastAccuracyEntity>
}
