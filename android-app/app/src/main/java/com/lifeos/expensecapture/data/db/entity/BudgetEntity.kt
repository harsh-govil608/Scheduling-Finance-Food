package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Budget Planner PRD (Phase 3, Doc 20). Supports category budgets (categoryId set) and one
 * overall budget (categoryId = null) - goal-linked budgets are explicitly deferred, per the
 * PRD's own "Open Questions" section flagging them as possibly out of first release.
 *
 * Period policy: reset-each-calendar-month, no rollover. The PRD names rollover-vs-reset as a
 * requirement to define, not a specific choice - reset was picked here for simplicity; revisit
 * if real usage shows rollover matters.
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long?,
    val monthlyLimit: Double,
    val createdAt: Long = System.currentTimeMillis()
)
