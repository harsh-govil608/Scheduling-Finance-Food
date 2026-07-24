package com.lifeos.expensecapture.update

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val notes: String
)

/**
 * A pilot tester on a manually-sideloaded APK has no Play Store to auto-update from - every fix
 * previously meant re-sending a whole new .apk file over WhatsApp/Drive and asking them to
 * re-install it by hand (see docs/coders-documentation/day-3.md). This is the minimal in-app
 * alternative: check a small JSON manifest committed alongside the APK itself in this same git
 * repo (no backend, no Play Console account, no new paid infrastructure), and if it names a
 * newer versionCode, offer a one-tap download + install.
 *
 * This is a one-way ratchet: the FIRST build containing this file still has to be manually sent
 * and installed once, the same as always - there is no code on a device that predates this file
 * that could have known to check for it. Every version AFTER that one can update itself as long
 * as `android-app/distribution/latest.json` and the APK it points to are kept in sync on every
 * release (see the "Releasing an update" note in that same file).
 *
 * No signature/checksum pinning on the downloaded APK - acceptable because the manifest and the
 * APK both live in a repo the developer directly controls, not third-party content.
 */
object UpdateChecker {

    private const val MANIFEST_URL =
        "https://raw.githubusercontent.com/harsh-govil608/Scheduling-Finance-Food/main/android-app/distribution/latest.json"

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val body = URL(MANIFEST_URL).readText()
            val json = JSONObject(body)
            val remoteVersionCode = json.getInt("versionCode")
            val currentVersionCode = currentVersionCode(context)
            if (remoteVersionCode > currentVersionCode) {
                UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = json.getString("versionName"),
                    downloadUrl = json.getString("downloadUrl"),
                    notes = json.optString("notes", "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            // Offline, GitHub unreachable, manifest malformed - never blocks the app over this.
            null
        }
    }

    suspend fun downloadAndLaunchInstall(context: Context, update: UpdateInfo) {
        val file = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val target = File(dir, "update.apk")
            URL(update.downloadUrl).openStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun currentVersionCode(context: Context): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            info.versionCode
        }
    }
}
