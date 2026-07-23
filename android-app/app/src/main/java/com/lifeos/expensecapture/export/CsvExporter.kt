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
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
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
