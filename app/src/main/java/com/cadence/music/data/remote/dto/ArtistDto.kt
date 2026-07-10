package com.cadence.music.data.remote.dto

data class ArtistDto(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val isIndependent: Boolean = false,
    val followerCount: Long = 0,
    val monthlyListeners: Long = 0
)
