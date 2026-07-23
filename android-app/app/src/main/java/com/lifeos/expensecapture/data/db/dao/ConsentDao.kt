package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.ConsentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(consent: ConsentEntity)

    @Query("SELECT * FROM consents WHERE permissionType = :permissionType LIMIT 1")
    suspend fun get(permissionType: String): ConsentEntity?

    @Query("SELECT * FROM consents")
    fun observeAll(): Flow<List<ConsentEntity>>
}
