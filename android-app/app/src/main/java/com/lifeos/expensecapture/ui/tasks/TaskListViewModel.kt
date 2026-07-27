package com.lifeos.expensecapture.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.BillDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TaskPriority
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val taskDao: TaskDao,
    /** Optional so every other TaskListViewModel call site (Projects' task list, etc.) doesn't
     * need a BillDao just to toggle completion - only needed to close the loop on H1's
     * bill-generated tasks (see toggleCompleted). */
    private val billDao: BillDao? = null
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, priority: TaskPriority, dueDate: Long?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(TaskEntity(title = title.trim(), priority = priority, dueDate = dueDate))
        }
    }

    fun toggleCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val nowCompleted = !task.completed
            taskDao.update(
                task.copy(
                    completed = nowCompleted,
                    completedAt = if (nowCompleted) System.currentTimeMillis() else null
                )
            )
            // AI Transformation Plan H1: closes the loop the other direction - completing a
            // bill-generated task marks the underlying bill paid, so it stops being flagged as
            // due and the sync worker won't recreate a task for the same cycle.
            val billId = task.sourceBillId
            if (nowCompleted && billId != null) {
                billDao?.findById(billId)?.let { bill ->
                    billDao.update(bill.copy(lastPaidDate = System.currentTimeMillis()))
                }
            }
        }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { taskDao.delete(task) }
    }
}
