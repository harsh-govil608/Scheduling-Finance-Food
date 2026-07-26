package com.lifeos.expensecapture.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design System PRD (Phase 7) Docs 02/04: "calm authority, quiet competence" as the visual
 * identity, with semantic colors that "encourage, never guilt" - a missed budget or an overdue
 * bill should never render with the same alarm-red urgency as a real system error. The docs
 * specify intent and roles, not literal hex values (there was nothing to copy - see
 * docs/coders-documentation for the design pass that produced these), so these are one concrete
 * realization of that intent: a deep teal-green primary (trust/money without corporate-blue
 * cliche), warm off-white surfaces instead of stark white, and a muted amber Warning role kept
 * deliberately separate from Material3's `error` role so "over budget" and "system broke" never
 * look the same.
 */

// ---------- Light ----------
val PrimaryLight = Color(0xFF2E6F58)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFB7ECDA)
val OnPrimaryContainerLight = Color(0xFF00201A)
val SecondaryLight = Color(0xFF4C6355)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCEE9DB)
val OnSecondaryContainerLight = Color(0xFF092017)
val TertiaryLight = Color(0xFF3D6373)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC0E8FB)
val OnTertiaryContainerLight = Color(0xFF001F29)
val ErrorLight = Color(0xFFA23B3B)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF6D8D4)
val OnErrorContainerLight = Color(0xFF410E0B)
val BackgroundLight = Color(0xFFFBFAF7)
val OnBackgroundLight = Color(0xFF1A1C1A)
val SurfaceLight = Color(0xFFFBFAF7)
val OnSurfaceLight = Color(0xFF1A1C1A)
val SurfaceVariantLight = Color(0xFFDDE5DE)
val OnSurfaceVariantLight = Color(0xFF414942)
val OutlineLight = Color(0xFF717971)

// ---------- Dark ----------
val PrimaryDark = Color(0xFF9BD3C0)
val OnPrimaryDark = Color(0xFF00382C)
val PrimaryContainerDark = Color(0xFF145142)
val OnPrimaryContainerDark = Color(0xFFB7ECDA)
val SecondaryDark = Color(0xFFB3CCC0)
val OnSecondaryDark = Color(0xFF1E352C)
val SecondaryContainerDark = Color(0xFF354B42)
val OnSecondaryContainerDark = Color(0xFFCEE9DB)
val TertiaryDark = Color(0xFFA4CCDD)
val OnTertiaryDark = Color(0xFF063542)
val TertiaryContainerDark = Color(0xFF244C5A)
val OnTertiaryContainerDark = Color(0xFFC0E8FB)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)
val BackgroundDark = Color(0xFF101413)
val OnBackgroundDark = Color(0xFFE2E3DE)
val SurfaceDark = Color(0xFF101413)
val OnSurfaceDark = Color(0xFFE2E3DE)
val SurfaceVariantDark = Color(0xFF414942)
val OnSurfaceVariantDark = Color(0xFFC1C9C1)
val OutlineDark = Color(0xFF8B938B)

/**
 * Not part of Material3's ColorScheme role set (there is no "warning" role), so these are plain
 * top-level colors referenced directly rather than through MaterialTheme.colorScheme. Used
 * anywhere the app needs to flag something worth noticing without implying a real error - an
 * over-budget category, a bill due soon, a subscription renewal. Real errors (a permission
 * getting revoked and breaking capture) still use the ColorScheme's actual `error` role.
 */
val WarningContainerLight = Color(0xFFFBE7B7)
val OnWarningContainerLight = Color(0xFF3F2E00)
val WarningContainerDark = Color(0xFF544000)
val OnWarningContainerDark = Color(0xFFFBE7B7)

/** Foreground/accent-strength amber, for things like a progress-bar stroke rather than a card
 * background. Two tiers so "approaching a limit" and "over it" stay legible as different
 * urgency, without either one escalating all the way to alarm-red `error`. */
val Warning = Color(0xFFB8860B)
val WarningStrong = Color(0xFFB35C00)

/**
 * Dedicated card-background colors (2026-07-26 redesign follow-up), used instead of
 * `colorScheme.surface`/`surfaceVariant` directly. `surface` alone wasn't safe because
 * SurfaceDark and BackgroundDark are the same hex value - a card would blend invisibly into the
 * page in dark mode. `surfaceVariant` fixed that but was too strong a jump from the light theme's
 * off-white background - it read as a highlighter color rather than a subtle card. These sit
 * deliberately between the two: barely-there in light mode (matching what the old
 * elevation-tinted plain Card looked like), genuinely visible in dark mode without being loud.
 */
val CardSurfaceLight = Color(0xFFF6F5F0)
val CardSurfaceDark = Color(0xFF1B211F)
