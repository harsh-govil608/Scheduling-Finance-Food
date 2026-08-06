package com.lifeos.expensecapture.family.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class FamilyPillar { HOME, EXPENSES, TASKS, CALENDAR, MORE }

/**
 * Family module's own 5-tab bottom nav (2026-08, `ui3/` reference mockups) - the Family module
 * already has six shared destinations (Tasks/Calendar/Expenses/Documents/Health/Contacts) plus
 * Members/Invite/SOS/Notifications, too many for a flat grid to read as "the app" the way the
 * reference intends. The three highest-traffic modules (Expenses/Tasks/Calendar) get their own
 * tab; everything else moves under More (see FamilyMoreScreen). Same selected-tab treatment as
 * the main PillarBottomBar (mint color swap, no pill indicator) for visual consistency between
 * the two bottom bars a user sees in this app.
 */
@Composable
fun FamilyPillarBottomBar(current: FamilyPillar, onSelect: (FamilyPillar) -> Unit) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        NavigationBarItem(
            selected = current == FamilyPillar.HOME,
            onClick = { onSelect(FamilyPillar.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == FamilyPillar.EXPENSES,
            onClick = { onSelect(FamilyPillar.EXPENSES) },
            icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null) },
            label = { Text("Expenses") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == FamilyPillar.TASKS,
            onClick = { onSelect(FamilyPillar.TASKS) },
            icon = { Icon(Icons.Filled.Checklist, contentDescription = null) },
            label = { Text("Tasks") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == FamilyPillar.CALENDAR,
            onClick = { onSelect(FamilyPillar.CALENDAR) },
            icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            label = { Text("Calendar") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == FamilyPillar.MORE,
            onClick = { onSelect(FamilyPillar.MORE) },
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = null) },
            label = { Text("More") },
            colors = itemColors
        )
    }
}
