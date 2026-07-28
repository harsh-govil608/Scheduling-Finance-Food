package com.lifeos.expensecapture.data.repository

import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.CorrectionDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.dao.TransactionDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.CorrectionEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
import com.lifeos.expensecapture.data.db.entity.TransactionEntity
import com.lifeos.expensecapture.data.db.entity.TransactionSource
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val merchantRuleDao: MerchantRuleDao,
    private val correctionDao: CorrectionDao
) {
    fun observeLedger(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    /**
     * Recategorizing also writes a merchant_rule so future transactions from the same
     * merchant default correctly - this is the entire "learning" loop for the pilot
     * (architecture doc Section 6/10), and the exact seed dataset Phase 5's eventual
     * categorization ML would train on (Section 12).
     */
    suspend fun recategorize(transaction: TransactionEntity, newCategoryId: Long) {
        transactionDao.recategorize(transaction.id, newCategoryId)
        correctionDao.insert(
            CorrectionEntity(
                transactionId = transaction.id,
                oldCategoryId = transaction.categoryId,
                newCategoryId = newCategoryId
            )
        )
        merchantRuleDao.upsert(
            MerchantRuleEntity(
                merchantPattern = transaction.merchantNormalized,
                categoryId = newCategoryId,
                createdFromUserCorrection = true
            )
        )
    }

    suspend fun addManualTransaction(
        amount: Double,
        direction: TransactionDirection,
        merchant: String,
        categoryId: Long,
        date: Long
    ) {
        transactionDao.insert(
            TransactionEntity(
                amount = amount,
                direction = direction,
                merchantRaw = merchant,
                merchantNormalized = merchant.trim().lowercase(),
                categoryId = categoryId,
                date = date,
                source = TransactionSource.MANUAL,
                confidenceScore = 1.0f
            )
        )
    }

    /** Ledger deletion (found via a real user report, 2026-07): no way existed to remove a
     * transaction (a mis-entered manual one, a duplicate, etc.) once it was captured. */
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }
}
