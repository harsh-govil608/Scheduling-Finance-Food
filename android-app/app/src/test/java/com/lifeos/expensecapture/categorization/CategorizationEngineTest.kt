package com.lifeos.expensecapture.categorization

import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pre-beta hardening: every captured transaction runs through this exact function, so a
 * regression here silently miscategorizes real money data. Uses hand-written fakes for the two
 * small DAO interfaces rather than pulling in Robolectric/an in-memory Room DB for a two-method
 * class - proportionate to the size of the thing being tested.
 */
class CategorizationEngineTest {

    private class FakeMerchantRuleDao(private val rules: List<MerchantRuleEntity>) : MerchantRuleDao {
        override suspend fun upsert(rule: MerchantRuleEntity) = error("not used by CategorizationEngine")
        override suspend fun update(rule: MerchantRuleEntity) = error("not used by CategorizationEngine")
        override suspend fun delete(rule: MerchantRuleEntity) = error("not used by CategorizationEngine")
        override suspend fun getAll(): List<MerchantRuleEntity> = rules
        override fun observeAll(): Flow<List<MerchantRuleEntity>> = flowOf(rules)
        override suspend fun reassignCategoryToUncategorized(deletedCategoryId: Long, uncategorizedId: Long) =
            error("not used by CategorizationEngine")
    }

    private class FakeCategoryDao(private val uncategorized: CategoryEntity?) : CategoryDao {
        override suspend fun insertAll(categories: List<CategoryEntity>) = error("not used by CategorizationEngine")
        override suspend fun insert(category: CategoryEntity): Long = error("not used by CategorizationEngine")
        override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(emptyList())
        override suspend fun count(): Int = error("not used by CategorizationEngine")
        override suspend fun getUncategorized(): CategoryEntity? = uncategorized
        override suspend fun delete(category: CategoryEntity) = error("not used by CategorizationEngine")
    }

    private val uncategorized = CategoryEntity(id = 1, name = "Uncategorized")
    private val foodCategory = CategoryEntity(id = 2, name = "Food")

    @Test
    fun `falls back to Uncategorized when no rule matches`() = runBlocking {
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), FakeCategoryDao(uncategorized))
        assertEquals(uncategorized.id, engine.categorize("Some Random Merchant"))
    }

    @Test
    fun `applies a matching active rule`() {
        val rule = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("SWIGGY*Order123")) }
    }

    @Test
    fun `rule matching is case-insensitive and substring-based`() {
        val rule = MerchantRuleEntity(merchantPattern = "ZOMATO", categoryId = foodCategory.id)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("zomato online order")) }
    }

    @Test
    fun `skips a paused rule and falls back to Uncategorized`() {
        val rule = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id, isPaused = true)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(uncategorized.id, engine.categorize("Swiggy Order")) }
    }

    @Test
    fun `uses the first matching rule when more than one could match`() {
        val rules = listOf(
            MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id),
            MerchantRuleEntity(merchantPattern = "order", categoryId = 3L)
        )
        val engine = CategorizationEngine(FakeMerchantRuleDao(rules), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("Swiggy Order")) }
    }

    @Test
    fun `throws a clear error when Uncategorized seeding is missing, rather than silently miscategorizing`() {
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), FakeCategoryDao(null))
        assertThrows(IllegalStateException::class.java) {
            runBlocking { engine.categorize("Anything") }
        }
    }
}
