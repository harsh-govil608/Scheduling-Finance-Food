package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority { LOW, MEDIUM, HIGH }

/**
 * Task Management PRD, Phase 3 Doc 10, scoped drastically down: the full PRD defines AI-inferred
 * priority/duration, recurrence-instance generation, and subtask hierarchy with completion
 * rollup - all explicitly out of scope here, since there's no AI/ML anywhere in this app and no
 * backend to sync recurrence state across devices. What's implemented is exactly what the PRD
 * itself says every sibling feature depends on: "a single, unambiguous definition of what a task
 * is and what state it can be in" - manual creation, a user-set (not inferred) priority, an
 * optional due date, and a completed/active status. No recurrence, no subtasks.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    /** Projects PRD, Phase 3 Doc 11, scoped to exactly this: a task optionally tagged to a
     * project, no dependency graph or milestone tracking. Null means "not part of a project". */
    val projectId: Long? = null,
    /** AI Transformation Plan H1 (bill-to-task auto-creation): links this task back to the Bill
     * it was generated from, so NotificationCheckWorker.syncBillTasks can update the same task in
     * place across checks instead of spawning a duplicate every cycle. Null means self-authored,
     * not bill-generated - the overwhelming majority of tasks. */
    val sourceBillId: Long? = null
)
