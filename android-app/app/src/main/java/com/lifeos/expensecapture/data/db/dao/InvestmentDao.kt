package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import com.lifeos.expensecapture.data.db.entity.InvestmentType
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {

    @Insert
    suspend fun insert(investment: InvestmentEntity)

    /** Mutual-fund NAV sync (2026-08) - the first update path this DAO has ever needed, since
     * MANUAL holdings were previously add/delete only with no editable value. Used by
     * InvestmentSyncTracker to refresh a MUTUAL_FUND holding's currentValue/lastNavUpdatedAt. */
    @Update
    suspend fun update(investment: InvestmentEntity)

    @Delete
    suspend fun delete(investment: InvestmentEntity)

    @Query("SELECT * FROM investments ORDER BY name ASC")
    fun observeAll(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE type = :type")
    suspend fun getByType(type: InvestmentType): List<InvestmentEntity>
}
