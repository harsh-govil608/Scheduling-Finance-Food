package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pre-beta hardening (Priority 2): this app had zero crash visibility before this - every
 * verification this whole project has ever done depended on someone physically holding the
 * device and reading logcat live. This is the local, privacy-preserving alternative: crashes and
 * handled exceptions are recorded on-device only, in the same "100% on-device, no backend"
 * architecture as everything else here - nothing is sent anywhere unless the user explicitly
 * shares a Diagnostics export themselves (see DiagnosticsScreen), the same share-sheet pattern
 * CsvExporter already uses for transaction export.
 */
@Entity(tableName = "crash_logs")
data class CrashLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    /** true for an uncaught exception that actually terminated the app; false for a handled
     * exception logged via AppLogger.e() while the app kept running. */
    val fatal: Boolean,
    val threadName: String,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String,
    val appVersionName: String,
    /** A short breadcrumb naming where this was logged from, e.g. "NotificationCheckWorker.checkBills" */
    val source: String?
)
