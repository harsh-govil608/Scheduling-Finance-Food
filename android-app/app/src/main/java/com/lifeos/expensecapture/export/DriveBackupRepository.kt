package com.lifeos.expensecapture.export

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import com.lifeos.expensecapture.data.db.AppDatabase
import com.lifeos.expensecapture.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File as JavaFile

sealed class DriveBackupResult {
    data class Success(val fileName: String) : DriveBackupResult()
    data class Failure(val message: String) : DriveBackupResult()
}

/**
 * Auto Backup to Google Drive (2026-08-15, real user request, standing backlog item since the
 * project's earliest planning). Reuses BackupExporter/BackupImporter's exact same database-file
 * copy/validate logic unchanged - this class only adds "where the file goes," not a second
 * backup format to keep in sync with the local one.
 *
 * Scoped to [DriveScopes.DRIVE_FILE] deliberately, not full Drive access - that scope only ever
 * sees files this app itself created, which is exactly what auto-backup needs and, unlike
 * broader Drive scopes, qualifies for Google's lighter OAuth verification path rather than a
 * full sensitive-scope security review (real consideration flagged during this app's Play Store
 * SMS-permission research the same day this was built).
 *
 * ONE-TIME SETUP STILL NEEDED before this works (same class of external, non-code step as the
 * Firebase project setup already documented elsewhere in this repo):
 * 1. In Google Cloud Console (the same GCP project google-services.json already points at),
 *    enable the "Google Drive API" under APIs & Services > Library.
 * 2. Under APIs & Services > Credentials, create (or confirm) an OAuth 2.0 Client ID of type
 *    "Android", registered with this app's applicationId (com.lifeos.expensecapture) and its
 *    signing certificate SHA-1 fingerprint - both the debug keystore's and, once relevant, the
 *    release keystore's (see keystore.properties/keystore/release.jks already in this repo).
 * 3. Configure the OAuth consent screen (Testing mode is enough for a pilot - only explicitly
 *    added test-user Google accounts, e.g. yours and Sohom's, can sign in until this app goes
 *    through verification for a wider audience).
 * Without this, GoogleSignIn will fail at the account picker or the Drive API calls will return
 * an authorization error - both surface through DriveBackupResult.Failure with a real message
 * rather than crashing, but the feature is inert until this setup is done.
 */
object DriveBackupRepository {

    private const val BACKUP_MIME_TYPE = "application/octet-stream"
    private const val BACKUP_NAME_PREFIX = "lifeos_backup_"
    /** Keeps Drive storage bounded - an auto-backup that runs daily forever without pruning would
     * otherwise accumulate indefinitely for a "set it and forget it" feature nobody manually
     * cleans up. */
    private const val MAX_RETAINED_BACKUPS = 5

    private fun signInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
            .build()

    fun signInClient(context: Context): GoogleSignInClient =
        GoogleSignIn.getClient(context, signInOptions())

    fun getSignInIntent(context: Context): Intent = signInClient(context).signInIntent

    /** Non-null only once the account has actually granted the Drive.file scope - a plain "signed
     * into Google" account (e.g. one already used elsewhere on the device) without that scope
     * grant can't be used for Drive calls, so this is deliberately stricter than
     * GoogleSignIn.getLastSignedInAccount alone. */
    fun signedInAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val hasScope = GoogleSignIn.hasPermissions(account, com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
        return if (hasScope) account else null
    }

    fun isSignedIn(context: Context): Boolean = signedInAccount(context) != null

    fun signOut(context: Context) {
        signInClient(context).signOut()
    }

    fun accountFromSignInResult(intent: Intent?): GoogleSignInAccount? {
        return try {
            GoogleSignIn.getSignedInAccountFromIntent(intent).getResult(ApiException::class.java)
        } catch (e: ApiException) {
            AppLogger.e("DriveBackupRepository", "sign-in result failed, code=${e.statusCode}", e)
            null
        }
    }

