package com.lifeos.expensecapture.ui.ledger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fallback path for the pilot: cash spend and any SMS the parser couldn't handle.
 *
 * Date picker added (found via a real user report, 2026-07): this always saved with
 * System.currentTimeMillis() - if the app missed a real expense (SMS parsing gap, no signal
 * yet, cash), there was no way to backfill it under the date it actually happened, only "now."
 * Defaults to today so the common case (an expense from just now) needs no extra taps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(
    categories: List<CategoryEntity>,
    // Defaults to now for the Ledger's own "+" entry point; UnparsedReviewScreen passes the
    // SMS's actual received time instead, since defaulting an old message's review to "today"
    // would be wrong far more often than right.
    initialDateMillis: Long = System.currentTimeMillis(),
    onConfirm: (amount: Double, merchant: String, direction: TransactionDirection, categoryId: Long, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var isDebit by remember { mutableStateOf(true) }
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add transaction manually") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    FilterChip(
                        selected = isDebit,
                        onClick = { isDebit = true },
                        label = { Text("Spent") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = !isDebit,
                        onClick = { isDebit = false },
                        label = { Text("Received") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Date: ${dateFormat.format(Date(selectedDateMillis))}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull()
                val category = selectedCategory
                if (amount != null && merchant.isNotBlank() && category != null) {
                    onConfirm(
                        amount,
                        merchant,
                        if (isDebit) TransactionDirection.DEBIT else TransactionDirection.CREDIT,
                        category.id,
                        selectedDateMillis
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
