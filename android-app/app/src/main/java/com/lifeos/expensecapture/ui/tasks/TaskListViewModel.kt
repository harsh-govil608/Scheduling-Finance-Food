package com.lifeos.expensecapture.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TaskPriority
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(private val taskDao: TaskDao) : ViewModel() {

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
            taskDao.update(
                task.copy(
                    completed = !task.completed,
                    completedAt = if (!task.completed) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { taskDao.delete(task) }
    }
}
