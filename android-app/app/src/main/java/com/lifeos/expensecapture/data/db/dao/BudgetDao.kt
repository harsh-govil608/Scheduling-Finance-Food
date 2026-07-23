package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity): Long

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY categoryId IS NULL DESC, categoryId ASC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun findByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL LIMIT 1")
    suspend fun findOverall(): BudgetEntity?
}
