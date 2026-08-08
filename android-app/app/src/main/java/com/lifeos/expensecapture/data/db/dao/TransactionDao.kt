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

    /** Category deletion (see CategoryDao.delete's kdoc): every transaction pointing at a
     * category the user is about to remove needs somewhere real to land, not a dangling
     * categoryId - reassigned to Uncategorized, same fallback CategorizationEngine already uses
     * when nothing else matches. Deliberately does NOT set isUserCorrected - this is a removal
     * side effect, not a correction the user made to this specific transaction. */
    @Query("UPDATE transactions SET categoryId = :uncategorizedId WHERE categoryId = :deletedCategoryId")
    suspend fun reassignCategoryToUncategorized(deletedCategoryId: Long, uncategorizedId: Long)

    /** Secondary duplicate signal (see TransactionEntity.referenceId's kdoc): catches the same
     * real transaction described by two different SMS with different wording - e.g. the bank's
     * alert and a UPI app's own notification - that the exact sender+body sourceHash can't. */
    @Query("SELECT COUNT(*) FROM transactions WHERE referenceId = :referenceId AND referenceId IS NOT NULL")
    suspend fun countByReferenceId(referenceId: String): Int

    /** Bulk-loaded once by SmsDiagnosticsScanner rather than queried per-message - a full-inbox
     * dry-run scan can be thousands of messages, and a per-row suspend query for each would be
     * far slower than one query plus in-memory set membership checks. */
    @Query("SELECT sourceHash FROM transactions")
    suspend fun getAllSourceHashes(): List<String>

    @Query("SELECT referenceId FROM transactions WHERE referenceId IS NOT NULL")
    suspend fun getAllReferenceIds(): List<String>
}
