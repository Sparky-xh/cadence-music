package com.cadence.music.domain.model

data class Artist(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val isIndependent: Boolean = false, // true for creator-tool artist profiles
    val followerCount: Long = 0,
    val monthlyListeners: Long = 0
)

data class Genre(
    val id: String,
    val displayName: String,
    val accentColorHex: String // lets each genre "shelf" on Home tint slightly differently
)
