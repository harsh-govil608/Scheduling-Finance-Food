package com.lifeos.expensecapture.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.GoalDao
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(private val goalDao: GoalDao) : ViewModel() {

    val goals: StateFlow<List<GoalEntity>> = goalDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(title: String, targetDate: Long?, targetAmount: Double? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            goalDao.insert(GoalEntity(title = title.trim(), targetDate = targetDate, targetAmount = targetAmount))
        }
    }

    fun toggleCompleted(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.update(
                goal.copy(
                    completed = !goal.completed,
                    completedAt = if (!goal.completed) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun delete(goal: GoalEntity) {
        viewModelScope.launch { goalDao.delete(goal) }
    }
}
