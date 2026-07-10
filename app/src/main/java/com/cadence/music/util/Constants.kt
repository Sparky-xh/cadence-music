package com.cadence.music.util

object Constants {
    const val DATABASE_NAME = "cadence_local.db"

    // Public, key-less lyrics API. See data/remote/lyrics/LyricsApiService.kt for the exact
    // shape; verify against https://lrclib.net/docs before shipping since third-party APIs change.
    const val LYRICS_API_BASE_URL = "https://lrclib.net/"

    const val MIN_SEEK_GESTURE_MS = 10_000L // double-tap rewind/forward step
    const val FAST_FORWARD_SPEED = 2.0f     // hold-to-speed-up factor
    const val NORMAL_SPEED = 1.0f

    val SLEEP_TIMER_PRESETS_MIN = listOf(5, 15, 30, 45, 60)
    val PLAYBACK_SPEED_OPTIONS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    const val FREE_DOWNLOAD_LIMIT = 25 // free tier cap; premium/creator = unlimited
}
