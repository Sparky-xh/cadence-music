package com.cadence.music.data.repository

import com.cadence.music.data.mapper.toDomain
import com.cadence.music.domain.model.SubscriptionTier
import com.cadence.music.domain.model.User
import com.cadence.music.domain.repository.UserRepository
import com.cadence.music.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    private fun userDoc() = firestore.collection("users").document(auth.currentUser!!.uid)

    override suspend fun updateProfile(username: String, profilePhotoUrl: String?): Resource<User> = runCatching {
        val updates = mutableMapOf<String, Any>("username" to username)
        profilePhotoUrl?.let { updates["profilePhotoUrl"] = it }
        userDoc().update(updates).await()
        userDoc().get().await().toObject(com.cadence.music.data.remote.dto.UserDto::class.java)!!.toDomain()
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Could not update profile") })

    override suspend fun setFavoriteGenres(genreIds: List<String>): Resource<Unit> = runCatching {
        userDoc().update("favoriteGenreIds", genreIds).await()
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not save genres") })

    override suspend fun setFavoriteArtists(artistIds: List<String>): Resource<Unit> = runCatching {
        userDoc().update("favoriteArtistIds", artistIds).await()
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not save artists") })

    override suspend fun markOnboardingComplete(): Resource<Unit> = runCatching {
        userDoc().update("hasCompletedOnboarding", true).await()
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not update onboarding state") })

    override suspend fun upgradeSubscription(tier: SubscriptionTier): Resource<Unit> = runCatching {
        // In production this should be driven by a verified Play Billing purchase callback /
        // server-side receipt validation, not called directly from the client. Wired here as a
        // stub so the Premium screen has something real to call during development.
        userDoc().update("subscriptionTier", tier.name).await()
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not update subscription") })
}
