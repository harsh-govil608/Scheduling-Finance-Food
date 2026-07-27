package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY completed ASC, dueDate ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY completed ASC, dueDate ASC, createdAt DESC")
    fun observeForProject(projectId: Long): Flow<List<TaskEntity>>

    /** Most recent task generated from a given Bill, if any - lets the bill-task sync tell a
     * still-open instance (update in place) apart from one completed for an earlier cycle
     * (leave it, create a fresh one for the current cycle). */
    @Query("SELECT * FROM tasks WHERE sourceBillId = :billId ORDER BY dueDate DESC, createdAt DESC LIMIT 1")
    suspend fun findLatestForBill(billId: Long): TaskEntity?
}
