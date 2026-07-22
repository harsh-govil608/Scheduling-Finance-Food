package com.lifeos.expensecapture.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizeSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onCategorySelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Recategorize \"${transaction.merchantRaw}\"",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
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
