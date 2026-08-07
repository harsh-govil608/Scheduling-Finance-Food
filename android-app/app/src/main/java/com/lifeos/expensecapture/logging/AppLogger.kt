package com.lifeos.expensecapture.logging

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Pre-beta hardening (Priority 2): one place for structured logging instead of ad-hoc/no logging
 * (there wasn't a single Log.* call anywhere in this codebase before this pass). `e()` both logs
 * to Logcat (for a connected debugger, same as before) and persists a record locally via
 * CrashLogDao, so a handled exception on a tester's phone is now something that can actually be
 * looked at later - the entire reason NotificationCheckWorker's checks needed try/catch wrapping
 * in the first place (see its kdoc) is that a caught exception with nowhere to go is barely
 * better than an uncaught one.
 */
object AppLogger {
    private const val TAG_PREFIX = "LifeOS"
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun d(tag: String, message: String) {
        Log.d("$TAG_PREFIX:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$TAG_PREFIX:$tag", message)
    }

    /** A handled exception - the app keeps running. Logged immediately; persisted to the local
     * crash_logs table best-effort (a failure to persist must never itself throw or block the
     * caller, so it's fire-and-forget on a supervisor scope). Also reported to Crashlytics
     * (2026-08 infra hardening) as a non-fatal - the local table is per-device and only ever seen
     * if someone opens Diagnostics on that exact phone; Crashlytics is what actually makes a
     * handled exception visible to the founder without needing physical access to the device it
     * happened on. try/catch around the Crashlytics call for the same reason as the DB write
     * below - a logging call must never itself become a new source of crashes. */
    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e("$TAG_PREFIX:$tag", message, throwable)
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (crashlyticsError: Exception) {
            Log.e("$TAG_PREFIX:AppLogger", "failed to report to Crashlytics", crashlyticsError)
        }
        val context = appContext ?: return
        logScope.launch {
            try {
                AppDatabase.getInstance(context).crashLogDao().insert(
                    CrashLogEntity(
                        fatal = false,
                        threadName = Thread.currentThread().name,
                        exceptionType = throwable::class.java.name,
                        message = message,
                        stackTrace = Log.getStackTraceString(throwable),
                        appVersionName = appVersionName(context),
                        source = tag
                    )
                )
            } catch (persistError: Exception) {
                // Logging must never crash the app it's trying to observe.
                Log.e("$TAG_PREFIX:AppLogger", "failed to persist a handled-exception log", persistError)
            }
        }
    }

    fun appVersionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}
