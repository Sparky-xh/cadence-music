package com.cadence.music.domain.model

enum class SubscriptionTier { FREE, PREMIUM, CREATOR }

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profilePhotoUrl: String? = null,
    val favoriteGenreIds: List<String> = emptyList(),
    val favoriteArtistIds: List<String> = emptyList(),
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val hasCompletedOnboarding: Boolean = false
) {
    val isPremiumOrAbove: Boolean get() = subscriptionTier != SubscriptionTier.FREE
    val canUseCreatorTools: Boolean get() = subscriptionTier == SubscriptionTier.CREATOR
}

data class HistoryEntry(
    val song: Song,
    val playedAt: Long,
    val msPlayed: Long
)
