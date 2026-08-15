package com.lifeos.expensecapture.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.lifeos.expensecapture.R

enum class Pillar { FINANCE, HOME, ANALYTICS, AI, PROFILE }

/**
 * Five-tab bottom nav (2026-08 reference mockups, `ui2/` folder) - Finance/Home/Analytics were
 * already pillars; AI and Profile move here from being a FAB (AssistantScreen) and an avatar-button
 * destination (ProfileScreen) respectively, matching every one of the new mockups' bottom bar. AI
 * is still the same rule-based CommandIntent assistant, not a new model - see AssistantScreen's
 * kdoc; this is a navigation change only. The avatar-button shortcut to Profile and the old
 * chat-FAB are left in place elsewhere too - both are just additional paths to the same routes,
 * not a conflict.
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
            label = { Text(stringResource(R.string.nav_finance)) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == Pillar.HOME,
            onClick = { onSelect(Pillar.HOME) },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == Pillar.ANALYTICS,
            onClick = { onSelect(Pillar.ANALYTICS) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_analytics)) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == Pillar.AI,
            onClick = { onSelect(Pillar.AI) },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_ai)) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = current == Pillar.PROFILE,
            onClick = { onSelect(Pillar.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            colors = itemColors
        )
    }
}
