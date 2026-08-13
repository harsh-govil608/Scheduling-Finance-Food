package com.lifeos.expensecapture

import android.app.Application
import com.lifeos.expensecapture.billing.BillingRepository
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.db.entity.CategoryEntity
import com.lifeos.expensecapture.data.db.entity.MerchantRuleEntity
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.data.seed.DefaultMerchantRules
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.logging.CrashHandler
import com.lifeos.expensecapture.notifications.NotificationChannels
import com.lifeos.expensecapture.sms.parser.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class App : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    /** Monetization scaffolding (2026-08-12) - see BillingRepository's own kdoc for why this is
     * inert (connects fine, but every purchase/product query comes back empty) until the app has
     * a real Play Console listing. Application-scoped like `database`, for the same reason: it
     * needs to outlive any single screen, and every screen that cares about entitlement should
     * observe the same instance rather than each opening its own billing connection. */
    lateinit var billingRepository: BillingRepository
        private set

    override fun onCreate() {
        super.onCreate()
        // Pre-beta hardening (Priority 2): must be the first two lines, before anything else can
        // possibly throw - a crash during this method's own later setup should still be caught.
        AppLogger.init(this)
        CrashHandler.install(this)

        database = AppDatabase.getInstance(this)
        billingRepository = BillingRepository(this)
        NotificationChannels.ensureCreated(this)
        applicationScope.launch {
            // Best-effort, deliberately outside the try/catch below: a billing connection
            // failure (e.g. no Play Store on this device at all, or - right now - simply because
            // this app isn't installed via Play yet) must never be able to block or crash the
            // rest of startup. isPremium just stays at its locally-cached value (false, until a
            // real purchase ever completes) if this doesn't succeed.
            try {
                if (billingRepository.connect()) billingRepository.restorePurchases()
            } catch (e: Exception) {
                AppLogger.e("App", "billing connect/restore failed", e)
            }
        }
        applicationScope.launch {
            // Pre-beta hardening (Priority 4 - reliability): this runs unconditionally on every
            // single app launch. Before this try/catch, an exception here (e.g. a transient DB
            // issue) would propagate uncaught out of a SupervisorJob-scoped coroutine straight to
            // the thread's default handler - now CrashHandler, meaning a genuine boot-loop risk
            // (crash on every launch, with no way to even reach the Diagnostics screen to see
            // why). This is the one async call in the app that gets that treatment explicitly;
            // every other ViewModel/worker coroutine is reviewed in the reliability report rather
            // than individually wrapped, since this is the only one that runs before the user can
            // do anything at all.
            try {
                CrashHandler.adoptPendingCrashIfAny(this@App)
                if (database.categoryDao().count() == 0) {
                    database.categoryDao().insertAll(DefaultCategories.asEntities())
                } else {
                    backfillNewDefaultCategoriesOnce()
                }
                seedDefaultMerchantRulesOnce()
                cleanUpExistingReviewNoiseOnce()
                cleanUpOtpMessagesFromReviewOnce()
            } catch (e: Exception) {
                AppLogger.e("App", "startup initialization failed", e)
            }
        }
    }

    /**
     * One-time migration (2026-07-31): DefaultCategories.names only reaches a brand-new install
     * (categoryDao().count() == 0 branch above) - an existing install's categories table was
     * already populated long before "Travel"/"Loan & EMI" were added to that list, so it would
     * never pick them up on its own. Backfills just those two, by name, rather than re-running
     * the full seed list - inserting the other ten again would either duplicate them (no unique
     * constraint on name) or silently no-op depending on which row ID conflict fired, neither of
     * which is what "add two categories" should do to everyone's existing category list.
     */
    private suspend fun backfillNewDefaultCategoriesOnce() {
        val prefs = getSharedPreferences("app_migrations", MODE_PRIVATE)
        if (prefs.getBoolean("travel_emi_categories_v1_done", false)) return

        val existingNames = database.categoryDao().observeAll().first().map { it.name }.toSet()
        val missing = listOf("Travel", "Loan & EMI").filterNot { it in existingNames }
        if (missing.isNotEmpty()) {
            database.categoryDao().insertAll(missing.map { CategoryEntity(name = it, isSystemDefault = true) })
        }
        prefs.edit().putBoolean("travel_emi_categories_v1_done", true).apply()
    }

    /**
     * Predefined categorization rules (2026-08, real user request - "Predefined Categorization
     * rules Coming soon"): seeds a curated starter set of common Indian merchant->category rules
     * (data/seed/DefaultMerchantRules.kt) so a fresh or existing install has reasonable default
     * categorization from day one instead of starting from zero rules. Runs for existing installs
     * too, not just fresh ones (unlike the categories seed above, which only ever reaches a
     * brand-new install) - guarded by its own one-time flag, same pattern as
     * backfillNewDefaultCategoriesOnce. Uses insertAllIgnoreConflicts (not upsert's REPLACE), so
     * this can never overwrite a rule that's already there for any reason.
     */
    private suspend fun seedDefaultMerchantRulesOnce() {
        val prefs = getSharedPreferences("app_migrations", MODE_PRIVATE)
        if (prefs.getBoolean("default_merchant_rules_v1_done", false)) return

        val categoryIdByName = database.categoryDao().observeAll().first().associate { it.name to it.id }
        val rules = DefaultMerchantRules.patternsByCategory.mapNotNull { (pattern, categoryName) ->
            categoryIdByName[categoryName]?.let { categoryId ->
                MerchantRuleEntity(merchantPattern = pattern, categoryId = categoryId, isSeededDefault = true)
            }
        }
        if (rules.isNotEmpty()) {
            database.merchantRuleDao().insertAllIgnoreConflicts(rules)
        }
        prefs.edit().putBoolean("default_merchant_rules_v1_done", true).apply()
    }

    /**
     * One-time migration (2026-07): the institutional-sender noise filter in
     * TransactionIngestor only stops NEW non-bank messages from being added to Needs Review -
     * it doesn't touch rows already sitting there from before this update. Without this, a user
     * upgrading would see no visible change (see the real user report that caught this: "nothing
     * seemed to change, what is the change"). Runs once per install, guarded by a SharedPreferences
     * flag so it never re-scans on every launch.
     */
    private suspend fun cleanUpExistingReviewNoiseOnce() {
        val prefs = getSharedPreferences("app_migrations", MODE_PRIVATE)
        if (prefs.getBoolean("needs_review_noise_cleanup_v1_done", false)) return

        val noiseIds = database.unparsedMessageDao().getAllUnresolved()
            .filterNot { TransactionParser.looksLikeInstitutionalSender(it.sender) }
            .map { it.id }
        if (noiseIds.isNotEmpty()) {
            database.unparsedMessageDao().deleteByIds(noiseIds)
        }
        prefs.edit().putBoolean("needs_review_noise_cleanup_v1_done", true).apply()
    }

    /**
     * One-time migration (2026-08): TransactionParser's OTP filter only ever stopped a NEW
     * OTP/verification SMS from being stored in Needs Review going forward - it never touched
     * rows already sitting there from before that filter existed (or before its keyword list
     * covered a given bank's wording). Found via a real user report, 2026-08: "Data privacy
     * concerns. I see OTPs are also shown in the app... in needs review" - the live filter was
     * already correct, the gap was purely retroactive, the same shape of bug
     * cleanUpExistingReviewNoiseOnce fixed for non-institutional-sender noise. Runs once per
     * install, guarded by its own SharedPreferences flag.
     */
    private suspend fun cleanUpOtpMessagesFromReviewOnce() {
        val prefs = getSharedPreferences("app_migrations", MODE_PRIVATE)
        if (prefs.getBoolean("needs_review_otp_cleanup_v1_done", false)) return

        val otpIds = database.unparsedMessageDao().getAllUnresolved()
            .filter { TransactionParser.looksLikeOtpOrVerification(it.body) }
            .map { it.id }
        if (otpIds.isNotEmpty()) {
            database.unparsedMessageDao().deleteByIds(otpIds)
        }
        prefs.edit().putBoolean("needs_review_otp_cleanup_v1_done", true).apply()
    }
}
