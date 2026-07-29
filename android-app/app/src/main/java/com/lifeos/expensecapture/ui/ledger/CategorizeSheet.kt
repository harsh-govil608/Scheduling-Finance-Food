package com.lifeos.expensecapture.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionEntity

/**
 * Delete option added (found via a real user report, 2026-07): there was previously no way to
 * remove a transaction at all. Placed on this existing "tap a transaction for options" sheet
 * rather than a new gesture/icon on the Ledger row itself - one surface for both actions.
 *
 * Moved out of the category list into the header (found via a second real user report, 2026-07 -
 * "delete is at the very bottom and not correctly placed"): delete used to be the last item
 * inside the same scrollable LazyColumn as every category, meaning it was buried behind however
 * many categories exist (13 defaults plus any custom ones) - a destructive, one-off action
 * doesn't belong grouped with a list of category choices to begin with, scroll position aside.
 * Now a fixed icon next to the title, reachable with zero scrolling regardless of category count.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizeSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onCategorySelected: (Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Recategorize \"${transaction.merchantRaw}\"",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete transaction",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        HorizontalDivider()
        LazyColumn {
            items(categories, key = { it.id }) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    modifier = Modifier.clickable { onCategorySelected(category.id) }
                )
            }
        }
    }
}
