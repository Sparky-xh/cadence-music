package com.cadence.music.playback

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service that owns the actual ExoPlayer instance + MediaSession. Because it extends
 * MediaSessionService, Android automatically surfaces controls in the notification shade, lock
 * screen, Bluetooth head units, and Android Auto/Wear without any extra code here — Media3
 * generates that UI from session metadata (see [PlayerController.updateNowPlayingMetadata]).
 */
@AndroidEntryPoint
class CadencePlaybackService : MediaSessionService() {

    @Inject lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        exoPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true // auto pause/duck on calls, other media apps, etc.
        )
        exoPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "cadence_playback"
    }
}
