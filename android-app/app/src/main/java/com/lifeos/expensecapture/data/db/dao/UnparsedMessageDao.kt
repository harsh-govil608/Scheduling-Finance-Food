package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.UnparsedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnparsedMessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: UnparsedMessageEntity): Long

    @Query("SELECT * FROM unparsed_messages WHERE resolved = 0 ORDER BY receivedAt DESC")
    fun observeUnresolved(): Flow<List<UnparsedMessageEntity>>

    @Query("UPDATE unparsed_messages SET resolved = 1 WHERE id = :id")
    suspend fun markResolved(id: Long)

    @Query("SELECT * FROM unparsed_messages WHERE resolved = 0")
    suspend fun getAllUnresolved(): List<UnparsedMessageEntity>

    @Query("DELETE FROM unparsed_messages WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
