package com.lifeos.expensecapture.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSlice(val label: String, val value: Double, val color: Color)

/**
 * Category-breakdown donut - same plain-Canvas approach as HeroMoneyCard's Sparkline/
 * ProgressRing elsewhere in this app (see their kdocs): no charting library needed for a handful
 * of arcs. Each slice's sweep angle is its real share of the total, not a rounded/fabricated
 * percentage - an empty/zero-total slice list just draws nothing rather than a fake full ring.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val total = slices.sumOf { it.value }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (total <= 0.0) return@Canvas
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total * 360.0).toFloat()
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtLeast(1.5f),
                        useCenter = false,
                        style = stroke
                    )
                }
                startAngle += sweep
            }
        }
        centerContent()
    }
}

/** A legend row - color dot, label, and the real value/percentage - used beside DonutChart so
 * the chart is never the only place the underlying numbers appear. */
@Composable
fun ChartLegendRow(color: Color, label: String, valueText: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(10.dp)) { drawCircle(color = color) }
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(valueText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * A multi-series line trend chart (reference mockups' "analysis curve") - up to a few series
 * (e.g. income vs expenses) plotted on one shared scale, with month labels below and a color
 * legend. Same Canvas-line-path approach as Sparkline, generalized to more than one series and
 * given real axis labels instead of being purely decorative.
 */
@Composable
fun TrendChart(
    labels: List<String>,
    series: List<Pair<String, List<Float>>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val allValues = series.flatMap { it.second }
            val maxValue = (allValues.maxOrNull() ?: 0f).coerceAtLeast(1f)
            val pointCount = labels.size
            if (pointCount < 2) return@Canvas
            val stepX = size.width / (pointCount - 1)

            series.forEachIndexed { seriesIndex, (_, values) ->
                val path = Path()
                values.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value / maxValue) * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path,
                    color = colors.getOrElse(seriesIndex) { Color.Gray },
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            series.forEachIndexed { index, (name, _) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = colors.getOrElse(index) { Color.Gray })
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
