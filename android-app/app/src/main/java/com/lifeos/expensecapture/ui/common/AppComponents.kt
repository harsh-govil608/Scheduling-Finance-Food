package com.lifeos.expensecapture.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.ui.theme.AmountBody
import com.lifeos.expensecapture.ui.theme.AmountHero
import com.lifeos.expensecapture.ui.theme.CardSurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared components for the dark mint-green design (refresh 2026-07-31, see Color.kt's kdoc for
 * the reference source): dark cards one step lighter than the page, a single mint accent per
 * element rather than full-bleed color fills, real icons instead of text-only rows, and a hero
 * number treatment for the one figure that matters most on a screen. Originally introduced
 * 2026-07-26 to replace duplicated plain-`Card`+`Text` blocks; carried forward into the dark
 * refresh since the same component shapes (icon-badge rows, hero cards, accent-bordered
 * highlight cards) are exactly what the reference mockups use too.
 */

/** See CardSurfaceDark's kdoc - a card background one step lighter than the page background,
 * picked directly rather than derived from colorScheme.surface/surfaceVariant (which are the same
 * hex as background - a card would blend invisibly into the page otherwise). */
@Composable
fun cardSurfaceColor(): Color = CardSurfaceDark

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

/**
 * A plain Canvas line chart - no charting library needed for a 7-point trend line.
 *
 * Threshold marker added (found via a real user report, 2026-07 - "adding a threshold mark"):
 * previously just a bare line with no reference point, so there was no visual answer to "is this
 * pace okay?" without reading the numbers elsewhere. [threshold] draws a dashed reference line
 * (typically the daily budget pace) at its own position on the same scale as the data - if it
 * falls outside the data's own min/max range, the range is widened to include it so the line is
 * always visible rather than clipped off-canvas.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    threshold: Float? = null,
    thresholdColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (values.size < 2) return
    val max = maxOf(values.max(), threshold ?: values.max())
    val min = minOf(values.min(), threshold ?: values.min())
    val range = max - min
    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1)
        fun yFor(value: Float) =
            if (range < 0.0001f) size.height / 2f else size.height - ((value - min) / range) * size.height

        threshold?.let {
            val y = yFor(it)
            drawLine(
                color = thresholdColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
        }

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = yFor(value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            color = color,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * A circular "donut" progress indicator - the ring treatment the reference mockups use in place
 * of a plain linear bar wherever a single percentage is the headline figure (Analytics' month
 * spent ring, Budget Overview's "64% utilized" ring, Home's per-goal rings). Deliberately a plain
 * Canvas arc, same reasoning as Sparkline above - no charting library needed for one ring.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = stroke
            )
        }
        content()
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
    // Today's-spend stat (found via a real user report, 2026-07): the hero card only ever showed
    // the month total, with no lower-effort way to see "how much today alone" without doing the
    // Ledger math yourself. Rendered beside the main label/amount, not underneath it (a second
    // real user report, 2026-07 - "spent this month ke baaju me spent today aana chahiye, side
    // me hona chahiye") so it reads as a companion stat rather than a caption. Optional so other
    // HeroMoneyCard call sites (Night Summary) are unaffected.
    secondaryLabel: String? = null,
    secondaryAmount: Double? = null,
    // Threshold mark for the trend line (found via a real user report, 2026-07) - see
    // Sparkline's kdoc. Optional, same reasoning as secondaryLabel/secondaryAmount.
    trendThreshold: Float? = null,
    // Mint border glow (2026-07-31 design refresh, see Color.kt's kdoc) - the reference mockups'
    // "Monthly Spending"/"This Week's Flow" hero cards have a visible accent border, not a flat
    // edgeless panel. Defaults on since Home's own spend card is the headline figure on the
    // screen; Night Summary's reuse of this same component opts out (today's total there is real
    // but less "the one number that matters" than Home's monthly total is).
    accentBorder: Boolean = true
) {
    val borderColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (accentBorder) {
                    it.border(1.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                } else {
                    it
                }
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // Padding trimmed from 24dp/14dp/10dp (found via a real user report, 2026-07 - "give
        // more space to other cards or buttons"): this card could stack above the Morning
        // Briefing, AI Insight, and Needs Attention cards on Home, pushing the actual navigation
        // entries (Ledger, Budgets, etc.) further down before any scrolling. A more compact hero
        // card leaves more of the screen for everything below it without losing legibility -
        // the amount itself is still the largest text on the page.
        Column(Modifier.padding(20.dp)) {
            // Splitting the hero amount itself into a half-width column (an earlier version of
            // this layout) wrapped multi-digit totals onto two lines at AmountHero's 42sp - found
            // by actually running the app against real device data (₹61429.24 wrapped to
            // "₹61429.2" / "4"). The secondary stat sits beside the *label* line instead, so the
            // hero amount keeps the full card width it always had.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (secondaryLabel != null && secondaryAmount != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            secondaryLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("₹${"%.2f".format(secondaryAmount)}", style = AmountBody)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("₹${"%.2f".format(amount)}", style = AmountHero)
            if (trend.size >= 2) {
                Spacer(Modifier.height(10.dp))
                Sparkline(
                    values = trend,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    threshold = trendThreshold
                )
            }
            Spacer(Modifier.height(8.dp))
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

/**
 * A category-icon-badged transaction row (moved here from LedgerScreen 2026-07-31 so Home's new
 * Recent Transactions preview - reference mockups' "Recent Active Flow"/"Recent Transactions" -
 * and Ledger's full list render an identical row instead of two hand-copied versions). Credit
 * amounts render in the primary mint (a real "+₹X" gain, matching the reference's green
 * "+₹1,20,000 Salary Credit" row) instead of the default debit text color.
 */
