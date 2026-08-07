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
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyLedgerRepository
import com.lifeos.expensecapture.family.data.FamilyRepository
import com.lifeos.expensecapture.productivity.HabitStreakCalculator
import com.lifeos.expensecapture.productivity.ProductivityInsightEngine
import com.lifeos.expensecapture.ui.projects.ProjectRow
import com.lifeos.expensecapture.util.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One real, computed suggestion for the "AI Suggestions" card (2026-08, `ui3/` reference -
 * redesigned from a single paragraph into a short list of tappable tips). Deliberately built from
 * up to 3 independent, already-real signals rather than an AI call inventing several - see
 * ProductivityHomeViewModel's kdoc on why each one is computed, not fabricated. */
enum class HomeTipKind { BUDGET, TASK, INSIGHT }
data class HomeTip(val kind: HomeTipKind, val text: String)

enum class ActivityKind { TASK, HABIT, EXPENSE }
data class RecentActivityItem(val kind: ActivityKind, val text: String, val timestamp: Long)

data class ProductivityHomeUiState(
    val displayName: String = "",
    val profilePhotoPath: String? = null,
    val todayTasks: List<TaskEntity> = emptyList(),
    val totalOpenTasks: Int = 0,
    val pendingHabitsToday: List<HabitEntity> = emptyList(),
    val totalHabits: Int = 0,
    val doneTodayHabitsCount: Int = 0,
    /** Composite of today's real task/habit completion signals (2026-08, `ui3/` reference's
     * "Today Score" card) - the average of whichever of (tasks completed today / tasks completed
     * or still due today) and (habits done today / total habits) actually has data, scaled to a
     * percentage. Null when neither signal exists yet (no habits and nothing due/completed today)
     * - a fresh/quiet day gets an honest empty state, not a fabricated 100%. */
    val todayScore: Int? = null,
    /** Real, computed suggestions - see [HomeTip]'s kdoc. Empty list hides the card entirely
     * rather than padding it with a placeholder. */
    val tips: List<HomeTip> = emptyList(),
    /** Engagement hook (moved here from Finance's Home, 2026-08 - the founder's own feedback:
     * habits belong with the rest of the Home pillar's habit content, not on the Finance
     * screen). The best current streak across all habits - see HabitStreakCalculator's kdoc.
     * 0 means no active streak (or no habits at all), rendered as absent rather than a
     * discouraging "0-day streak". */
    val bestHabitStreak: Int = 0,
    /** Mini "Tasks vs Habits completed" chart (2026-08 visual polish pass, real user request:
     * the empty space beside the Projects card when there's only one project). Deliberately
     * Home-pillar data, not a repeat of Finance's own spend chart - real completedAt/dateEpochDay
     * counts per day, nothing fabricated. */
    val tasksCompletedLast7Days: List<Float> = emptyList(),
    val habitsCompletedLast7Days: List<Float> = emptyList(),
    /** Reference-mockup "Projects" preview (2026-07-31 design refresh, see Color.kt's kdoc) -
     * reuses ProjectsViewModel's own ProjectRow/progress math rather than a second hand-copied
     * version. */
    val projects: List<ProjectRow> = emptyList(),
    /** Reference-mockup "Goal Progress" rings - each ring is 100%/0% from the real `completed`
     * flag, not a fabricated numeric progress GoalEntity doesn't track. */
    val goals: List<GoalEntity> = emptyList(),
    /** Today's real family spend total, from the same shared ledger the Family Dashboard reads
     * (2026-08, real user request - the original Home tab text spec's "Family spent ₹1,850"
     * stat). Null when the signed-in user isn't in any family at all - distinct from 0.0 (in a
     * family, nothing spent yet) - so the card can hide itself entirely rather than showing a
     * misleading ₹0 to someone who never linked a family. */
    val todayFamilySpend: Double? = null,
    /** Recent Activity feed (2026-08, real user request - the original Home tab text spec's
     * "Recent Activity" section, previously only shown on the Family Dashboard). Personal signals
     * only (completed tasks/habits, logged transactions) - explicitly NOT family activity, per a
     * follow-up founder request: this is the individual's own Home tab, family events belong on
     * the Family Dashboard, not mixed into a personal feed. Newest first, capped at 5 - a
     * landing-surface preview, not a full activity log. */
    val recentActivity: List<RecentActivityItem> = emptyList()
)

