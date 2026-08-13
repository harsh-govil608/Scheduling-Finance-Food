package com.lifeos.expensecapture.categorization

import com.lifeos.expensecapture.data.db.dao.CategoryDao
import com.lifeos.expensecapture.data.db.dao.MerchantRuleDao
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import com.lifeos.expensecapture.data.db.entity.TransactionDirection
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
        override suspend fun insertAllIgnoreConflicts(rules: List<MerchantRuleEntity>) = error("not used by CategorizationEngine")
        override suspend fun update(rule: MerchantRuleEntity) = error("not used by CategorizationEngine")
        override suspend fun delete(rule: MerchantRuleEntity) = error("not used by CategorizationEngine")
        override suspend fun getAll(): List<MerchantRuleEntity> = rules
        override fun observeAll(): Flow<List<MerchantRuleEntity>> = flowOf(rules)
        override suspend fun reassignCategoryToUncategorized(deletedCategoryId: Long, uncategorizedId: Long) =
            error("not used by CategorizationEngine")
    }

    private class FakeCategoryDao(
        private val uncategorized: CategoryEntity?,
        private val byName: Map<String, CategoryEntity> = emptyMap()
    ) : CategoryDao {
        override suspend fun insertAll(categories: List<CategoryEntity>) = error("not used by CategorizationEngine")
        override suspend fun insert(category: CategoryEntity): Long = error("not used by CategorizationEngine")
        override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(emptyList())
        override suspend fun count(): Int = error("not used by CategorizationEngine")
        override suspend fun getUncategorized(): CategoryEntity? = uncategorized
        override suspend fun delete(category: CategoryEntity) = error("not used by CategorizationEngine")
        override suspend fun findByName(name: String): CategoryEntity? = byName[name]
    }

    private val uncategorized = CategoryEntity(id = 1, name = "Uncategorized")
    private val foodCategory = CategoryEntity(id = 2, name = "Food")
    private val groceriesCategory = CategoryEntity(id = 3, name = "Groceries")
    private val shoppingCategory = CategoryEntity(id = 4, name = "Shopping")

    // Existing tests below use CREDIT so they stay decoupled from the amount-based DEBIT-only
    // fallback (added 2026-08, see CategorizationEngine's kdoc) - they're testing merchant-rule
    // matching and the Uncategorized fallback specifically, not the new fallback.

    @Test
    fun `falls back to Uncategorized when no rule matches`() = runBlocking {
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), FakeCategoryDao(uncategorized))
        assertEquals(uncategorized.id, engine.categorize("Some Random Merchant", 100.0, TransactionDirection.CREDIT))
    }

    @Test
    fun `applies a matching active rule`() {
        val rule = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("SWIGGY*Order123", 250.0, TransactionDirection.DEBIT)) }
    }

    @Test
    fun `rule matching is case-insensitive and substring-based`() {
        val rule = MerchantRuleEntity(merchantPattern = "ZOMATO", categoryId = foodCategory.id)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("zomato online order", 250.0, TransactionDirection.DEBIT)) }
    }

    @Test
    fun `skips a paused rule and falls back to Uncategorized`() {
        val rule = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id, isPaused = true)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(uncategorized.id, engine.categorize("Swiggy Order", 250.0, TransactionDirection.CREDIT)) }
    }

    @Test
    fun `uses the first matching rule when more than one could match`() {
        val rules = listOf(
            MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id),
            MerchantRuleEntity(merchantPattern = "order", categoryId = 3L)
        )
        val engine = CategorizationEngine(FakeMerchantRuleDao(rules), FakeCategoryDao(uncategorized))

        runBlocking { assertEquals(foodCategory.id, engine.categorize("Swiggy Order", 250.0, TransactionDirection.DEBIT)) }
    }

    @Test
    fun `throws a clear error when Uncategorized seeding is missing, rather than silently miscategorizing`() {
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), FakeCategoryDao(null))
        assertThrows(IllegalStateException::class.java) {
            runBlocking { engine.categorize("Anything", 100.0, TransactionDirection.CREDIT) }
        }
    }

    // Amount-based default categorization (2026-08, real user request: "lower spending, less
    // than 500 goes to groceries and more than 500 goes to the shopping category") - a fallback
    // that only applies once merchant-rule matching above has already missed.

    @Test
    fun `falls back to Groceries for a small DEBIT amount when no rule matches`() = runBlocking {
        val categoryDao = FakeCategoryDao(uncategorized, mapOf("Groceries" to groceriesCategory, "Shopping" to shoppingCategory))
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), categoryDao)
        assertEquals(groceriesCategory.id, engine.categorize("Some Random Merchant", 499.99, TransactionDirection.DEBIT))
    }

    @Test
    fun `falls back to Shopping for a large DEBIT amount when no rule matches`() = runBlocking {
        val categoryDao = FakeCategoryDao(uncategorized, mapOf("Groceries" to groceriesCategory, "Shopping" to shoppingCategory))
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), categoryDao)
        assertEquals(shoppingCategory.id, engine.categorize("Some Random Merchant", 500.0, TransactionDirection.DEBIT))
    }

    @Test
    fun `does not apply the amount fallback to a CREDIT transaction`() = runBlocking {
        val categoryDao = FakeCategoryDao(uncategorized, mapOf("Groceries" to groceriesCategory, "Shopping" to shoppingCategory))
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), categoryDao)
        assertEquals(uncategorized.id, engine.categorize("Salary Credit", 100.0, TransactionDirection.CREDIT))
    }

    @Test
    fun `a matching merchant rule wins over the amount fallback`() = runBlocking {
        val rule = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id)
        val categoryDao = FakeCategoryDao(uncategorized, mapOf("Groceries" to groceriesCategory, "Shopping" to shoppingCategory))
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(rule)), categoryDao)
        assertEquals(foodCategory.id, engine.categorize("Swiggy Order", 100.0, TransactionDirection.DEBIT))
    }

    @Test
    fun `falls back to Uncategorized when Groceries and Shopping are not seeded`() = runBlocking {
        val engine = CategorizationEngine(FakeMerchantRuleDao(emptyList()), FakeCategoryDao(uncategorized))
        assertEquals(uncategorized.id, engine.categorize("Some Random Merchant", 100.0, TransactionDirection.DEBIT))
    }

    // Predefined categorization rules (2026-08, real user request) - a real bug found while
    // scoping this: the lookup had no priority ordering, so a seeded default could silently
    // shadow a real user correction whenever both patterns matched the same merchant text.

    @Test
    fun `a real user correction wins over a seeded default for the same merchant text`() = runBlocking {
        val seeded = MerchantRuleEntity(merchantPattern = "swiggy", categoryId = foodCategory.id, isSeededDefault = true)
        val userCorrection = MerchantRuleEntity(
            merchantPattern = "swiggy",
            categoryId = groceriesCategory.id,
            createdFromUserCorrection = true
        )
        // Deliberately inserted seeded-first, mirroring real startup order (seeding runs once at
        // launch, before any correction could exist) - if match order alone decided this, the
        // seeded rule (earlier in the list) would win, which is exactly the bug being tested for.
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(seeded, userCorrection)), FakeCategoryDao(uncategorized))
        assertEquals(groceriesCategory.id, engine.categorize("Swiggy Order", 100.0, TransactionDirection.DEBIT))
    }

    @Test
    fun `a longer seeded pattern wins over a shorter seeded pattern for the same merchant text`() = runBlocking {
        val generic = MerchantRuleEntity(merchantPattern = "amazon", categoryId = shoppingCategory.id, isSeededDefault = true)
        val specific = MerchantRuleEntity(merchantPattern = "amazon prime", categoryId = foodCategory.id, isSeededDefault = true)
        val engine = CategorizationEngine(FakeMerchantRuleDao(listOf(generic, specific)), FakeCategoryDao(uncategorized))
        assertEquals(foodCategory.id, engine.categorize("AMAZON PRIME MEMBERSHIP", 100.0, TransactionDirection.DEBIT))
    }
}
