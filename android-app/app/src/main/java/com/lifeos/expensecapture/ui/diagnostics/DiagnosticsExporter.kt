package com.lifeos.expensecapture.ui.diagnostics

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifeos.expensecapture.data.db.entity.CrashLogEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Same local-file + FileProvider + share-sheet pattern as CsvExporter - the only way this data
 * ever leaves the device is the user explicitly choosing to share it themselves. */
object DiagnosticsExporter {

    fun export(context: Context, entries: List<CrashLogEntity>): Uri {
        // Own subdirectory (not shared with CsvExporter's "exports/transactions") so clearing
        // old diagnostics exports below can never race with a CSV export in flight.
        val dir = File(context.cacheDir, "exports/diagnostics").apply { mkdirs() }
        // Bug fix (found via a real user report, 2026-07 - "check storage/cache"): same
        // unbounded-cache-growth issue as CsvExporter - see its kdoc.
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "diagnostics_${System.currentTimeMillis()}.txt")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        file.bufferedWriter().use { writer ->
            entries.sortedByDescending { it.timestamp }.forEach { entry ->
                writer.write("=".repeat(60))
                writer.newLine()
                writer.write("${dateFormat.format(Date(entry.timestamp))} - ${if (entry.fatal) "FATAL" else "handled"}")
                writer.newLine()
                writer.write("app version: ${entry.appVersionName}")
                writer.newLine()
                writer.write("source: ${entry.source ?: "unknown"}")
                writer.newLine()
                writer.write("thread: ${entry.threadName}")
                writer.newLine()
                writer.write("${entry.exceptionType}: ${entry.message ?: ""}")
                writer.newLine()
                writer.write(entry.stackTrace)
                writer.newLine()
                writer.newLine()
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
