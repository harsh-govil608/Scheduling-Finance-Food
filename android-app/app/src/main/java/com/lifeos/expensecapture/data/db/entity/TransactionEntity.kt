package com.lifeos.expensecapture.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class TransactionDirection { DEBIT, CREDIT }

enum class TransactionSource { SMS_AUTO, MANUAL }

/**
 * Mirrors the `transactions` table in the architecture doc (Section 7), with two additions:
 * `synced` tracks whether this row has been pushed to the backend once sync is wired, and
 * `sourceHash` (added when the SBI template landed) makes re-ingesting the same SMS a no-op
 * instead of a duplicate row. Before this, `TransactionDao.insert`'s `OnConflictStrategy.IGNORE`
 * had no unique constraint to actually act on, so re-scanning the SMS inbox a second time (which
 * a manual scan-flag reset can trigger - see docs/coders-documentation/day-3.md) silently
 * doubled every transaction already in the ledger. `sourceHash` is `sender::body` for
 * SMS-derived rows (identical real SMS text -> identical hash -> the unique index below rejects
 * the re-insert) and a random UUID for manual entries, which should never be deduplicated
 * against each other.
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["sourceHash"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val direction: TransactionDirection,
    val merchantRaw: String,
    val merchantNormalized: String,
    val categoryId: Long,
    val date: Long, // epoch millis
    val source: TransactionSource,
    val confidenceScore: Float,
    val isUserCorrected: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val sourceHash: String = UUID.randomUUID().toString(),
    /** Bank/UPI reference or transaction ID, when the parser could extract one (see
     * GenericTransactionExtractor/TransactionParser). Null for manual entries and for any SMS
     * format with no extractable reference. Used as a secondary duplicate check alongside
     * `sourceHash` - see TransactionDao.countByReferenceId's kdoc for why the exact-text hash
     * alone isn't enough. */
    val referenceId: String? = null,
    /** True for a CREDIT that reverses an earlier purchase (matched "refund"/"reversed" in the
     * SMS - see GenericTransactionExtractor.REFUND_KEYWORDS), as opposed to unrelated income
     * (salary, a friend paying you back). Real user observation, 2026-08-12: without this, both
     * look like the same generic CREDIT, and there's no way to answer "how much came back to me
     * as refunds" - or, later, to decide deliberately whether refunds should net against "Spent
     * This Month" instead of always leaving it untouched. Always false for manual entries and
     * DEBIT transactions. */
    val isRefund: Boolean = false,
    /** True when the user has explicitly marked this as money moving between their OWN accounts
     * (e.g. a personal transfer between two bank accounts, or to/from a wallet they also own) -
     * not real income or real spending. Pattern Engine design, 2026-08-12 (see
     * IncomePatternDetector's kdoc): a recurring transfer would otherwise look identical to
     * recurring income and pollute income-pattern detection. Deliberately manual-only for now,
     * not auto-detected - telling apart "my own money moving" from "a spouse's genuine monthly
     * contribution" from bank/UPI SMS text alone is a real, unsolved classification problem, and
     * guessing wrong here would silently suppress real income from someone's forecast. Always
     * false until a user marks it; never set automatically. */
    val isTransfer: Boolean = false
)
