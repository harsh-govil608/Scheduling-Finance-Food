package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {

    @Insert
    suspend fun insert(investment: InvestmentEntity)

    @Delete
    suspend fun delete(investment: InvestmentEntity)

    @Query("SELECT * FROM investments ORDER BY name ASC")
    fun observeAll(): Flow<List<InvestmentEntity>>
}
