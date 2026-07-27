package com.lifeos.expensecapture.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.data.db.dao.ShoppingItemDao
import com.lifeos.expensecapture.data.db.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** AI Transformation Plan F2: a soft "about due" suggestion for an item that isn't currently on
 * the list but has a real history of being bought on roughly this cadence - never auto-added,
 * see ShoppingScreen for the accept action. */
data class ShoppingSuggestion(val name: String, val quantity: String, val daysSinceLastBought: Int)

private const val MIN_CHECKED_OCCURRENCES_FOR_SUGGESTION = 2
private const val MILLIS_PER_DAY = 86_400_000.0

class ShoppingViewModel(private val dao: ShoppingItemDao) : ViewModel() {

    val items: StateFlow<List<ShoppingItemEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<ShoppingSuggestion>> = dao.observeAll()
        .map(::computeSuggestions)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, quantity: String) {
        if (name.isBlank()) return
        viewModelScope.launch { dao.insert(ShoppingItemEntity(name = name.trim(), quantity = quantity.trim())) }
    }

    fun toggleChecked(item: ShoppingItemEntity) {
        val nowChecked = !item.checked
        viewModelScope.launch {
            dao.update(
                item.copy(
                    checked = nowChecked,
                    checkedAt = if (nowChecked) System.currentTimeMillis() else item.checkedAt
                )
            )
        }
    }

    fun delete(item: ShoppingItemEntity) {
        viewModelScope.launch { dao.delete(item) }
    }

    fun acceptSuggestion(suggestion: ShoppingSuggestion) {
        addItem(suggestion.name, suggestion.quantity)
    }
}

/** Groups every item ever recorded (checked items are never auto-deleted - see
 * ShoppingItemEntity's kdoc on scope) by name, and looks for repeated check-off events that
 * imply a cadence - the same interval-averaging idea RecurringPatternDetector already applies to
 * transactions, kept as its own small function here rather than forcing a shared abstraction
 * across two genuinely different input shapes for a single reuse. */
private fun computeSuggestions(allItems: List<ShoppingItemEntity>): List<ShoppingSuggestion> {
    val activeNames = allItems.filter { !it.checked }.map { it.name.trim().lowercase() }.toSet()
    val now = System.currentTimeMillis()

    return allItems
        .groupBy { it.name.trim().lowercase() }
        .mapNotNull { (normalizedName, group) ->
            if (normalizedName in activeNames) return@mapNotNull null // already back on the list

            val checkedTimestamps = group.mapNotNull { it.checkedAt }.sorted()
            if (checkedTimestamps.size < MIN_CHECKED_OCCURRENCES_FOR_SUGGESTION) return@mapNotNull null

            val averageIntervalDays = checkedTimestamps.zipWithNext { a, b -> (b - a) / MILLIS_PER_DAY }.average()
            val daysSinceLast = (now - checkedTimestamps.last()) / MILLIS_PER_DAY
            if (daysSinceLast < averageIntervalDays * 0.8) return@mapNotNull null // not due yet

            val mostRecent = group.maxBy { it.checkedAt ?: 0L }
            ShoppingSuggestion(mostRecent.name, mostRecent.quantity, daysSinceLast.toInt())
        }
        .sortedByDescending { it.daysSinceLastBought }
}
