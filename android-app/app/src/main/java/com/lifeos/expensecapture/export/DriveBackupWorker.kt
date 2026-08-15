package com.lifeos.expensecapture.export

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.lifeos.expensecapture.App
import com.lifeos.expensecapture.logging.AppLogger
import java.util.concurrent.TimeUnit

/** Daily automatic backup to Google Drive (2026-08-15) - a no-op (not a failure) if the user
 * never signed in, so this can always be scheduled unconditionally without checking sign-in
 * state at schedule time; it just won't do anything until they do. */
class DriveBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!DriveBackupRepository.isSignedIn(applicationContext)) return Result.success()
        val app = applicationContext as App
        return when (val result = DriveBackupRepository.uploadBackup(applicationContext, app.database)) {
            is DriveBackupResult.Success -> Result.success()
            is DriveBackupResult.Failure -> {
                AppLogger.e("DriveBackupWorker", "scheduled backup failed", Exception(result.message))
                Result.retry()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "drive_auto_backup"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<DriveBackupWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
