package com.lifeos.expensecapture.data.seed

/**
 * Predefined categorization rules (2026-08, real user request: "Predefined Categorization rules
 * Coming soon" - "there can be multiple UPI spending to multiple merchants, we can't categorize
 * them all"). Seeded once via App.seedDefaultMerchantRulesOnce into merchant_rules with
 * isSeededDefault = true - see MerchantRuleEntity.isSeededDefault's kdoc for why these are kept
 * lower-priority than any real user rule.
 *
 * Deliberately includes both a generic pattern and a more specific one for the same merchant
 * family where it matters (e.g. "amazon" -> Shopping, "amazon prime" -> Subscriptions) -
 * CategorizationEngine's longer-pattern-wins tiebreak (within the same isSeededDefault tier)
 * makes the more specific one correctly take priority for that one case, without needing every
 * "amazon"-prefixed pattern spelled out.
 *
 * Patterns are matched as a case-insensitive substring of the transaction's merchant text (see
 * CategorizationEngine.categorize) - lowercase here to match how they're compared.
 */
object DefaultMerchantRules {

    val patternsByCategory: List<Pair<String, String>> = listOf(
        // Food & Dining
        "swiggy" to "Food & Dining",
        "zomato" to "Food & Dining",
        "dominos" to "Food & Dining",
        "mcdonald" to "Food & Dining",
        "kfc" to "Food & Dining",
        "pizza hut" to "Food & Dining",
        "pizzahut" to "Food & Dining",
        "starbucks" to "Food & Dining",
        "cafe coffee day" to "Food & Dining",
        "barbeque nation" to "Food & Dining",
        "faasos" to "Food & Dining",

        // Groceries
        "bigbasket" to "Groceries",
        "blinkit" to "Groceries",
        "zepto" to "Groceries",
        "grofers" to "Groceries",
        "dmart" to "Groceries",
        "nature's basket" to "Groceries",
        "jiomart" to "Groceries",

        // Shopping (kept before the more specific "amazon prime"/subscription patterns below -
        // order in this list doesn't matter for matching, only pattern length does)
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "myntra" to "Shopping",
        "ajio" to "Shopping",
        "meesho" to "Shopping",
        "nykaa" to "Shopping",
        "snapdeal" to "Shopping",

        // Subscriptions - deliberately longer/more specific than the generic "amazon" pattern
        // above so it wins the tiebreak for an actual Amazon Prime charge.
        "netflix" to "Subscriptions",
        "spotify" to "Subscriptions",
        "hotstar" to "Subscriptions",
        "amazon prime" to "Subscriptions",
        "sonyliv" to "Subscriptions",
        "zee5" to "Subscriptions",
        "youtube premium" to "Subscriptions",
        "apple music" to "Subscriptions",

        // Transport
        "uber" to "Transport",
        "ola cabs" to "Transport",
        "olacabs" to "Transport",
        "rapido" to "Transport",
        "irctc" to "Transport",

        // Bills & Utilities
        "airtel" to "Bills & Utilities",
        "jio prepaid" to "Bills & Utilities",
        "jiofiber" to "Bills & Utilities",
        "vodafone" to "Bills & Utilities",
        "vi prepaid" to "Bills & Utilities",
        "bsnl" to "Bills & Utilities",
        "act fibernet" to "Bills & Utilities",
        "tikona" to "Bills & Utilities",

        // Travel
        "makemytrip" to "Travel",
        "goibibo" to "Travel",
        "yatra" to "Travel",
        "oyo" to "Travel",
        "airbnb" to "Travel",
        "indigo" to "Travel",
        "spicejet" to "Travel",

        // Health
        "apollo pharmacy" to "Health",
        "practo" to "Health",
        "pharmeasy" to "Health",
        "netmeds" to "Health",
        "1mg" to "Health",
        "medplus" to "Health"
    )
}
