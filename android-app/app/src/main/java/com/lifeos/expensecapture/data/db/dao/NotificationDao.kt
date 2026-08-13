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

    /** Bug fix (found via a real user report, 2026-07): the Notification Center only ever grew,
     * with no way to remove anything. Filters out dismissed rows for display - the rows
     * themselves stay in the table so countRecent()'s cooldown check below still sees them; see
     * NotificationEntity.isDismissed's kdoc. Renamed from observeAll() since it no longer
     * literally returns every row - its one caller (NotificationCenterViewModel) updated to match. */
    @Query("SELECT * FROM app_notifications WHERE isDismissed = 0 ORDER BY createdAt DESC")
    fun observeVisible(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0 AND isDismissed = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE isDismissed = 0")
    suspend fun markAllRead()

    @Query("UPDATE app_notifications SET isDismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("UPDATE app_notifications SET isDismissed = 1 WHERE isDismissed = 0")
    suspend fun dismissAll()

    /** Used to avoid re-notifying for the same bill/subscription/budget more than once per
     * cooldown window - see NotificationCheckWorker. */
    @Query(
        "SELECT COUNT(*) FROM app_notifications WHERE type = :type AND sourceKey = :sourceKey " +
            "AND createdAt >= :sinceEpochMillis"
    )
    suspend fun countRecent(type: NotificationType, sourceKey: String, sinceEpochMillis: Long): Int

    /** True if a notification with this sourceKey has EVER been recorded - unlike countRecent's
     * ~20h cooldown window (for periodically-rechecked conditions), a Smart Split you were just
     * added to should only ever notify once, permanently. See SmartSplitNotificationWatcher. */
    @Query("SELECT EXISTS(SELECT 1 FROM app_notifications WHERE sourceKey = :sourceKey)")
    suspend fun existsBySourceKey(sourceKey: String): Boolean
}
