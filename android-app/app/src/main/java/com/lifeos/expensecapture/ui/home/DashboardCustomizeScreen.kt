package com.lifeos.expensecapture.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.util.Prefs

/**
 * Customizable Dashboard (2026-08, real user request). Applies immediately on every
 * toggle/reorder tap - same "no separate Save step" pattern SettingsToggleRow already uses
 * elsewhere in this app - since there's nothing here that benefits from a draft/confirm step.
 * Up/down arrows rather than drag-and-drop: no reorder library exists in this codebase and one
 * isn't worth adding for 6 items - this is simpler and fully keyboard/accessibility-friendly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCustomizeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var order by remember { mutableStateOf(Prefs.getHomeSectionOrder(context)) }
    val hidden = HomeSection.entries.filterNot { it in order }

    fun persist(newOrder: List<HomeSection>) {
        order = newOrder
        Prefs.setHomeSectionOrder(context, newOrder)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Choose which sections show on your Home screen, and reorder them. Changes apply immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { SectionLabel("Visible") }
            items(order, key = { it.name }) { section ->
                DashboardSectionRow(
                    section = section,
                    visible = true,
                    onToggle = { persist(order - section) },
                    onMoveUp = if (order.firstOrNull() != section) ({ persist(order.movedUp(section)) }) else null,
                    onMoveDown = if (order.lastOrNull() != section) ({ persist(order.movedDown(section)) }) else null
                )
            }
            if (hidden.isNotEmpty()) {
                item { SectionLabel("Hidden") }
                items(hidden, key = { it.name }) { section ->
                    DashboardSectionRow(
                        section = section,
                        visible = false,
                        onToggle = { persist(order + section) },
                        onMoveUp = null,
                        onMoveDown = null
                    )
                }
            }
        }
    }
}

private fun List<HomeSection>.movedUp(section: HomeSection): List<HomeSection> {
    val index = indexOf(section)
    if (index <= 0) return this
    return toMutableList().apply { removeAt(index); add(index - 1, section) }
}

private fun List<HomeSection>.movedDown(section: HomeSection): List<HomeSection> {
    val index = indexOf(section)
    if (index < 0 || index >= size - 1) return this
    return toMutableList().apply { removeAt(index); add(index + 1, section) }
}

@Composable
private fun DashboardSectionRow(
    section: HomeSection,
    visible: Boolean,
    onToggle: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = visible, onCheckedChange = { onToggle() })
            Text(section.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (visible) {
                IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                }
            }
        }
    }
}
