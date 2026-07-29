package com.lifeos.expensecapture.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifeos.expensecapture.data.db.AppDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup & Restore (built via a real user request, 2026-07): "if my phone breaks tomorrow, I
 * lose everything I paid for" was the single blocker to this app being worth paying for at all.
 * Deliberately not a cloud sync feature - no backend exists in this app and none is being added
 * here. Instead: copy the actual Room database file (byte-for-byte, not a hand-serialized re-
 * encoding of every entity) and hand it to Android's own share sheet, same pattern already used
 * for CSV/diagnostics export - the user decides where it lives (Google Drive, email to
 * themselves, a USB drive), which is the only thing that actually survives a lost or broken
 * phone. This app never touches a server either way.
 */
object BackupExporter {

    fun exportDatabase(context: Context, db: AppDatabase): Uri {
        // Must run before copying - see AppDatabase.checkpoint()'s kdoc. Without this, recent
        // writes sitting in the WAL sidecar file could be silently missing from the backup.
        db.checkpoint()

        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val dir = File(context.cacheDir, "exports/backup").apply { mkdirs() }
        // Same reasoning as CsvExporter/DiagnosticsExporter: single-use share artifact, not a
        // history worth keeping - and its own subdirectory so it can never race either of them.
        dir.listFiles()?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
        val backupFile = File(dir, "lifeos_backup_$timestamp.db")
        dbFile.copyTo(backupFile, overwrite = true)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
    }
}
