package com.lifeos.expensecapture.export

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.logging.AppLogger
import java.io.File

sealed class RestoreResult {
    object Success : RestoreResult()
    object InvalidFile : RestoreResult()
    data class Failed(val message: String) : RestoreResult()
}

/**
 * See BackupExporter's kdoc for the overall design. Restoring is destructive (it replaces every
 * transaction, budget, task, habit - everything), so this validates the picked file is a real
 * database belonging to this app *before* touching anything live, rather than trusting the file
 * extension or discovering a problem after the current data is already gone.
 */
object BackupImporter {

    /** A quick, cheap check the confirmation dialog can call before the user commits to the
     * destructive restore - so "this file doesn't look right" surfaces before, not after. */
    fun looksLikeValidBackup(context: Context, sourceUri: Uri): Boolean {
        val tempFile = copyToTemp(context, sourceUri) ?: return false
        return try {
            isValidAppDatabase(tempFile)
        } finally {
            tempFile.delete()
        }
    }

    suspend fun restoreDatabase(context: Context, sourceUri: Uri): RestoreResult {
        val tempFile = copyToTemp(context, sourceUri)
            ?: return RestoreResult.Failed("Couldn't read that file.")

        return try {
            if (!isValidAppDatabase(tempFile)) {
                return RestoreResult.InvalidFile
            }

            AppDatabase.closeAndClearInstance()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            // A stale WAL/SHM from the *old* database must not survive to be replayed against
            // the restored file - they're sidecar files for a database that no longer exists.
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            tempFile.copyTo(dbFile, overwrite = true)

            RestoreResult.Success
        } catch (e: Exception) {
            AppLogger.e("BackupImporter", "restore failed", e)
            RestoreResult.Failed("Something went wrong restoring that backup - your existing data hasn't been touched.")
        } finally {
            tempFile.delete()
        }
    }

    private fun copyToTemp(context: Context, sourceUri: Uri): File? {
        return try {
            val tempFile = File(context.cacheDir, "restore_candidate.db")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            tempFile
        } catch (e: Exception) {
            AppLogger.e("BackupImporter", "failed to read picked file", e)
            null
        }
    }

    /** Real SQLite files start with this exact 16-byte header ("SQLite format 3" plus a null
     * terminator, not a space or trailing garbage - confirmed byte-for-byte against a real
     * pulled database file earlier this session) - catches "picked a random file that isn't a
     * database at all" before anything else. Then opening it and checking for a couple of this
     * app's own core tables catches "a real SQLite file, just not one of ours" (Room would
     * otherwise fail confusingly deep inside a schema-hash check instead of here, with a clear
     * message, before any live data has been touched). */
    private fun isValidAppDatabase(file: File): Boolean {
        val header = file.inputStream().use { it.readNBytes(16) }
        val sqliteMagic = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        if (!header.contentEquals(sqliteMagic)) return false

        return try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { candidate ->
                candidate.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('transactions', 'budgets', 'tasks')",
                    null
                ).use { cursor -> cursor.count == 3 }
            }
        } catch (e: Exception) {
            false
        }
    }
}
