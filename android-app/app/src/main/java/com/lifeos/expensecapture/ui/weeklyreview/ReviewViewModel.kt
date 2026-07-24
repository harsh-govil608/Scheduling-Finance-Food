package com.lifeos.expensecapture.ui.weeklyreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

enum class ReviewPeriod(val days: Int) { WEEK(7), MONTH(30) }

data class ReviewUiState(
    val period: ReviewPeriod = ReviewPeriod.WEEK,
    val tasksCompleted: Int = 0,
    val tasksCreated: Int = 0,
    val habitMaintenancePercent: Int? = null,
    val activeHabitCount: Int = 0
)

/**
 * Weekly Review PRD (Phase 3 Doc 15) and Monthly Review PRD (Doc 16) combined into one screen
 * with a period toggle, the same consolidation pattern as Subscriptions/Bills sharing one
 * detector - both PRDs ask for the same rollup shape at a different window, not different logic.
 * The full PRDs call for AI-generated qualitative insight narratives ("you did X because Y");
 * what's built is the real-numbers version only - completed/created task counts and a habit
 * maintenance percentage - with no invented narrative on top of them.
 */
class ReviewViewModel(
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao
) : ViewModel() {

    private val period = MutableStateFlow(ReviewPeriod.WEEK)

    val uiState: StateFlow<ReviewUiState> = combine(
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll(),
        period
    ) { tasks, habits, completions, selectedPeriod ->
        val today = LocalDate.now()
        val periodStartEpochDay = today.minusDays(selectedPeriod.days.toLong()).toEpochDay()
        val periodStartMillis = today.minusDays(selectedPeriod.days.toLong())
            .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val tasksCompleted = tasks.count { it.completed && (it.completedAt ?: 0) >= periodStartMillis }
        val tasksCreated = tasks.count { it.createdAt >= periodStartMillis }

        val activeHabits = habits.filter { it.createdAt >= 0 } // all non-archived (query already filters archived)
        val maintenancePercent = if (activeHabits.isEmpty()) {
            null
        } else {
            var possibleDays = 0
            var doneDays = 0
            activeHabits.forEach { habit ->
                val habitStartEpochDay = java.time.Instant.ofEpochMilli(habit.createdAt)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
                val windowStart = maxOf(periodStartEpochDay, habitStartEpochDay)
                val daysApplicable = (today.toEpochDay() - windowStart).toInt().coerceAtLeast(0)
                possibleDays += daysApplicable
                doneDays += completions.count { it.habitId == habit.id && it.dateEpochDay in windowStart until today.toEpochDay() }
            }
            if (possibleDays == 0) null else ((doneDays.toDouble() / possibleDays) * 100).toInt()
        }

        ReviewUiState(
            period = selectedPeriod,
            tasksCompleted = tasksCompleted,
            tasksCreated = tasksCreated,
            habitMaintenancePercent = maintenancePercent,
            activeHabitCount = activeHabits.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReviewUiState())

    fun selectPeriod(newPeriod: ReviewPeriod) {
        period.value = newPeriod
    }
}
