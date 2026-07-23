package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscriptions ORDER BY status ASC, lastTransactionDate DESC")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE merchantNormalized = :merchant LIMIT 1")
    suspend fun findByMerchant(merchant: String): SubscriptionEntity?
}
