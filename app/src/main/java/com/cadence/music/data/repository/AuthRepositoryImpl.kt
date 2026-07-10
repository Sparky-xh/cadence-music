package com.cadence.music.data.repository

import com.cadence.music.data.mapper.toDomain
import com.cadence.music.data.remote.firebase.FirebaseAuthDataSource
import com.cadence.music.domain.model.User
import com.cadence.music.domain.repository.AuthRepository
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remote: FirebaseAuthDataSource
) : AuthRepository {

    override val currentUser: Flow<User?> = remote.authStateChanges().map { it?.toDomain() }

    override suspend fun signUpWithEmail(email: String, password: String, username: String): Resource<User> =
        runCatching { remote.signUp(email, password, username).toDomain() }
            .fold(onSuccess = { Resource.Success(it) }, onFailure = { it.toAuthError() })

    override suspend fun signInWithEmail(email: String, password: String): Resource<User> =
        runCatching { remote.signIn(email, password).toDomain() }
            .fold(onSuccess = { Resource.Success(it) }, onFailure = { it.toAuthError() })

    override suspend fun signInWithGoogle(idToken: String): Resource<User> =
        runCatching { remote.signInWithGoogleIdToken(idToken).toDomain() }
            .fold(onSuccess = { Resource.Success(it) }, onFailure = { it.toAuthError() })

    override suspend fun signInWithFacebook(accessToken: String): Resource<User> =
        runCatching { remote.signInWithFacebookToken(accessToken).toDomain() }
            .fold(onSuccess = { Resource.Success(it) }, onFailure = { it.toAuthError() })

    override suspend fun sendPasswordReset(email: String): Resource<Unit> =
        runCatching { remote.sendPasswordReset(email) }
            .fold(onSuccess = { Resource.Success(Unit) }, onFailure = { it.toAuthError() })

    override suspend fun signOut() = remote.signOut()

    private fun <T> Throwable.toAuthError(): Resource<T> = Resource.Error(
        message = message ?: "Authentication failed. Please try again.",
        throwable = this
    )
}
