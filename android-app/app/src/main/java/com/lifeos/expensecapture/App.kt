package com.lifeos.expensecapture

import android.app.Application
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.data.seed.DefaultCategories
import com.lifeos.expensecapture.logging.AppLogger
import com.lifeos.expensecapture.logging.CrashHandler
import com.lifeos.expensecapture.notifications.NotificationChannels
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
            CrashHandler.adoptPendingCrashIfAny(this@App)
            if (database.categoryDao().count() == 0) {
                database.categoryDao().insertAll(DefaultCategories.asEntities())
            }
        }
    }
}
