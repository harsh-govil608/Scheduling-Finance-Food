package com.lifeos.expensecapture.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.dao.TaskDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

enum class TimelinePillar { FINANCE, HOME }

data class TimelineEntry(
    val timestamp: Long,
    val pillar: TimelinePillar,
    val description: String
)

/**
 * Context Timeline PRD, Phase 3 Doc 32, scoped drastically down: the full PRD implies an
 * AI-driven cross-pillar inference engine ("why this matters now"); what's built here is the
 * honestly-buildable core underneath that - a plain chronological merge of what actually
 * happened today across both pillars (transactions captured, tasks completed, habits checked
 * off), sorted by time, with no inference or ranking. This is the same "connect what's already
 * built, don't invent new intelligence" move as folding Daily Planning into the Home dashboard -
 * every data point here already exists in Finance's or Home's own tables; this just merges them
 * into one feed instead of requiring two separate screens to piece the day together.
 */
class ContextTimelineViewModel(
    transactionDao: TransactionDao,
    taskDao: TaskDao,
    habitDao: HabitDao,
    habitCompletionDao: HabitCompletionDao
) : ViewModel() {

    val entries: StateFlow<List<TimelineEntry>> = combine(
        transactionDao.observeAll(),
        taskDao.observeAll(),
        habitDao.observeAll(),
        habitCompletionDao.observeAll()
    ) { transactions, tasks, habits, completions ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfToday = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayEpochDay = today.toEpochDay()

        val entries = mutableListOf<TimelineEntry>()

        transactions.filter { it.date in startOfToday until endOfToday }.forEach { txn ->
            val verb = if (txn.direction == TransactionDirection.DEBIT) "Spent" else "Received"
            entries += TimelineEntry(
                timestamp = txn.date,
                pillar = TimelinePillar.FINANCE,
                description = "$verb ₹${"%.2f".format(txn.amount)} - ${txn.merchantRaw}"
            )
        }

        tasks.filter { it.completed && (it.completedAt ?: 0) in startOfToday until endOfToday }.forEach { task ->
            entries += TimelineEntry(
                timestamp = task.completedAt ?: 0,
                pillar = TimelinePillar.HOME,
                description = "Completed task - ${task.title}"
            )
        }

        val habitById = habits.associateBy { it.id }
        completions.filter { it.dateEpochDay == todayEpochDay }.forEach { completion ->
            val habitName = habitById[completion.habitId]?.name ?: return@forEach
            entries += TimelineEntry(
                // Habit completions only store a calendar day, not a time - anchored to the
                // start of today so they sort alongside timed events without claiming a false
                // precision about exactly when the habit was checked off.
                timestamp = startOfToday,
                pillar = TimelinePillar.HOME,
                description = "Checked off habit - $habitName"
            )
        }

        entries.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
