package com.cadence.music.domain.model

enum class RepeatMode { OFF, ONE, ALL }

/** Snapshot of everything the UI (mini player, full player, lyrics screen) needs to render. */
data class PlaybackState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val playbackSpeed: Float = 1f,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerEndsAtMs: Long? = null
)

data class CreatorAnalytics(
    val songId: String,
    val title: String,
    val totalPlays: Long,
    val uniqueListeners: Long,
    val downloads: Long,
    val plays7d: List<Long> // 7 buckets, oldest first, for a tiny sparkline
)
