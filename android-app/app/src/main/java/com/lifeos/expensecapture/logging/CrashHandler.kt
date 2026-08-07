package com.lifeos.expensecapture.logging

import android.content.Context
import android.util.Log
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import java.io.File

/**
 * Pre-beta hardening (Priority 2): a genuinely fatal (uncaught) crash can't safely rely on an
 * async Room write completing before the process dies - the JVM may terminate before a suspend
 * DB insert on a background dispatcher gets scheduled at all. This writes synchronously to a
 * small local file instead (the standard pattern for a last-chance crash handler), then the next
 * app start (see `adoptPendingCrashIfAny`, called from App.onCreate) moves it into the same
 * crash_logs table AppLogger's handled-exception path uses, so both kinds end up in one place -
 * the Diagnostics screen doesn't need to know the difference.
 */
object CrashHandler {
    private const val CRASH_FILE_NAME = "pending_crash.log"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeSync(appContext, thread, throwable)
            } catch (writeError: Exception) {
                // A failure here must never prevent the real crash from reaching the platform's
                // own handler below - this is a best-effort extra, not the primary crash path.
                Log.e("LifeOS:CrashHandler", "failed to write crash file", writeError)
            }
            // Always defer to the previous (system) handler - this app must keep crashing exactly
            // as it did before, just with a local record left behind first.
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    // Each crash is written as a "---\n"-prefixed block of single-line fields, with `stack=`
    // always last, since a stack trace is the one field that's genuinely multi-line - see
    // adoptPendingCrashIfAny for why that ordering matters for parsing it back out correctly.
    private const val BLOCK_DELIMITER = "---\n"
    private const val STACK_FIELD_PREFIX = "stack="

    private fun writeSync(context: Context, thread: Thread, throwable: Throwable) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        // Bug fix (found via a real Diagnostics audit, 2026-08): throwable.message is very
        // commonly null (any exception thrown with no message string, e.g. bare
        // NullPointerException()) - a Kotlin string template renders that as the literal text
        // "null", which then got persisted and displayed on the Diagnostics screen as if it
        // were real message content instead of "no message provided". Omitting the field
        // entirely when there's no message keeps adoptPendingCrashIfAny's field lookup naturally
        // null instead of the string "null".
        val messageLine = throwable.message?.let { "message=$it\n" } ?: ""
        file.appendText(
            BLOCK_DELIMITER +
                "timestamp=${System.currentTimeMillis()}\n" +
                "thread=${thread.name}\n" +
                "type=${throwable::class.java.name}\n" +
                messageLine +
                "version=${AppLogger.appVersionName(context)}\n" +
                "$STACK_FIELD_PREFIX${Log.getStackTraceString(throwable)}\n"
        )
    }

    /** Called once from App.onCreate - if the previous process run crashed, its file-based record
     * gets folded into the normal Room-backed crash log and the temp file is removed. Safe to
     * call on every launch: a no-op when there's nothing pending. */
    suspend fun adoptPendingCrashIfAny(context: Context) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        if (!file.exists()) return

        try {
            val blocks = file.readText().split(BLOCK_DELIMITER).filter { it.isNotBlank() }
            val db = AppDatabase.getInstance(context)
            for (block in blocks) {
                // The stack trace field is multi-line, so it must be sliced out as everything
                // after "stack=" rather than treated as one more single-line field like the rest.
                val stackIndex = block.indexOf(STACK_FIELD_PREFIX)
                val headerLines = if (stackIndex >= 0) block.substring(0, stackIndex) else block
                val stackTrace = if (stackIndex >= 0) block.substring(stackIndex + STACK_FIELD_PREFIX.length).trim() else ""

                val fields = headerLines.lines().filter { it.isNotBlank() }.associate { line ->
                    val separatorIndex = line.indexOf('=')
                    if (separatorIndex < 0) "" to "" else line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
                }

                db.crashLogDao().insert(
                    CrashLogEntity(
                        timestamp = fields["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                        fatal = true,
                        threadName = fields["thread"] ?: "unknown",
                        exceptionType = fields["type"] ?: "unknown",
                        message = fields["message"],
                        stackTrace = stackTrace,
                        appVersionName = fields["version"] ?: "unknown",
                        source = "uncaught"
                    )
                )
            }
        } finally {
            file.delete()
        }
    }
}
