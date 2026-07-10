package com.cadence.music.data.remote.dto

data class UserDto(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePhotoUrl: String? = null,
    val favoriteGenreIds: List<String> = emptyList(),
    val favoriteArtistIds: List<String> = emptyList(),
    val subscriptionTier: String = "FREE",
    val hasCompletedOnboarding: Boolean = false
)
