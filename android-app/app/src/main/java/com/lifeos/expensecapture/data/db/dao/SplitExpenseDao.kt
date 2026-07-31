package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.SplitExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitExpenseDao {

    @Insert
    suspend fun insert(expense: SplitExpenseEntity): Long

    /** No @ForeignKey/cascade in this schema (see SplitParticipantEntity's kdoc) -
     * SplitExpensesViewModel.deleteExpense deletes this row's participants via
     * SplitParticipantDao.deleteForExpense first, so this never leaves orphaned rows behind. */
    @Delete
    suspend fun delete(expense: SplitExpenseEntity)

    @Query("SELECT * FROM split_expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<SplitExpenseEntity>>
}
