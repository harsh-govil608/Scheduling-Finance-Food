package com.lifeos.expensecapture.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Design System PRD (Phase 7) Doc 08 (Cards) describes card anatomy and density rules but no
 * concrete corner-radius/elevation values to copy. `medium` (16.dp) is what Compose's default
 * Card uses unless overridden per call, so wiring this into the theme is a one-file change that
 * softens every existing Card() across the app - see docs/coders-documentation for the design
 * pass this came from.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
