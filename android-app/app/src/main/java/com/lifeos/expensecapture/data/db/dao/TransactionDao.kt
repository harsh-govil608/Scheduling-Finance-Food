package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    /** Ledger deletion (found via a real user report, 2026-07): there was previously no way to
     * remove a transaction at all, e.g. a manually mis-entered one or a duplicate. */
    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    /** Used by the future weekly-summary feature (Section 1) once wired. */
    @Query("SELECT * FROM transactions WHERE date >= :sinceEpochMillis ORDER BY date DESC")
    suspend fun getSince(sinceEpochMillis: Long): List<TransactionEntity>

    /** Used by the future backend-sync increment (Section 8's POST /transactions/sync). */
    @Query("SELECT * FROM transactions WHERE synced = 0")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET categoryId = :newCategoryId, isUserCorrected = 1 WHERE id = :transactionId")
    suspend fun recategorize(transactionId: Long, newCategoryId: Long)
}
