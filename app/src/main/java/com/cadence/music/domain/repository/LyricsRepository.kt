package com.cadence.music.domain.repository

import com.cadence.music.domain.model.Lyrics
import com.cadence.music.util.Resource

interface LyricsRepository {
    /** Cached-first: Room copy is returned instantly if present, network is a fallback. */
    suspend fun getLyrics(songId: String, title: String, artistName: String, albumName: String?, durationSec: Int): Resource<Lyrics?>

    suspend fun saveManualLyrics(lyrics: Lyrics): Resource<Unit>

    /** Overwrites just the per-line timestamps produced by the manual sync editor. */
    suspend fun updateLineTimings(songId: String, lines: List<Pair<Int, Long?>>): Resource<Unit>

    suspend fun hasManualOverride(songId: String): Boolean
}
