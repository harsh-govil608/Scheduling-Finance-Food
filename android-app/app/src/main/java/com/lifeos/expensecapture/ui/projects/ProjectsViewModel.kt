package com.lifeos.expensecapture.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ProjectDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.entity.ProjectEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectRow(val project: ProjectEntity, val openTaskCount: Int, val totalTaskCount: Int)

class ProjectsViewModel(
    private val projectDao: ProjectDao,
    taskDao: TaskDao
) : ViewModel() {

    val projects: StateFlow<List<ProjectRow>> = combine(
        projectDao.observeAll(),
        taskDao.observeAll()
    ) { projects, allTasks ->
        projects.map { project ->
            val tasksForProject = allTasks.filter { it.projectId == project.id }
            ProjectRow(
                project = project,
                openTaskCount = tasksForProject.count { !it.completed },
                totalTaskCount = tasksForProject.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProject(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { projectDao.insert(ProjectEntity(name = name.trim())) }
    }

    fun archive(project: ProjectEntity) {
        viewModelScope.launch { projectDao.update(project.copy(archived = true)) }
    }
}