@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    categoryName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val (tint, container) = CategoryVisuals.colorPairFor(categoryName)
        IconBadge(
            icon = CategoryVisuals.iconFor(categoryName),
            tint = tint,
            containerColor = container,
            size = 40.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.merchantRaw, style = MaterialTheme.typography.bodyLarge)
            Text(
                "$categoryName · ${dateFormat.format(Date(transaction.date))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val isCredit = transaction.direction == TransactionDirection.CREDIT
        val sign = if (isCredit) "+" else "-"
        Text(
            "$sign₹${"%.2f".format(transaction.amount)}",
            style = AmountBody,
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * A small stat card (reference mockups' Income/Expenses/Savings/Investments 2x2 grid on
 * finance-dashboard) - an icon badge, an optional delta chip, a label, and a value. `deltaText` is
 * left null wherever there's no genuine prior-period figure to compare against (see HomeViewModel)
 * rather than showing a fabricated percentage.
 */
@Composable
fun StatTile(
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    deltaText: String? = null,
    deltaPositive: Boolean = true
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = icon, tint = iconTint, containerColor = iconContainerColor, size = 32.dp)
                deltaText?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (deltaPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = AmountBody)
        }
    }
}

/**
 * A bordered summary header card - Bills'/Subscriptions' equivalent of Budget's ring summary and
 * Home's hero card, so every list screen opens on one real aggregate figure in the same "glowing
 * bordered card" treatment the reference mockups use (e.g. finance-dashboard's "Monthly
 * Spending"), instead of dropping straight into a plain list. Deliberately generic (icon/label/
 * value/caption) since Bills and Subscriptions would otherwise duplicate near-identical layout.
 */
@Composable
fun SummaryStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Row(Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = accent, containerColor = accent.copy(alpha = 0.16f), size = 48.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text(value, style = AmountHero)
                caption?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** A colored pill for a short status word (reference mockups' "ACTIVE" plan badge) - replaces a
 * plain sentence-style status line wherever the status is really a single state word (due today,
 * overdue, tracked, possibly lapsed) rather than prose. */
@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * The greeting header (reference mockups' "Good Morning, Sohom" / "Hello Sohom") shared between
 * Finance's HomeScreen and Home pillar's ProductivityHomeScreen, so the two pillar landing
 * screens read as one app - see ProductivityHomeScreen's kdoc. Time-of-day-aware, falls back to
 * "there" rather than an empty string when no display name has been set yet.
 */
@Composable
fun GreetingTitle(displayName: String) {
    Column {
        val hour = remember { java.time.LocalTime.now().hour }
        val timeOfDay = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        val name = displayName.ifBlank { "there" }
        Text("$timeOfDay, $name", style = MaterialTheme.typography.titleMedium)
        val today = remember {
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        }
        Text(today, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The profile entry point beside GreetingTitle - the real photo from Profile when one's set,
 * the default icon otherwise. See ProfileScreen's photo picker. */
@Composable
fun ProfileAvatarButton(photoPath: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.IconButton(onClick = onClick, modifier = modifier) {
        val photoBitmap = remember(photoPath) {
            photoPath?.let { android.graphics.BitmapFactory.decodeFile(it)?.asImageBitmap() }
        }
        if (photoBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = photoBitmap,
                contentDescription = "Profile & settings",
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile & settings")
        }
    }
}

/**
 * A tappable icon+label tile (reference mockups' "Quick Actions" 2x2 grid) - a lighter-weight
 * cousin of EntryRow with no subtitle/chevron, for a shortcut grid rather than a list.
 */
@Composable
fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
    ) {
        Column(Modifier.padding(16.dp)) {
            IconBadge(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                size = 36.dp
            )
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
