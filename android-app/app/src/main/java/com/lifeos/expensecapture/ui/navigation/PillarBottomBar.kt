package com.lifeos.expensecapture.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class Pillar { FINANCE, HOME }

/**
 * First real multi-pillar navigation element in the app - until today every screen was
 * implicitly "Finance" (the TopAppBar even says "Finance" outright). Only shown on each pillar's
 * own landing screen (Finance's HomeScreen, Home's ProductivityHomeScreen), not on drill-down
 * detail screens (Ledger, Tasks, etc.) - standard Android practice for tab-style navigation, and
 * avoids needing a nested NavHost/back-stack redesign for a 2-tab pilot. Food is deliberately
 * absent - see docs/coders-documentation/day-3.md's Known Gaps for why it's not started.
 *
 * Selected-tab treatment (2026-07-31 dark refresh) matches the reference mockups' bottom nav: the
 * icon and label simply switch to the mint primary color, no pill-shaped indicator behind them -
 * so `indicatorColor` is forced transparent rather than the Material3 default secondaryContainer
 * pill.
 */
@Composable
fun PillarBottomBar(current: Pillar, onSelect: (Pillar) -> Unit) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        NavigationBarItem(
            selected = current == Pillar.FINANCE,
            onClick = { onSelect(Pillar.FINANCE) },
            icon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
            label = { Text("Finance") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == Pillar.HOME,
            onClick = { onSelect(Pillar.HOME) },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            label = { Text("Home") },
            colors = itemColors
        )
    }
}
