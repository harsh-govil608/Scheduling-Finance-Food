package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Insert
    suspend fun insert(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items ORDER BY checked ASC, createdAt DESC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>
}
