package com.lifeos.expensecapture.importer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.expensecapture.categorization.CategorizationEngine
import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/** Common statement date formats to offer, not a claim of covering every issuer - the user
 * picks whichever actually matches their file, same "user resolves the ambiguity we can't
 * verify" approach as the sign-convention toggle. */
val SUPPORTED_DATE_FORMATS = listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "dd MMM yyyy")

data class ParsedRow(val date: Long?, val merchant: String, val amount: Double?, val direction: TransactionDirection)

data class ImportStatementUiState(
    val headers: List<String> = emptyList(),
    val dataRows: List<List<String>> = emptyList(),
    val dateColumn: Int? = null,
    val descriptionColumn: Int? = null,
    val amountColumn: Int? = null,
    /** Most credit card/bank exports show spending as positive and refunds/payments-in as
     * negative - this is the more common convention, hence the default, but real formats vary
     * enough that it has to be a toggle, not an assumption (see CsvParser's kdoc). */
    val positiveMeansDebit: Boolean = true,
    val dateFormat: String = SUPPORTED_DATE_FORMATS[0],
    val importing: Boolean = false,
    val importedCount: Int = 0,
    val importComplete: Boolean = false,
    val loadError: String? = null
) {
    val isMappingComplete: Boolean get() = dateColumn != null && descriptionColumn != null && amountColumn != null
}

class ImportStatementViewModel(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val merchantRuleDao: MerchantRuleDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportStatementUiState())
    val uiState: StateFlow<ImportStatementUiState> = _uiState.asStateFlow()

    fun loadCsv(text: String) {
        val rows = CsvParser.parse(text)
        if (rows.isEmpty()) {
            _uiState.value = ImportStatementUiState(loadError = "That file has no rows to import.")
            return
        }
        val headers = rows.first()
        val dataRows = rows.drop(1)
        // Best-effort auto-detect by header name - a convenience, not a requirement; the user
        // can always override any of these via the dropdowns.
        fun findColumn(vararg keywords: String): Int? =
            headers.indexOfFirst { header -> keywords.any { header.contains(it, ignoreCase = true) } }
                .takeIf { it >= 0 }

        _uiState.value = ImportStatementUiState(
            headers = headers,
            dataRows = dataRows,
            dateColumn = findColumn("date"),
            descriptionColumn = findColumn("narration", "description", "particulars", "details", "merchant"),
            amountColumn = findColumn("amount", "value")
        )
    }

    fun setDateColumn(index: Int) { _uiState.value = _uiState.value.copy(dateColumn = index) }
    fun setDescriptionColumn(index: Int) { _uiState.value = _uiState.value.copy(descriptionColumn = index) }
    fun setAmountColumn(index: Int) { _uiState.value = _uiState.value.copy(amountColumn = index) }
    fun setPositiveMeansDebit(value: Boolean) { _uiState.value = _uiState.value.copy(positiveMeansDebit = value) }
    fun setDateFormat(format: String) { _uiState.value = _uiState.value.copy(dateFormat = format) }

    fun parseRow(row: List<String>, state: ImportStatementUiState = _uiState.value): ParsedRow? {
        val dateCol = state.dateColumn ?: return null
        val descCol = state.descriptionColumn ?: return null
        val amountCol = state.amountColumn ?: return null
        if (dateCol >= row.size || descCol >= row.size || amountCol >= row.size) return null

        val dateFormatter = remember(state.dateFormat)
        val date = try {
            dateFormatter.parse(row[dateCol])?.time
        } catch (e: Exception) {
            null
        }
        val rawAmount = row[amountCol].replace(",", "").replace("₹", "").trim()
        val amount = rawAmount.toDoubleOrNull()
        val direction = if (amount != null) {
            val isPositive = amount >= 0
            if (isPositive == state.positiveMeansDebit) TransactionDirection.DEBIT else TransactionDirection.CREDIT
        } else {
            TransactionDirection.DEBIT
        }
        return ParsedRow(date = date, merchant = row[descCol], amount = amount?.let { Math.abs(it) }, direction = direction)
    }

    // Not @Composable - a tiny private cache so every row during a bulk import doesn't
    // reallocate its own SimpleDateFormat, which (unlike most formatters) is genuinely not
    // thread-safe to share across calls if state.dateFormat changes mid-import - safe here since
    // one import run always uses one fixed format from the confirmed uiState snapshot.
    private var cachedFormat: SimpleDateFormat? = null
    private var cachedPattern: String? = null
    private fun remember(pattern: String): SimpleDateFormat {
        if (cachedPattern != pattern) {
            cachedFormat = SimpleDateFormat(pattern, Locale.getDefault())
            cachedPattern = pattern
        }
        return cachedFormat!!
    }

    fun previewRows(count: Int = 5): List<ParsedRow?> {
        val state = _uiState.value
        return state.dataRows.take(count).map { parseRow(it, state) }
    }

    fun import() {
        val state = _uiState.value
        if (!state.isMappingComplete || state.importing) return
        _uiState.value = state.copy(importing = true)
        viewModelScope.launch {
            val categorizationEngine = CategorizationEngine(merchantRuleDao, categoryDao)
            var imported = 0
            for (row in state.dataRows) {
                val parsed = parseRow(row, state) ?: continue
                val date = parsed.date ?: continue
                val amount = parsed.amount ?: continue
                if (amount <= 0.0 || parsed.merchant.isBlank()) continue

                val categoryId = categorizationEngine.categorize(parsed.merchant)
                val entity = TransactionEntity(
                    amount = amount,
                    direction = parsed.direction,
                    merchantRaw = parsed.merchant,
                    merchantNormalized = parsed.merchant.trim().lowercase(),
                    categoryId = categoryId,
                    date = date,
                    source = TransactionSource.MANUAL,
                    confidenceScore = 1f,
                    // Deterministic per logical transaction (not random, unlike MANUAL entries
                    // added one at a time via ManualEntryDialog) so re-importing the same
                    // statement file twice is a safe no-op via the existing unique-index +
                    // OnConflictStrategy.IGNORE dedup TransactionDao.insert already relies on for
                    // SMS - see TransactionEntity's own kdoc on sourceHash.
                    sourceHash = "csv::$date::${parsed.merchant.trim().lowercase()}::$amount"
                )
                val insertedId = transactionDao.insert(entity)
                if (insertedId > 0) imported++
            }
            _uiState.value = _uiState.value.copy(importing = false, importedCount = imported, importComplete = true)
        }
    }
}
