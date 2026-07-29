package com.lifeos.expensecapture.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.HabitCompletionDao
import com.lifeos.expensecapture.data.db.dao.HabitDao
import com.lifeos.expensecapture.data.db.entity.HabitCompletionEntity
import com.lifeos.expensecapture.data.db.entity.HabitEntity
import com.lifeos.expensecapture.productivity.HabitStreakCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitRow(
    val habit: HabitEntity,
    val doneToday: Boolean,
    val currentStreak: Int
)

/**
 * Habits PRD, Phase 3 Doc 13. The streak computation here is the one part of this feature that
 * directly implements a named requirement, not just a scope cut: the PRD calls the supportive
 * (never reset-to-zero, never shame-coded) streak model one of its highest-risk areas to get
 * wrong. `currentStreak` intentionally does NOT drop to 0 the moment "today" isn't done yet -
 * only after a full day is missed with no completion logged - and `HabitsScreen` never renders
 * copy like "streak broken"; a lapsed streak reads as "ready when you are."
 */
class HabitsViewModel(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) : ViewModel() {

    val habits: StateFlow<List<HabitRow>> = combine(
        habitDao.observeAll(),
        completionDao.observeAll()
    ) { habits, completions ->
        val today = LocalDate.now().toEpochDay()
        val byHabit = completions.groupBy { it.habitId }
        habits.map { habit ->
            val days = byHabit[habit.id]?.map { it.dateEpochDay }?.toSet() ?: emptySet()
            HabitRow(
                habit = habit,
                doneToday = days.contains(today),
                currentStreak = HabitStreakCalculator.currentStreak(days, today)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { habitDao.insert(HabitEntity(name = name.trim())) }
    }

    fun toggleToday(habit: HabitEntity, currentlyDone: Boolean) {
        val today = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            if (currentlyDone) {
                completionDao.delete(habit.id, today)
            } else {
                completionDao.insert(HabitCompletionEntity(habitId = habit.id, dateEpochDay = today))
            }
        }
    }

    fun archive(habit: HabitEntity) {
        viewModelScope.launch { habitDao.update(habit.copy(archived = true)) }
    }
}
