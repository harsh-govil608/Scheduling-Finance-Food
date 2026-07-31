package com.lifeos.expensecapture.importer

/**
 * A small quoted-field-aware CSV line splitter (real user review: "Just give us the option to
 * upload credit card statement"). Deliberately generic rather than per-bank templates, the same
 * reasoning BankTemplate.kt's own kdoc gives for SMS parsing: every bank/card issuer's statement
 * export uses different column names and ordering, and fabricating specific column-name guesses
 * without real sample exports from each issuer would be guessing, not engineering.
 * ImportStatementScreen instead asks the user which of their file's own columns is which
 * (date/description/amount) - see ImportStatementViewModel.
 */
object CsvParser {

    /** Splits every non-blank line of `text` into cells, honoring double-quoted fields that may
     * contain commas or escaped ("") quotes - a naive `split(",")` would corrupt any statement
     * where a merchant description itself contains a comma, which real ones routinely do. */
    fun parse(text: String): List<List<String>> =
        text.lineSequence()
            .filter { it.isNotBlank() }
            .map { parseLine(it) }
            .toList()

    private fun parseLine(line: String): List<String> {
        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    cells.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        cells.add(current.toString())
        return cells.map { it.trim() }
    }
}
