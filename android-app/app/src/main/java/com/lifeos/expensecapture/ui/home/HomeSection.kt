package com.lifeos.expensecapture.ui.home

/**
 * Customizable Dashboard (2026-08, real user request) - the 6 genuinely optional/reorderable
 * Home sections. Conditional-on-data system banners (update available, morning briefing, offline
 * note, SMS-revoked warning) and the single "needs attention" slot are NOT part of this enum -
 * they must always be able to appear regardless of layout preference, so they stay in their
 * fixed positions in HomeScreen rather than being user-togglable. See
 * Prefs.getHomeSectionOrder's kdoc for how this is persisted (order AND visibility are one
 * setting - omission from the stored list means hidden, not just unordered).
 */
enum class HomeSection(val displayName: String) {
    HERO("Spent this month"),
    STATS("Income / Expenses / Savings / Investments"),
    INSIGHT("What changed (spending insight)"),
    QUICK_ACTIONS("Quick Actions"),
    RECENT("Recent Transactions"),
    EXPLORE("Explore (Ledger, Budgets, Subscriptions, Bills, Needs Review)")
}
