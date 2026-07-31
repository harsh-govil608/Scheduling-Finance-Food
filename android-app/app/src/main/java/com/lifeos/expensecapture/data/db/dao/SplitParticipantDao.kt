package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.SplitParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitParticipantDao {

    @Insert
    suspend fun insertAll(participants: List<SplitParticipantEntity>)

    @Update
    suspend fun update(participant: SplitParticipantEntity)

    @Query("SELECT * FROM split_participants")
    fun observeAll(): Flow<List<SplitParticipantEntity>>

    @Query("SELECT * FROM split_participants WHERE splitExpenseId = :splitExpenseId")
    fun observeForExpense(splitExpenseId: Long): Flow<List<SplitParticipantEntity>>

    @Query("DELETE FROM split_participants WHERE splitExpenseId = :splitExpenseId")
    suspend fun deleteForExpense(splitExpenseId: Long)
}
