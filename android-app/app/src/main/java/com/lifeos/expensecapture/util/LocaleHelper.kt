package com.lifeos.expensecapture.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Per-app language switching (2026-08, real user request: "if a user doesn't understand
 * English, he/she can select the language and get updates or notifications in his/her
 * language"). AppCompatDelegate.setApplicationLocales persists the choice itself (survives app
 * restart) and triggers the necessary Activity recreation to re-resolve every stringResource()
 * call against the new locale - this app doesn't need its own Context-wrapping or persistence
 * logic on top of it. Works on a plain ComponentActivity (see MainActivity's kdoc) as of
 * appcompat 1.6+, not just AppCompatActivity. */
object LocaleHelper {
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_HINDI = "hi"

    fun applyLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    /** Empty string means "follow system default" (never explicitly chosen yet). */
    fun currentLanguageTag(): String =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(",")
}
