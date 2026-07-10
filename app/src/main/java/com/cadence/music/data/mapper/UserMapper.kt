package com.cadence.music.data.mapper

import com.cadence.music.data.remote.dto.UserDto
import com.cadence.music.domain.model.SubscriptionTier
import com.cadence.music.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id, username = username, email = email, profilePhotoUrl = profilePhotoUrl,
    favoriteGenreIds = favoriteGenreIds, favoriteArtistIds = favoriteArtistIds,
    subscriptionTier = runCatching { SubscriptionTier.valueOf(subscriptionTier) }.getOrDefault(SubscriptionTier.FREE),
    hasCompletedOnboarding = hasCompletedOnboarding
)

fun User.toDto(): UserDto = UserDto(
    id = id, username = username, email = email, profilePhotoUrl = profilePhotoUrl,
    favoriteGenreIds = favoriteGenreIds, favoriteArtistIds = favoriteArtistIds,
    subscriptionTier = subscriptionTier.name, hasCompletedOnboarding = hasCompletedOnboarding
)
