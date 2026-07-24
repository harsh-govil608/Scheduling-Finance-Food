package com.lifeos.expensecapture.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class Pillar { FINANCE, HOME }

/**
 * First real multi-pillar navigation element in the app - until today every screen was
 * implicitly "Finance" (the TopAppBar even says "Finance" outright). Only shown on each pillar's
 * own landing screen (Finance's HomeScreen, Home's ProductivityHomeScreen), not on drill-down
 * detail screens (Ledger, Tasks, etc.) - standard Android practice for tab-style navigation, and
 * avoids needing a nested NavHost/back-stack redesign for a 2-tab pilot. Food is deliberately
 * absent - see docs/coders-documentation/day-3.md's Known Gaps for why it's not started.
 */
@Composable
fun PillarBottomBar(current: Pillar, onSelect: (Pillar) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Pillar.FINANCE,
            onClick = { onSelect(Pillar.FINANCE) },
            icon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
            label = { Text("Finance") }
        )
        NavigationBarItem(
            selected = current == Pillar.HOME,
            onClick = { onSelect(Pillar.HOME) },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            label = { Text("Home") }
        )
    }
}
