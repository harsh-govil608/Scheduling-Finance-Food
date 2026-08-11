package com.lifeos.expensecapture.util

import android.content.Context
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

    /** Free tier's monthly cap on FinanceQaEngine questions (the "ask AI about my finances" chat
     * feature specifically) - see FinanceQaEngine.answer's own kdoc for exactly what this does
     * and doesn't gate. A product decision, not a technical one - change freely once real pricing
     * is finalized. */
    const val FREE_AI_QUESTIONS_PER_MONTH = 20

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

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
}
