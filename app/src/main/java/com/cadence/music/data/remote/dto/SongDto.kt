package com.cadence.music.data.remote.dto

/** Firestore document shape for /songs/{id}. Firestores Kotlin SDK maps documents to plain
 *  data classes via reflection (no @Serializable needed here — that annotation is reserved for
 *  the kotlinx.serialization path used by Retrofit/LyricsApiService). */
data class SongDto(
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val albumName: String? = null,
    val coverArtUrl: String? = null,
    val durationMs: Long = 0,
    val streamUrl: String? = null,
    val genres: List<String> = emptyList(),
    val isCreatorUpload: Boolean = false,
    val uploaderId: String? = null,
    val playCount: Long = 0,
    val releaseDate: Long? = null
)
