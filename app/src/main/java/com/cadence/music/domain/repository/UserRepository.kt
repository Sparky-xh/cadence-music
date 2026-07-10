package com.cadence.music.domain.repository

import com.cadence.music.domain.model.User
import com.cadence.music.util.Resource

interface UserRepository {
    suspend fun updateProfile(username: String, profilePhotoUrl: String?): Resource<User>
    suspend fun setFavoriteGenres(genreIds: List<String>): Resource<Unit>
    suspend fun setFavoriteArtists(artistIds: List<String>): Resource<Unit>
    suspend fun markOnboardingComplete(): Resource<Unit>
    suspend fun upgradeSubscription(tier: com.cadence.music.domain.model.SubscriptionTier): Resource<Unit>
}
