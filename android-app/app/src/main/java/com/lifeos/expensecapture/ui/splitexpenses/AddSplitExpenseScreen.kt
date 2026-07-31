package com.lifeos.expensecapture.ui.splitexpenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App

private class ParticipantInput(name: String = "", share: String = "") {
    var name by mutableStateOf(name)
    var share by mutableStateOf(share)
}

/**
 * Add Split Expense - description + total, then who owes what. "Split equally" is a convenience
 * that overwrites every current share field with total/participantCount; it's a one-time fill,
 * not a live constraint - shares can still be hand-edited afterward for an uneven split (e.g.
 * someone only had a side dish).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSplitExpenseScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        SplitExpensesViewModel(
            splitExpenseDao = app.database.splitExpenseDao(),
            splitParticipantDao = app.database.splitParticipantDao()
        )
    }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf(ParticipantInput(), ParticipantInput()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Split Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What was it? (e.g. Dinner at XYZ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total amount you paid") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Who's splitting it?", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = {
                        val total = amountText.toDoubleOrNull()
                        if (total != null && participants.isNotEmpty()) {
                            val each = total / participants.size
                            participants.forEach { it.share = "%.2f".format(each) }
                        }
                    }) { Text("Split equally") }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participants.size) { index ->
                    val entry = participants[index]
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = entry.name,
                            onValueChange = { entry.name = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = entry.share,
                            onValueChange = { entry.share = it },
                            label = { Text("Share") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { participants.removeAt(index) },
                            enabled = participants.size > 1
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove person")
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { participants.add(ParticipantInput()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add person")
                    }
                }
            }

            Button(
                onClick = {
                    val total = amountText.toDoubleOrNull()
                    val validParticipants = participants
                        .filter { it.name.isNotBlank() }
                        .mapNotNull { entry -> entry.share.toDoubleOrNull()?.let { entry.name to it } }
                    if (description.isNotBlank() && total != null && total > 0 && validParticipants.isNotEmpty()) {
                        viewModel.addExpense(description, total, System.currentTimeMillis(), validParticipants)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Save") }
        }
    }
}
