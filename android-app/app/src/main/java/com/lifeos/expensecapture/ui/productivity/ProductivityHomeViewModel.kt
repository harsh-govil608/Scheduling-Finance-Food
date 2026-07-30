package com.lifeos.expensecapture.ui.productivity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.GoalDao
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.ProjectDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.productivity.ProductivityInsightEngine
import com.lifeos.expensecapture.ui.projects.ProjectRow
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class ProductivityHomeUiState(
    val displayName: String = "",
    val profilePhotoPath: String? = null,
    val todayTasks: List<TaskEntity> = emptyList(),
    val totalOpenTasks: Int = 0,
    val pendingHabitsToday: List<HabitEntity> = emptyList(),
    val totalHabits: Int = 0,
    val doneTodayHabitsCount: Int = 0,
    /** Reference-mockup "Projects" preview (2026-07-31 design refresh, see Color.kt's kdoc) -
     * reuses ProjectsViewModel's own ProjectRow/progress math rather than a second hand-copied
     * version. */
    val projects: List<ProjectRow> = emptyList(),
    /** Reference-mockup "Goal Progress" rings - each ring is 100%/0% from the real `completed`
     * flag, not a fabricated numeric progress GoalEntity doesn't track. */
    val goals: List<GoalEntity> = emptyList(),
    val insight: String? = null
)

/**
 * Landing surface for the "Home" pillar (Task Management Doc 10 + Habits Doc 13), folding in
 * Daily Planning (Doc 14) as its primary content rather than a separate screen - the same
 * consolidation choice already made for Spend Prediction inside Budget (see
 * docs/coders-documentation/day-2.md): Daily Planning's core idea ("what should I focus on
 * today") IS what a pillar landing screen should show, not a distinct surface. The full Daily
 * Planning PRD's AI-generated focus suggestions and cross-pillar time-blocking are out of scope -
 * this is a plain filtered view of today's due tasks and not-yet-done habits, nothing inferred.
 */
class ProductivityHomeViewModel(
    context: Context,
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao,
    projectDao: ProjectDao,
    goalDao: GoalDao
) : ViewModel() {

    val uiState: StateFlow<ProductivityHomeUiState> = combine(
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll(),
        projectDao.observeAll(),
        goalDao.observeAll()
    ) { tasks, habits, completions, projects, goals ->
        val zone = ZoneId.systemDefault()
        val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val openTasks = tasks.filter { !it.completed }
        val todayTasks = openTasks.filter { it.dueDate != null && it.dueDate < endOfToday }

        val today = LocalDate.now(zone).toEpochDay()
        val doneTodayHabitIds = completions.filter { it.dateEpochDay == today }.map { it.habitId }.toSet()
        val pendingHabits = habits.filter { it.id !in doneTodayHabitIds }

        val projectRows = projects.map { project ->
            val tasksForProject = tasks.filter { it.projectId == project.id }
            ProjectRow(
                project = project,
                openTaskCount = tasksForProject.count { !it.completed },
                totalTaskCount = tasksForProject.size
            )
        }

        ProductivityHomeUiState(
            displayName = Prefs.getDisplayName(context),
            profilePhotoPath = Prefs.getProfilePhotoPath(context),
            todayTasks = todayTasks,
            totalOpenTasks = openTasks.size,
            pendingHabitsToday = pendingHabits,
            totalHabits = habits.size,
            doneTodayHabitsCount = habits.size - pendingHabits.size,
            projects = projectRows,
            goals = goals,
            insight = ProductivityInsightEngine.compute(tasks, habits, completions)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityHomeUiState())
}
