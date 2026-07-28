package com.lifeos.expensecapture.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Simplified, pilot-scale implementation of the Data Export & Portability PRD (Phase 3 Doc
 * 47): an instant local CSV export shared via Android's share sheet, rather than the full
 * PRD's request -> async-generate -> time-limited-download-link flow (which assumes a backend
 * that doesn't exist in this local-only app). CSV satisfies the PRD's core non-functional
 * requirement that the format be "documented and non-proprietary" - see
 * docs/coders-documentation/day-2.md for this scope simplification, and what's still missing
 * relative to the full PRD (per-pillar scope selection, expiry, status states - all moot with
 * no backend to expire from).
 */
object CsvExporter {

    fun exportTransactions(
        context: Context,
        transactions: List<TransactionEntity>,
        categoryNameFor: (Long) -> String
    ): Uri {
        // Own subdirectory (not shared with DiagnosticsExporter's "exports/diagnostics") so
        // clearing old CSV exports below can never race with a diagnostics export in flight.
        val dir = File(context.cacheDir, "exports/transactions").apply { mkdirs() }
        // Bug fix (found via a real user report, 2026-07 - "check storage/cache"): every export
        // wrote a new timestamped file and nothing ever removed the old ones, so cache grew
        // without bound the more this button got tapped. These are single-use share artifacts,
        // not a history worth keeping - clearing the folder first keeps exactly one around.
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "transactions_${System.currentTimeMillis()}.csv")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        file.bufferedWriter().use { writer ->
            writer.write("Date,Merchant,Category,Amount,Direction,Source,Confidence\n")
            transactions.sortedByDescending { it.date }.forEach { txn ->
                writer.write(
                    listOf(
                        dateFormat.format(Date(txn.date)),
                        csvEscape(txn.merchantRaw),
                        csvEscape(categoryNameFor(txn.categoryId)),
                        "%.2f".format(txn.amount),
                        txn.direction.name,
                        txn.source.name,
                        "%.2f".format(txn.confidenceScore)
                    ).joinToString(",")
                )
                writer.newLine()
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun csvEscape(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
