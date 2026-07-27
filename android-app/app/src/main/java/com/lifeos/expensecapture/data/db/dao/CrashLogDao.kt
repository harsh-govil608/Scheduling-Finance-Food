package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashLogDao {

    @Insert
    suspend fun insert(entry: CrashLogEntity): Long

    /** Capped at 200 - a rolling recent-history window, not an unbounded log that could grow
     * forever on a device that crashes repeatedly. */
    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC LIMIT 200")
    fun observeRecent(): Flow<List<CrashLogEntity>>

    @Query("DELETE FROM crash_logs")
    suspend fun clearAll()
}
