package com.lifeos.expensecapture.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * UI polish pass (found via a real user report, 2026-07 - "the UI is looking too basic"): the
 * Design System refresh (Color.kt/Type.kt/Shape.kt/AppComponents.kt) only ever reached Home and
 * Night Summary - every other screen listing transactions (Ledger, Search) was still a plain
 * Row + Text + HorizontalDivider list with no visual accent, which is a real, visible
 * inconsistency once you've seen the polished screens. This gives every transaction row a
 * category icon badge, the same IconBadge treatment Home's EntryRow already uses for navigation,
 * reusing only the app's existing three theme color-container pairs rather than introducing new
 * ad hoc hex colors this design system has deliberately avoided everywhere else.
 */
object CategoryVisuals {

    fun iconFor(categoryName: String): ImageVector = when (categoryName) {
        "Food & Dining" -> Icons.Filled.Restaurant
        "Groceries" -> Icons.Filled.ShoppingCart
        "Transport" -> Icons.Filled.DirectionsCar
        "Shopping" -> Icons.Filled.ShoppingBag
        "Bills & Utilities" -> Icons.AutoMirrored.Filled.ReceiptLong
        "Subscriptions" -> Icons.Filled.Autorenew
        "Health" -> Icons.Filled.LocalHospital
        "Entertainment" -> Icons.Filled.Attractions
        "Rent" -> Icons.Filled.Home
        "Transfers" -> Icons.Filled.SwapHoriz
        "ATM & Cash" -> Icons.Filled.LocalAtm
        "Salary/Income" -> Icons.Filled.AttachMoney
        "Travel" -> Icons.Filled.Flight
        "Loan & EMI" -> Icons.Filled.AccountBalance
        "Uncategorized" -> Icons.AutoMirrored.Filled.HelpOutline
        else -> Icons.Filled.Category // a user-created custom category
    }

    /** (tint, container) - cycles through the theme's three color-container pairs by name so
     * the same category always lands on the same color, without hand-picking a fourth hue this
     * design system doesn't otherwise use. */
    @Composable
    fun colorPairFor(categoryName: String): Pair<Color, Color> {
        val pairs = listOf(
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiaryContainer
        )
        val index = Math.floorMod(categoryName.hashCode(), pairs.size)
        return pairs[index]
    }
}
