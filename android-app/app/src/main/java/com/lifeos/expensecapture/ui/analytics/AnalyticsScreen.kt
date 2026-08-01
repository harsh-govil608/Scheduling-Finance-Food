package com.lifeos.expensecapture.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.ui.common.GreetingTitle
import com.lifeos.expensecapture.ui.common.ProfileAvatarButton
import com.lifeos.expensecapture.ui.common.ProgressRing
import com.lifeos.expensecapture.ui.common.SectionLabel
import com.lifeos.expensecapture.ui.common.StatTile
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import com.lifeos.expensecapture.ui.navigation.Pillar
import com.lifeos.expensecapture.ui.navigation.PillarBottomBar
import com.lifeos.expensecapture.ui.theme.Warning
import com.lifeos.expensecapture.ui.theme.WarningStrong
import com.lifeos.expensecapture.util.Prefs

/**
 * Analytics pillar landing surface - dashboards/charts/analysis per the founder's own request,
 * redesigned 2026-08 to match the `ui2/` reference mockups' time-range chips, Spending Overview
 * donut, grouped Income vs Expense bars, and Savings Rate / Avg Daily Spend / Financial Health
 * Score cards - every figure real (see AnalyticsViewModel's kdoc), the health score deterministic
 * (see FinancialHealthScore's kdoc), not AI/ML.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(app: App, onSelectPillar: (Pillar) -> Unit, onOpenProfile: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        AnalyticsViewModel(
            transactionDao = app.database.transactionDao(),
            categoryDao = app.database.categoryDao(),
            budgetDao = app.database.budgetDao()
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val displayName = remember { Prefs.getDisplayName(context) }
    val photoPath = remember { Prefs.getProfilePhotoPath(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { GreetingTitle(displayName) },
                actions = { ProfileAvatarButton(photoPath = photoPath, onClick = onOpenProfile) }
            )
        },
        bottomBar = { PillarBottomBar(current = Pillar.ANALYTICS, onSelect = onSelectPillar) }
    ) { padding ->
        if (!uiState.hasData) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing to analyze yet - once you have some transactions captured, " +
                        "your spending breakdown and trends will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TimeRangeChipRow(
                        options = AnalyticsTimeRange.entries,
                        selected = uiState.selectedRange,
                        label = { it.label },
                        onSelect = { viewModel.selectRange(it) }
                    )
                }

                if (uiState.categoryBreakdown.isNotEmpty()) {
                    item { SectionLabel("Spending overview") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                val total = uiState.categoryBreakdown.sumOf { it.amount }
                                val colors = chartColors()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    DonutChart(
                                        slices = uiState.categoryBreakdown.mapIndexed { index, slice ->
                                            DonutSlice(slice.categoryName, slice.amount, colors[index % colors.size])
                                        },
                                        modifier = Modifier.size(120.dp)
                                    ) {
                                        Text("₹${"%.0f".format(total)}", style = MaterialTheme.typography.titleMedium)
                                    }
                                    Spacer(Modifier.width(20.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        uiState.categoryBreakdown.forEachIndexed { index, slice ->
                                            ChartLegendRow(
                                                color = colors[index % colors.size],
                                                label = slice.categoryName,
                                                valueText = "₹${"%.0f".format(slice.amount)}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { SectionLabel("Income vs expenses") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                    ) {
                        BarChart(
                            labels = uiState.monthLabels,
                            series = listOf("Income" to uiState.monthlyIncome, "Expenses" to uiState.monthlyExpenses),
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatTile(
                            icon = Icons.Filled.TrendingUp,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            label = "Savings rate",
                            value = uiState.savingsRatePercent?.let { "${"%.0f".format(it)}%" } ?: "-",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            icon = Icons.Filled.TrendingDown,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            label = "Avg. daily spend",
                            value = "₹${"%.0f".format(uiState.avgDailySpend)}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { SectionLabel("Financial health score") }
                item { FinancialHealthScoreCard(uiState.healthScore) }

                if (uiState.topMerchants.isNotEmpty()) {
                    item { SectionLabel("Top merchants") }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Column {
                                uiState.topMerchants.forEach { (merchant, amount) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            merchant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text("₹${"%.2f".format(amount)}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Deterministic score (see FinancialHealthScore's kdoc) rendered as a ring + one-line read-out,
 * matching the reference mockups' card - "great/good/needs attention" wording is a plain
 * threshold on the same real score, not a separate judgement. */
@Composable
private fun FinancialHealthScoreCard(score: Int) {
    val ringColor = when {
        score >= 75 -> MaterialTheme.colorScheme.primary
        score >= 50 -> Warning
        else -> WarningStrong
    }
    val message = when {
        score >= 75 -> "You're doing great! Keep maintaining your habits."
        score >= 50 -> "You're doing okay - a few tweaks could help."
        else -> "Spending is outpacing income or budgets this month."
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = score / 100f,
                modifier = Modifier.size(72.dp),
                progressColor = ringColor
            ) {
                Text("$score", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(16.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

/** Cycles through the theme's own accent roles rather than introducing new ad hoc hex colors -
 * same discipline CategoryVisuals.colorPairFor already applies elsewhere in this app. */
@Composable
private fun chartColors(): List<Color> = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
    MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
)
