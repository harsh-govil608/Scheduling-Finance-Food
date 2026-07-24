package com.lifeos.expensecapture.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ProjectDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TaskPriority
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val taskDao: TaskDao,
    projectDao: ProjectDao,
    private val projectId: Long
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskDao.observeForProject(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectName: StateFlow<String> = projectDao.observeAll()
        .map { projects -> projects.firstOrNull { it.id == projectId }?.name ?: "Project" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Project")

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insert(TaskEntity(title = title.trim(), priority = TaskPriority.MEDIUM, projectId = projectId))
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
