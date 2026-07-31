package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: BillEntity): Long

    @Update
    suspend fun update(bill: BillEntity)

    /** Real removal (found via a real user review of a comparable app: "no options to remove
     * bills which was not to be tracked") - dismissBill only ever marked a bill CANCELLED, which
     * kept it sitting in the list forever with no way to actually clear it out. */
    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills ORDER BY status ASC, dueDayOfMonth ASC")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE payeeNormalized = :payee LIMIT 1")
    suspend fun findByPayee(payee: String): BillEntity?

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): BillEntity?
}
