package com.lifeos.expensecapture.ui.nightsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.finance.FinanceInsightsRepository
import com.lifeos.expensecapture.ui.common.AccentInfoCard
import com.lifeos.expensecapture.ui.common.HeroMoneyCard

/**
 * Night Summary PRD, Phase 3 Doc 02: closes the day as evidence the AI was paying attention,
 * never as judgment. No "you overspent" framing anywhere here, per the PRD's anti-guilt
 * requirement and Phase 1's Guiding Principles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NightSummaryScreen(app: App, onBack: () -> Unit) {
    val viewModel = remember {
        NightSummaryViewModel(
            transactionDao = app.database.transactionDao(),
            insightsRepository = FinanceInsightsRepository(
                transactionDao = app.database.transactionDao(),
                categoryDao = app.database.categoryDao(),
                budgetDao = app.database.budgetDao(),
                subscriptionDao = app.database.subscriptionDao(),
                billDao = app.database.billDao()
            ),
            taskDao = app.database.taskDao(),
            habitDao = app.database.habitDao(),
            habitCompletionDao = app.database.habitCompletionDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your day") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroMoneyCard(
                    label = "Today",
                    amount = uiState.todaySpend,
                    caption = if (uiState.todayCount == 0) {
                        "A quiet day - nothing captured, nothing missed."
                    } else {
                        "${uiState.todayCount} transaction${if (uiState.todayCount == 1) "" else "s"} today, " +
                            "${uiState.autoCapturedCount} of them captured automatically - no typing needed."
                    }
                )
            }
            if (uiState.tasksCompletedToday > 0 || uiState.totalHabits > 0) {
                item {
                    AccentInfoCard(
                        icon = Icons.Filled.CheckCircle,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        title = "Beyond Finance today",
                        body = buildString {
                            if (uiState.tasksCompletedToday > 0) {
                                append("${uiState.tasksCompletedToday} task${if (uiState.tasksCompletedToday == 1) "" else "s"} completed")
                            }
                            if (uiState.totalHabits > 0) {
                                if (isNotEmpty()) append(", ")
                                append("${uiState.habitsMaintainedToday} of ${uiState.totalHabits} habits kept up")
                            }
                        }
                    )
                }
            }
            item {
                val diff = uiState.todaySpend - uiState.yesterdaySpend
                AccentInfoCard(
                    icon = when {
                        diff > 0 -> Icons.Filled.TrendingUp
                        diff < 0 -> Icons.Filled.TrendingDown
                        else -> Icons.Filled.TrendingFlat
                    },
                    accentColor = MaterialTheme.colorScheme.secondary,
                    title = "Compared to yesterday",
                    body = when {
                        uiState.yesterdaySpend == 0.0 && uiState.todaySpend == 0.0 -> "Both quiet days."
                        diff > 0 -> "₹${"%.2f".format(diff)} more than yesterday - just for context, not a target to hit."
                        diff < 0 -> "₹${"%.2f".format(-diff)} less than yesterday."
                        else -> "About the same as yesterday."
                    }
                )
            }
            item {
                AccentInfoCard(
                    icon = Icons.Filled.CalendarMonth,
                    accentColor = MaterialTheme.colorScheme.primary,
                    title = "Tomorrow",
                    body = if (uiState.billsDueTomorrow.isEmpty()) {
                        "Nothing due that we know of - a clear start."
                    } else {
                        "Coming up: ${uiState.billsDueTomorrow.joinToString(", ")}"
                    }
                )
            }
        }
    }
}
