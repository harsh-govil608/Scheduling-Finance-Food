package com.lifeos.expensecapture.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: MerchantRuleEntity)

    /** Predefined categorization rules (2026-08, real user request) - deliberately IGNORE, not
     * REPLACE like upsert() above: seeding must never overwrite a rule that's already there,
     * whether it's a real user correction or a previously-seeded default from an earlier version. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoreConflicts(rules: List<MerchantRuleEntity>)

    @Update
    suspend fun update(rule: MerchantRuleEntity)

    @Delete
    suspend fun delete(rule: MerchantRuleEntity)

    @Query("SELECT * FROM merchant_rules")
    suspend fun getAll(): List<MerchantRuleEntity>

    @Query("SELECT * FROM merchant_rules ORDER BY isManuallyAuthored DESC, merchantPattern ASC")
    fun observeAll(): Flow<List<MerchantRuleEntity>>

    /** Category deletion (see CategoryDao.delete's kdoc) - a rule pointing at a deleted category
     * would otherwise keep auto-categorizing future transactions into a categoryId that no
     * longer exists. */
    @Query("UPDATE merchant_rules SET categoryId = :uncategorizedId WHERE categoryId = :deletedCategoryId")
    suspend fun reassignCategoryToUncategorized(deletedCategoryId: Long, uncategorizedId: Long)
}
