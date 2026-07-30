package com.lifeos.expensecapture.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design refresh (2026-07-31), replacing the earlier light/dark-adaptive "premium minimal" pass.
 * Sourced directly from reference mockups the founder supplied in the repo's top-level `ui/`
 * folder (dashboard-home, analytics-screen, add-expense, budget-overview, transaction-detail,
 * finance-dashboard, profile-settings) - those mockups already use ₹ and "Sohom" (the pilot
 * tester), i.e. they were built as a target for this exact app, not a generic inspiration board.
 * Always-dark rather than light/dark-adaptive: every reference screen is dark, and the founder's
 * ask was for the app to look like them, not for a light variant nobody designed. See Theme.kt.
 */

// ---------- Dark (the only scheme in active use - see Theme.kt) ----------
val PrimaryDark = Color(0xFF2EDDA6) // mint-green accent - buttons, positive amounts, selected nav/tab, progress fill
val OnPrimaryDark = Color(0xFF04241A) // text/icons drawn on a solid mint fill (FAB, primary button)
val PrimaryContainerDark = Color(0xFF163229) // dark mint-tinted container - selected chip bg, "Active" plan badge
val OnPrimaryContainerDark = Color(0xFF7DF2CB)
// Brightened from an earlier 0xFFB8C4BE (found by actually running the app: icon badges tinted
// with this color, e.g. Subscriptions/Bills on Home's Explore list, read as nearly invisible
// against SecondaryContainerDark at real device brightness/compression, unlike the more saturated
// primary/tertiary hues used everywhere else).
val SecondaryDark = Color(0xFFD6E3DC)
val OnSecondaryDark = Color(0xFF16332B)
val SecondaryContainerDark = Color(0xFF1E2624)
val OnSecondaryContainerDark = Color(0xFFD3DAD7)
val TertiaryDark = Color(0xFF8AD1E0) // soft cyan-blue, second accent for variety (Investments, some icon badges)
val OnTertiaryDark = Color(0xFF06303A)
val TertiaryContainerDark = Color(0xFF163A42)
val OnTertiaryContainerDark = Color(0xFFBEE9F2)
val ErrorDark = Color(0xFFFF6B6E) // delete actions, negative/bad deltas
val OnErrorDark = Color(0xFF3A0A0A)
val ErrorContainerDark = Color(0xFF3A1113)
val OnErrorContainerDark = Color(0xFFFFDAD6)
val BackgroundDark = Color(0xFF0B0F0E) // near-black with a faint green tint, not pure black
val OnBackgroundDark = Color(0xFFF3F5F4)
val SurfaceDark = Color(0xFF0B0F0E)
val OnSurfaceDark = Color(0xFFF3F5F4)
val SurfaceVariantDark = Color(0xFF1B2321) // chip backgrounds, dividers, unselected-track fills
val OnSurfaceVariantDark = Color(0xFF93A19B) // secondary/caption text - the muted gray-green throughout every reference screen
val OutlineDark = Color(0xFF3A423F)

/**
 * Not part of Material3's ColorScheme role set (there is no "warning" role), so these are plain
 * top-level colors referenced directly rather than through MaterialTheme.colorScheme. Used
 * anywhere the app needs to flag something worth noticing without implying a real error - an
 * over-budget category, a bill due soon, a subscription renewal. Real errors (a permission
 * getting revoked and breaking capture) still use the ColorScheme's actual `error` role.
 */
val WarningContainerDark = Color(0xFF3A2E10)
val OnWarningContainerDark = Color(0xFFF5C56B)

/** Foreground/accent-strength amber, for things like a progress-bar stroke rather than a card
 * background. Two tiers so "approaching a limit" and "over it" stay legible as different
 * urgency, without either one escalating all the way to alarm-red `error` - matches the amber
 * "Entertainment" bar in the budget-overview reference vs. the red-bordered Delete button in
 * transaction-detail. */
val Warning = Color(0xFFF0A93E)
val WarningStrong = Color(0xFFF2843B)

/** Card-background color, distinct from `colorScheme.surface`/`background` (SurfaceDark and
 * BackgroundDark are deliberately the same near-black hex - see above) - every panel in the
 * reference screens (This Week's Flow, Recent Active Flow, Category Budgets, Preferences rows)
 * sits one step lighter than the page behind it. */
val CardSurfaceDark = Color(0xFF151C1A)

/** A second, slightly lighter card tone for a panel nested inside another card's padding (e.g. a
 * stat tile within the finance-dashboard reference's 2x2 Income/Expenses/Savings/Investments
 * grid), so nested panels stay visually distinct from their parent without needing a border. */
val CardSurfaceRaisedDark = Color(0xFF1B2422)
