package com.lifeos.expensecapture.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.ui.theme.AmountHero
import com.lifeos.expensecapture.ui.theme.CardSurfaceDark
import com.lifeos.expensecapture.ui.theme.CardSurfaceLight

/**
 * Shared "premium minimal" components (Design System refresh, 2026-07-26): mostly-neutral
 * surfaces, one accent color per element rather than full-bleed colored card backgrounds, real
 * icons instead of text-only rows, and a hero number treatment for the one figure that matters
 * most on a screen. Introduced to replace duplicated plain-`Card`+`Text` blocks across Home,
 * Productivity Home, and Night Summary that all looked like stock Material3 defaults.
 */

/** See CardSurfaceLight/CardSurfaceDark kdoc - a subtle-but-theme-safe card background, picked
 * directly rather than derived from colorScheme.surface/surfaceVariant. */
@Composable
fun cardSurfaceColor(): Color = if (isSystemInDarkTheme()) CardSurfaceDark else CardSurfaceLight

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(size).clip(CircleShape).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** A plain Canvas line chart - no charting library needed for a 7-point trend line. */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    if (values.size < 2) return
    val max = values.max()
    val min = values.min()
    val range = max - min
    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = if (range < 0.0001f) size.height / 2f else size.height - ((value - min) / range) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            color = color,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** The one hero number on a landing screen - big, confident, with an optional trend line. */
@Composable
fun HeroMoneyCard(
    label: String,
    amount: Double,
    caption: String,
    trend: List<Float> = emptyList(),
    modifier: Modifier = Modifier,
    // Today's-spend line (found via a real user report, 2026-07): the hero card only ever showed
    // the month total, with no lower-effort way to see "how much today alone" without doing the
    // Ledger math yourself. Optional so other HeroMoneyCard call sites (Night Summary) are unaffected.
    secondaryLine: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("₹${"%.2f".format(amount)}", style = AmountHero)
            secondaryLine?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trend.size >= 2) {
                Spacer(Modifier.height(14.dp))
                Sparkline(
                    values = trend,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * The one deliberately eye-catching element on either landing screen: a narrative synthesis of
 * real numbers (SpendingInsightEngine / ProductivityInsightEngine), not a chat box, not a model
 * call - but the one place this app is allowed to look a little more alive than everything
 * around it, since this is the actual "does this feel intelligent" moment for the whole product.
 * A soft primary-tinted gradient + border instead of a solid fill, so it reads as a highlight
 * against the neutral cards around it without going full gradient-hero cliché.
 */
@Composable
fun AiInsightCard(title: String, body: String, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.05f))))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(28.dp))
            .padding(22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Filled.AutoAwesome,
                    tint = accent,
                    containerColor = accent.copy(alpha = 0.18f),
                    size = 40.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** A neutral card with a colored icon accent - replaces full-bleed `*Container` color fills for
 * alerts/banners, which read as more "premium minimal" than solid pastel blocks. */
@Composable
fun AccentInfoCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            IconBadge(icon = icon, tint = accentColor, containerColor = accentColor.copy(alpha = 0.14f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                body?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                content?.let {
                    Spacer(Modifier.height(8.dp))
                    it()
                }
            }
        }
    }
}

/** A tappable list row with an icon badge, title/subtitle, and a trailing chevron - replaces the
 * text-only entry-point cards that were duplicated across Home and Productivity Home. */
@Composable
fun EntryRow(
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, tint = iconTint, containerColor = iconContainerColor)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
