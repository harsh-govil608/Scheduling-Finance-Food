package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lifeos.expensecapture.data.db.entity.NotificationEntity
import com.lifeos.expensecapture.data.db.entity.NotificationType
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM app_notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllRead()

    /** Used to avoid re-notifying for the same bill/subscription/budget more than once per
     * cooldown window - see NotificationCheckWorker. */
    @Query(
        "SELECT COUNT(*) FROM app_notifications WHERE type = :type AND sourceKey = :sourceKey " +
            "AND createdAt >= :sinceEpochMillis"
    )
    suspend fun countRecent(type: NotificationType, sourceKey: String, sinceEpochMillis: Long): Int
}
