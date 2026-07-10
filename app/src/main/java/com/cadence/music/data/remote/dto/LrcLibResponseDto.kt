package com.cadence.music.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response shape from GET https://lrclib.net/api/get (a free, key-less lyrics API). Confirmed
 * against https://lrclib.net/docs — re-check before shipping in case the schema changes.
 *
 * Example:
 * {
 *   "id": 151738, "trackName": "The Chain", "artistName": "Fleetwood Mac",
 *   "albumName": "Rumours", "duration": 271, "instrumental": false,
 *   "plainLyrics": "Listen to the wind blow\n...",
 *   "syncedLyrics": "[00:27.93] Listen to the wind blow\n..."
 * }
 */
@Serializable
data class LrcLibResponseDto(
    val id: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Int? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)
