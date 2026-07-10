package com.cadence.music.domain.repository

import com.cadence.music.domain.model.User
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Null when signed out. Backed by FirebaseAuth.addAuthStateListener under the hood. */
    val currentUser: Flow<User?>

    suspend fun signUpWithEmail(email: String, password: String, username: String): Resource<User>
    suspend fun signInWithEmail(email: String, password: String): Resource<User>

    /** [idToken] comes from Credential Managers GetGoogleIdOption, not the old GoogleSignInClient. */
    suspend fun signInWithGoogle(idToken: String): Resource<User>

    /** [accessToken] comes from the Facebook LoginManager callback. */
    suspend fun signInWithFacebook(accessToken: String): Resource<User>

    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun signOut()
}
