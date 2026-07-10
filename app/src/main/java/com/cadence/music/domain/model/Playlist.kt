package com.cadence.music.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val ownerId: String,
    val coverArtUrl: String? = null,
    val songIds: List<String> = emptyList(),
    val isSystemCollection: Boolean = false // e.g. auto "Saved Songs", not user-deletable
)