    private fun driveService(context: Context, account: GoogleSignInAccount): Drive {
        // Real bug fix (2026-08, user report - upload failed with the cryptic "the name must not
        // be empty: null"): GoogleSignInAccount.getAccount() (the raw on-device Account object)
        // is deprecated in current Play Services and commonly returns null even after a
        // successful sign-in, for privacy reasons unrelated to whether Drive access was granted.
        // Passing that null Account into selectedAccount left the credential with nothing to read
        // a name off of once it tried to fetch a token - selecting by email string instead avoids
        // depending on the raw Account object at all; GoogleSignInAccount.email is always present
        // once requestEmail() is part of the sign-in options (see signInOptions()).
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE)).apply {
            selectedAccountName = account.email
                ?: error("Signed-in Google account has no email - please sign out and sign in again")
        }
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Expense Capture Pilot")
            .build()
    }

    suspend fun uploadBackup(context: Context, db: AppDatabase): DriveBackupResult = withContext(Dispatchers.IO) {
        val account = signedInAccount(context)
            ?: return@withContext DriveBackupResult.Failure("Not signed in to Google Drive")
        try {
            val service = driveService(context, account)
            val backupUri = BackupExporter.exportDatabase(context, db)
            val localFile = context.contentResolver.openInputStream(backupUri)?.use { input ->
                val temp = JavaFile.createTempFile("drive_upload", ".db", context.cacheDir)
                temp.outputStream().use { output -> input.copyTo(output) }
                temp
            } ?: return@withContext DriveBackupResult.Failure("Couldn't read the backup file to upload")

            val metadata = DriveFile().setName(localFile.let {
                "$BACKUP_NAME_PREFIX${System.currentTimeMillis()}.db"
            })
            val content = com.google.api.client.http.FileContent(BACKUP_MIME_TYPE, localFile)
            val uploaded = service.files().create(metadata, content).setFields("id, name").execute()
            localFile.delete()

            pruneOldBackups(service)
            DriveBackupResult.Success(uploaded.name)
        } catch (e: Exception) {
            AppLogger.e("DriveBackupRepository", "upload failed", e)
            DriveBackupResult.Failure(e.message ?: "Upload to Google Drive failed")
        }
    }

    /** Downloads the most recently created backup into a local cache file for BackupImporter to
     * validate/restore exactly as it already does for a manually-picked local file - no separate
     * restore path to maintain. Null means either "not signed in" or "no backup exists yet." */
    suspend fun downloadLatestBackup(context: Context): JavaFile? = withContext(Dispatchers.IO) {
        val account = signedInAccount(context) ?: return@withContext null
        try {
            val service = driveService(context, account)
            val latest = listBackups(service).maxByOrNull { it.createdTime?.value ?: 0L } ?: return@withContext null

            val target = JavaFile(context.cacheDir, "drive_restore_candidate.db")
            target.outputStream().use { output ->
                service.files().get(latest.id).executeMediaAndDownloadTo(output)
            }
            target
        } catch (e: Exception) {
            AppLogger.e("DriveBackupRepository", "download failed", e)
            null
        }
    }

    private fun listBackups(service: Drive): List<DriveFile> =
        service.files().list()
            .setQ("name contains '$BACKUP_NAME_PREFIX' and trashed = false")
            .setFields("files(id, name, createdTime)")
            .setSpaces("drive")
            .execute()
            .files ?: emptyList()

    private fun pruneOldBackups(service: Drive) {
        val backups = listBackups(service).sortedByDescending { it.createdTime?.value ?: 0L }
        backups.drop(MAX_RETAINED_BACKUPS).forEach { stale ->
            try {
                service.files().delete(stale.id).execute()
            } catch (e: Exception) {
                AppLogger.e("DriveBackupRepository", "failed to prune old backup ${stale.name}", e)
            }
        }
    }
}
