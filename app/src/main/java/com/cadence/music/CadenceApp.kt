package com.cadence.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.media3.exoplayer.offline.DownloadService
import com.cadence.music.playback.CadencePlaybackService
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Hilt builds the whole dependency graph from here (see the di/
 * package). We also set up the two notification channels the app can post to:
 *  - playback: the persistent "now playing" media-style notification (Media3 manages content,
 *    we just need the channel to exist below Android O).
 *  - downloads: progress/completion notifications for the offline-download queue.
 */
@HiltAndroidApp
class CadenceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                CadencePlaybackService.PLAYBACK_CHANNEL_ID,
                getString(R.string.app_name) + " playback",
                NotificationManager.IMPORTANCE_LOW // low: no sound/heads-up for a persistent player
            )
        )

        manager.createNotificationChannel(
            NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "cadence_downloads"
    }
}