/** Keeps the top-level combine() to a 4-arg overload instead of an untyped vararg across many
 * heterogeneous flows - same pattern as HomeViewModel's FinanceSnapshot/StatusSnapshot. */
private data class TaskHabitSnapshot(
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val completions: List<HabitCompletionEntity>
)

private data class DailyFinanceSnapshot(
    /** How much room is left in the "Overall" (categoryId == null) monthly budget, divided by
     * days remaining in the month - null when no Overall budget is set, per-category budgets
     * alone aren't summed here to avoid double-counting spend that could fall under more than
     * one. Feeds the BUDGET tip in [ProductivityHomeUiState.tips]. */
    val safeToSpendToday: Double?,
    /** Real recent debit/credit transactions, newest first, for the Recent Activity feed - a
     * small slice of the same data the Finance pillar already tracks, not a duplicate ledger. */
    val recentTransactions: List<RecentActivityItem>
)

private data class FamilySnapshot(
    val todayFamilySpend: Double?
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
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProductivityHomeViewModel(
    context: Context,
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao,
    projectDao: ProjectDao,
    goalDao: GoalDao,
    transactionDao: TransactionDao,
    budgetDao: BudgetDao,
    familyAuthRepository: FamilyAuthRepository = FamilyAuthRepository(),
    familyRepository: FamilyRepository = FamilyRepository(),
    familyLedgerRepository: FamilyLedgerRepository = FamilyLedgerRepository()
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
        val thisMonthDebits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.date >= monthStart }
        val overallBudget = budgets.firstOrNull { it.categoryId == null }
        val safeToSpendToday = overallBudget?.let { budget ->
            val spent = thisMonthDebits.sumOf { it.amount }
            val remaining = budget.monthlyLimit - spent
            val daysLeftInMonth = (today.lengthOfMonth() - today.dayOfMonth + 1).coerceAtLeast(1)
            (remaining / daysLeftInMonth).takeIf { it > 0.0 }
        }
        val recentTransactions = transactions.sortedByDescending { it.date }.take(8).map { txn ->
            val verb = if (txn.direction == TransactionDirection.DEBIT) "Spent" else "Received"
            RecentActivityItem(
                ActivityKind.EXPENSE,
                "$verb ₹${"%.0f".format(txn.amount)} - ${txn.merchantRaw}",
                txn.date
            )
        }
        DailyFinanceSnapshot(safeToSpendToday, recentTransactions)
    }

    // Family spend (2026-08, real user request) - a one-time snapshot of "am I signed into the
    // Family module at all," same simplification FamilyEntryScreen already makes
    // (families.first() = the active family; multi-family switching isn't supported anywhere
    // yet). A user who never opened Family Sharing gets flowOf(no data) here, never touching
    // Firestore - this Flow is genuinely optional, not a hidden requirement to sign in. Family
    // *activity* was deliberately dropped from here (real follow-up founder request) - this is
    // the individual's own Home tab; family events belong on the Family Dashboard, not mixed
    // into a personal feed two different family members would otherwise see diverging copies of.
    private val familySnapshot: Flow<FamilySnapshot> = run {
        val uid = familyAuthRepository.currentUser?.uid
        if (uid == null) {
            flowOf(FamilySnapshot(null))
        } else {
            familyRepository.observeUserFamilies(uid).flatMapLatest { families ->
                val family = families.firstOrNull()
                if (family == null) {
                    flowOf(FamilySnapshot(null))
                } else {
                    val zone = ZoneId.systemDefault()
                    val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
                    familyLedgerRepository.observeEntries(family.id, todayStart).map { entries ->
                        FamilySnapshot(entries.filter { it.direction == "DEBIT" }.sumOf { it.amount })
                    }
                }
            }
        }
    }

    val uiState: StateFlow<ProductivityHomeUiState> = combine(
        taskHabitSnapshot,
        dailyFinanceSnapshot,
        projectDao.observeAll(),
        goalDao.observeAll(),
        familySnapshot
    ) { taskHabit, dailyFinance, projects, goals, family ->
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

        // Mini "Tasks vs Habits completed" chart data (see ProductivityHomeUiState's kdoc) - real
        // completedAt/dateEpochDay counts per day, last 7 days ending today.
        val last7Days = (6 downTo 0).map { today - it }
        val tasksCompletedLast7Days = last7Days.map { day ->
            tasks.count { task ->
                task.completedAt != null &&
                    Instant.ofEpochMilli(task.completedAt).atZone(zone).toLocalDate().toEpochDay() == day
            }.toFloat()
        }
        val habitsCompletedLast7Days = last7Days.map { day ->
            completions.count { it.dateEpochDay == day }.toFloat()
        }

        val projectRows = projects.map { project ->
            val tasksForProject = tasks.filter { it.projectId == project.id }
            ProjectRow(
                project = project,
                openTaskCount = tasksForProject.count { !it.completed },
                totalTaskCount = tasksForProject.size
            )
        }

        val doneTodayHabitsCount = habits.size - pendingHabits.size

        // Today Score (see ProductivityHomeUiState.todayScore's kdoc) - average of whichever of
        // the two components actually has data this pass, so a habits-only or tasks-only day
        // still gets a real score instead of one component silently dragging it toward zero.
        val tasksCompletedTodayCount = tasksCompletedLast7Days.last().toInt()
        val taskComponent = (tasksCompletedTodayCount + todayTasks.size)
            .takeIf { it > 0 }
            ?.let { denominator -> tasksCompletedTodayCount.toFloat() / denominator }
        val habitComponent = habits.size.takeIf { it > 0 }?.let { doneTodayHabitsCount.toFloat() / it }
        val todayScore = listOfNotNull(taskComponent, habitComponent)
            .takeIf { it.isNotEmpty() }
            ?.let { components -> ((components.sum() / components.size) * 100).toInt() }

        // AI Suggestions tips (see HomeTip's kdoc) - up to 3 real signals, never padded with a
        // fabricated one when fewer are available.
        val tips = buildList {
            dailyFinance.safeToSpendToday?.let { safeAmount ->
                add(HomeTip(HomeTipKind.BUDGET, "Spend below ₹${"%.0f".format(safeAmount)} today to stay within budget."))
            }
            todayTasks.maxByOrNull { it.priority.ordinal }?.let { task ->
                add(HomeTip(HomeTipKind.TASK, "\"${task.title}\" is still open - worth tackling today."))
            }
            ProductivityInsightEngine.compute(tasks, habits, completions)?.let { insight ->
                add(HomeTip(HomeTipKind.INSIGHT, insight))
            }
        }

        // Recent Activity (see ProductivityHomeUiState.recentActivity's kdoc) - personal task/
        // habit completions and recent transactions only, no family activity.
        val taskActivity = tasks.mapNotNull { task ->
            task.completedAt?.let { RecentActivityItem(ActivityKind.TASK, "Completed \"${task.title}\"", it) }
        }
        val habitActivity = completions.mapNotNull { completion ->
            habits.firstOrNull { it.id == completion.habitId }?.let { habit ->
                val timestamp = completion.dateEpochDay * 86_400_000L
                RecentActivityItem(ActivityKind.HABIT, "Completed \"${habit.name}\"", timestamp)
            }
        }
        // Capped at 5 (real user request, 2026-08) - the Home tab is a landing surface, not a
        // full activity log; "View all" style browsing belongs elsewhere.
        val recentActivity = (taskActivity + habitActivity + dailyFinance.recentTransactions)
            .sortedByDescending { it.timestamp }
            .take(5)

        ProductivityHomeUiState(
            displayName = Prefs.getDisplayName(context),
            profilePhotoPath = Prefs.getProfilePhotoPath(context),
            todayTasks = todayTasks,
            totalOpenTasks = openTasks.size,
            pendingHabitsToday = pendingHabits,
            totalHabits = habits.size,
            doneTodayHabitsCount = doneTodayHabitsCount,
            bestHabitStreak = bestStreak,
            todayScore = todayScore,
            tips = tips,
            tasksCompletedLast7Days = tasksCompletedLast7Days,
            habitsCompletedLast7Days = habitsCompletedLast7Days,
            projects = projectRows,
            goals = goals,
            todayFamilySpend = family.todayFamilySpend,
            recentActivity = recentActivity
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityHomeUiState())
}
