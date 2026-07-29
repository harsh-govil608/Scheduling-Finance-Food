package com.lifeos.expensecapture

import android.app.Application
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.logging.CrashHandler
import com.lifeos.expensecapture.notifications.NotificationChannels
import com.lifeos.expensecapture.sms.parser.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        // Pre-beta hardening (Priority 2): must be the first two lines, before anything else can
        // possibly throw - a crash during this method's own later setup should still be caught.
        AppLogger.init(this)
        CrashHandler.install(this)

        database = AppDatabase.getInstance(this)
        NotificationChannels.ensureCreated(this)
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
                }
                cleanUpExistingReviewNoiseOnce()
            } catch (e: Exception) {
                AppLogger.e("App", "startup initialization failed", e)
            }
        }
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
}
