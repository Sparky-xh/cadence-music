package com.cadence.music.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cadence.music.domain.model.PlaybackState
import com.cadence.music.domain.model.RepeatMode
import com.cadence.music.domain.model.Song
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide facade over a [MediaController] connected to [CadencePlaybackService]. Every screen
 * that touches playback (mini player, full player, lyrics auto-scroll, sleep timer) reads from
 * [state] rather than holding an ExoPlayer/MediaController reference directly, which keeps
 * Compose screens simple and makes the whole playback layer mockable in tests.
 */
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    private var queueSongs: List<Song> = emptyList()

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(context, ComponentName(context, CadencePlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            controller = future.get()
            attachListener()
        }, MoreExecutors.directExecutor())
    }

    private fun attachListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(isBuffering = playbackState == Player.STATE_BUFFERING)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller?.currentMediaItemIndex ?: -1
                val song = queueSongs.getOrNull(index)
                _state.value = _state.value.copy(
                    currentSong = song, queueIndex = index, durationMs = controller?.duration?.coerceAtLeast(0) ?: 0
                )
            }
        })
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        queueSongs = songs
        val items = songs.map { it.toMediaItem() }
        controller?.setMediaItems(items, startIndex, 0L)
        controller?.prepare()
        controller?.play()
        _state.value = _state.value.copy(
            queue = songs, queueIndex = startIndex, currentSong = songs.getOrNull(startIndex), isPlaying = true
        )
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun skipToNext() = controller?.seekToNextMediaItem()
    fun skipToPrevious() = controller?.seekToPreviousMediaItem()

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    /** Relative seek used by the double-tap gesture zones on the player screen. */
    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        val target = (c.currentPosition + deltaMs).coerceIn(0, c.duration.coerceAtLeast(0))
        c.seekTo(target)
    }

    /** Polled position for the progress bar + lyrics highlighter; Media3 has no continuous
     *  position-changed callback by design, so callers should poll this on a ~200ms ticker. */
    fun currentPositionMs(): Long = controller?.currentPosition ?: 0

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    /** Used by the hold-to-fast-forward gesture: bump speed while held, restore on release. */
    fun temporarilyBoostSpeed(boosted: Boolean, normalSpeed: Float, boostedSpeed: Float) {
        setPlaybackSpeed(if (boosted) boostedSpeed else normalSpeed)
    }

    fun toggleShuffle() {
        val enabled = !_state.value.isShuffleEnabled
        controller?.shuffleModeEnabled = enabled
        _state.value = _state.value.copy(isShuffleEnabled = enabled)
    }

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        controller?.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        _state.value = _state.value.copy(repeatMode = next)
    }

    fun startSleepTimer(minutes: Int, scope: kotlinx.coroutines.CoroutineScope) {
        sleepTimerJob?.cancel()
        val endsAt = System.currentTimeMillis() + minutes * 60_000L
        _state.value = _state.value.copy(sleepTimerEndsAtMs = endsAt)
        sleepTimerJob = scope.launch {
            kotlinx.coroutines.delay(minutes * 60_000L)
            controller?.pause()
            _state.value = _state.value.copy(sleepTimerEndsAtMs = null)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.value = _state.value.copy(sleepTimerEndsAtMs = null)
    }

    private fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(localFilePath ?: streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistName)
                .setArtworkUri(coverArtUrl?.let(android.net.Uri::parse))
                .build()
        )
        .build()
}
