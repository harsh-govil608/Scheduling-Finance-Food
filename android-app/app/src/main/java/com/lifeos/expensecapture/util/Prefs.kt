package com.lifeos.expensecapture.util

import android.content.Context

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
}
