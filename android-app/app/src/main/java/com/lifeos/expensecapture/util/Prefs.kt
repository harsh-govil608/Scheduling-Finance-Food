package com.lifeos.expensecapture.util

import android.content.Context
import com.lifeos.expensecapture.ui.home.HomeSection
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Small local (non-Room) settings that need synchronous access from places that shouldn't
 * depend on a coroutine (e.g. checked once per incoming SMS in ParseIncomingSmsWorker).
 * Account & Profile Management PRD, Phase 3 Doc 44's "preference center" scope.
 */
object Prefs {
    private const val FILE = "app_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_CAPTURE_PAUSED = "capture_paused"
    private const val KEY_PROFILE_PHOTO_PATH = "profile_photo_path"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_AI_QUOTA_MONTH = "ai_quota_month"
    private const val KEY_AI_QUOTA_COUNT = "ai_quota_count"
    private const val KEY_AI_LANGUAGE = "ai_language"
    private const val KEY_HOME_SECTION_ORDER = "home_section_order"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

    /** Free tier's monthly cap on FinanceQaEngine questions (the "ask AI about my finances" chat
     * feature specifically) - see FinanceQaEngine.answer's own kdoc for exactly what this does
     * and doesn't gate. A product decision, not a technical one - change freely once real pricing
     * is finalized. */
    const val FREE_AI_QUESTIONS_PER_MONTH = 20

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** Real bug fix (2026-08, user report - the "allow notifications" onboarding step, and really
     * the whole onboarding flow, was showing on every single app open): PilotApp's NavHost always
     * started at the "permission" route with no way to know a returning user had already been
     * through it, since nothing before this persisted that fact anywhere - each step either
     * re-checked live OS permission state (fine for skipping SMS/notification asks specifically)
     * or, once past those, just ran the whole flow again including the final summary screen. This
     * flag is the actual "done" signal: set once, checked once at startup to pick the real start
     * destination, so onboarding runs exactly once per install like the user expects. */
    fun isOnboardingComplete(context: Context): Boolean = prefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun getDisplayName(context: Context): String = prefs(context).getString(KEY_DISPLAY_NAME, "") ?: ""

    fun setDisplayName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_DISPLAY_NAME, name).apply()
    }

    /** Local file path (app-internal storage, not the picker's own content:// Uri - that grant
     * isn't guaranteed to survive a reboot) to the copy made in ProfileViewModel.setProfilePhoto. */
    fun getProfilePhotoPath(context: Context): String? = prefs(context).getString(KEY_PROFILE_PHOTO_PATH, null)

    fun setProfilePhotoPath(context: Context, path: String?) {
        prefs(context).edit().putString(KEY_PROFILE_PHOTO_PATH, path).apply()
    }

    fun isCapturePaused(context: Context): Boolean = prefs(context).getBoolean(KEY_CAPTURE_PAUSED, false)

    fun setCapturePaused(context: Context, paused: Boolean) {
        prefs(context).edit().putBoolean(KEY_CAPTURE_PAUSED, paused).apply()
    }

    /** Locally cached entitlement - see BillingRepository's kdoc for why Play itself, not this
     * flag, is the actual source of truth (restorePurchases() re-syncs this on every app start). */
    fun isPremium(context: Context): Boolean = prefs(context).getBoolean(KEY_IS_PREMIUM, false)

    fun setPremium(context: Context, premium: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /** Free-tier monthly quota for FinanceQaEngine questions specifically - see
     * FREE_AI_QUESTIONS_PER_MONTH's kdoc for scope. Resets automatically the first time it's
     * checked in a new calendar month, rather than needing a scheduled job to reset it. */
    fun aiQuestionsUsedThisMonth(context: Context): Int {
        val p = prefs(context)
        val currentMonth = LocalDate.now().format(monthFormatter)
        return if (p.getString(KEY_AI_QUOTA_MONTH, null) == currentMonth) p.getInt(KEY_AI_QUOTA_COUNT, 0) else 0
    }

    fun recordAiQuestionUsed(context: Context) {
        val p = prefs(context)
        val currentMonth = LocalDate.now().format(monthFormatter)
        val newCount = aiQuestionsUsedThisMonth(context) + 1
        p.edit().putString(KEY_AI_QUOTA_MONTH, currentMonth).putInt(KEY_AI_QUOTA_COUNT, newCount).apply()
    }

    /** Local language support in AI chat (2026-08, real user request). "auto" (default) means
     * FinanceQaEngine detects and mirrors whatever language the question itself was written in -
     * see its SYSTEM_PROMPT for the exact instruction. Any other value forces every answer into
     * that language regardless of the question's language. */
    fun getAiLanguage(context: Context): String = prefs(context).getString(KEY_AI_LANGUAGE, "auto") ?: "auto"

    fun setAiLanguage(context: Context, language: String) {
        prefs(context).edit().putString(KEY_AI_LANGUAGE, language).apply()
    }

    /** Customizable Dashboard (2026-08, real user request). Order AND visibility are one
     * setting - a section not present in the stored list is hidden, not just unordered. Defaults
     * to every section in HomeScreen's original fixed order. Stored as a comma-joined string of
     * enum names; an unrecognized name (e.g. from a future version removing an enum entry) is
     * silently dropped rather than crashing. */
    fun getHomeSectionOrder(context: Context): List<HomeSection> {
        val stored = prefs(context).getString(KEY_HOME_SECTION_ORDER, null) ?: return HomeSection.entries.toList()
        return stored.split(",").mapNotNull { name -> HomeSection.entries.firstOrNull { it.name == name } }
    }

    fun setHomeSectionOrder(context: Context, order: List<HomeSection>) {
        prefs(context).edit().putString(KEY_HOME_SECTION_ORDER, order.joinToString(",") { it.name }).apply()
    }
}
