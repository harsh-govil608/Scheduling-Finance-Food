package com.lifeos.expensecapture.ui.productivity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.BudgetDao
import com.lifeos.expensecapture.data.db.dao.GoalDao
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.ProjectDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.GoalEntity
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.data.db.entity.TaskEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.productivity.HabitStreakCalculator
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
    /** Engagement hook (moved here from Finance's Home, 2026-08 - the founder's own feedback:
     * habits belong with the rest of the Home pillar's habit content, not on the Finance
     * screen). The best current streak across all habits - see HabitStreakCalculator's kdoc.
     * 0 means no active streak (or no habits at all), rendered as absent rather than a
     * discouraging "0-day streak". */
    val bestHabitStreak: Int = 0,
    /** Daily Summary row (2026-08 reference mockups, `ui2/` folder) - spend and budget status
     * are the same real figures Finance's own screens show, cross-read here rather than
     * recomputed differently. */
    val spentToday: Double = 0.0,
    /** Null when no budgets are set at all - nothing real to report "on track" against. */
    val allBudgetsOnTrack: Boolean? = null,
    /** Mini spend-trend chart (2026-08 visual polish pass, real user request: the empty space
     * beside Projects/Goal Progress when there's only one of each). Same last-7-days-of-real-DEBIT
     * -transactions computation as HomeViewModel's own last7DaysSpend, not a separate metric. */
    val last7DaysSpend: List<Float> = emptyList(),
    /** Reference-mockup "Projects" preview (2026-07-31 design refresh, see Color.kt's kdoc) -
     * reuses ProjectsViewModel's own ProjectRow/progress math rather than a second hand-copied
     * version. */
    val projects: List<ProjectRow> = emptyList(),
    /** Reference-mockup "Goal Progress" rings - each ring is 100%/0% from the real `completed`
     * flag, not a fabricated numeric progress GoalEntity doesn't track. */
    val goals: List<GoalEntity> = emptyList(),
    val insight: String? = null
)

/** Keeps the top-level combine() to a 4-arg overload instead of an untyped vararg across many
 * heterogeneous flows - same pattern as HomeViewModel's FinanceSnapshot/StatusSnapshot. */
private data class TaskHabitSnapshot(
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val completions: List<HabitCompletionEntity>
)

private data class DailyFinanceSnapshot(
    val spentToday: Double,
    val allBudgetsOnTrack: Boolean?,
    val last7DaysSpend: List<Float>
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
    goalDao: GoalDao,
    transactionDao: TransactionDao,
    budgetDao: BudgetDao
) : ViewModel() {

    private val taskHabitSnapshot = combine(
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll()
    ) { tasks, habits, completions -> TaskHabitSnapshot(tasks, habits, completions) }

    private val dailyFinanceSnapshot = combine(
        transactionDao.observeAll(),
        budgetDao.observeAll()
    ) { transactions, budgets ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val spentToday = transactions
            .filter { it.direction == TransactionDirection.DEBIT && it.date >= todayStart }
            .sumOf { it.amount }
        val thisMonthDebits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.date >= monthStart }
        val allOnTrack = if (budgets.isEmpty()) {
            null
        } else {
            budgets.all { budget ->
                val relevant = thisMonthDebits.filter { budget.categoryId == null || it.categoryId == budget.categoryId }
                relevant.sumOf { it.amount } <= budget.monthlyLimit
            }
        }
        val last7DaysSpend = (6 downTo 0).map { today.minusDays(it.toLong()) }.map { day ->
            val start = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            transactions
                .filter { it.direction == TransactionDirection.DEBIT && it.date in start until end }
                .sumOf { it.amount }
                .toFloat()
        }
        DailyFinanceSnapshot(spentToday, allOnTrack, last7DaysSpend)
    }

    val uiState: StateFlow<ProductivityHomeUiState> = combine(
        taskHabitSnapshot,
        dailyFinanceSnapshot,
        projectDao.observeAll(),
        goalDao.observeAll()
    ) { taskHabit, dailyFinance, projects, goals ->
        val (tasks, habits, completions) = taskHabit
        val zone = ZoneId.systemDefault()
        val endOfToday = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val openTasks = tasks.filter { !it.completed }
        val todayTasks = openTasks.filter { it.dueDate != null && it.dueDate < endOfToday }

        val today = LocalDate.now(zone).toEpochDay()
        val doneTodayHabitIds = completions.filter { it.dateEpochDay == today }.map { it.habitId }.toSet()
        val pendingHabits = habits.filter { it.id !in doneTodayHabitIds }

        val byHabit = completions.groupBy { it.habitId }
        val bestStreak = habits.maxOfOrNull { habit ->
            val days = byHabit[habit.id]?.map { it.dateEpochDay }?.toSet() ?: emptySet()
            HabitStreakCalculator.currentStreak(days, today)
        } ?: 0

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
            bestHabitStreak = bestStreak,
            spentToday = dailyFinance.spentToday,
            allBudgetsOnTrack = dailyFinance.allBudgetsOnTrack,
            last7DaysSpend = dailyFinance.last7DaysSpend,
            projects = projectRows,
            goals = goals,
            insight = ProductivityInsightEngine.compute(tasks, habits, completions)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityHomeUiState())
}
