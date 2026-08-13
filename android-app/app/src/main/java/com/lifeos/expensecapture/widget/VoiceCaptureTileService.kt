package com.lifeos.expensecapture.widget

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.lifeos.expensecapture.voice.VoiceCaptureActivity

/**
 * Voice quick-capture (2026-08, real user request: "AI voice assistant outside app for quick
 * records and summaries") - a Quick Settings tile, reachable via swipe-down from anywhere
 * (including the lock screen, per Android's own per-tile lock-screen setting) without opening
 * this app at all. Tapping it launches VoiceCaptureActivity, a translucent Activity that
 * immediately starts listening - see that class's kdoc for the capture/log/speak-back flow.
 *
 * No new runtime permission needed: launching the system speech-recognizer via Intent (what
 * VoiceCaptureActivity does) doesn't require this app to hold RECORD_AUDIO itself, same as the
 * existing voice input in Search/Assistant.
 */
class VoiceCaptureTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, VoiceCaptureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        // startActivityAndCollapse(Intent) was deprecated in API 34 in favor of the
        // PendingIntent overload - both are kept here since this app's minSdk (26) is well below
        // that.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                android.app.PendingIntent.getActivity(
                    this, 0, intent,
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = "Log by voice"
            updateTile()
        }
    }
}
