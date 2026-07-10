package com.cadence.music.data.remote.firebase

import com.cadence.music.data.remote.dto.UserDto
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around FirebaseAuth + the /users/{uid} Firestore document. Kept separate from
 * AuthRepositoryImpl so the repository layer stays testable without booting real Firebase.
 *
 * Note on "Instagram login": Meta deprecated the old Instagram Basic Display / Instagram Login
 * API for general third-party consumer auth; Instagram no longer offers a standalone OAuth
 * login button the way Google/Facebook do. In practice, users who want to use their Instagram
 * identity sign in with Facebook (same underlying Meta account system) or via the Instagram
 * Graph API, which is scoped to business/creator accounts and content permissions rather than
 * general login. We surface Facebook Login prominently and treat "Instagram" as an alias/entry
 * point into the same flow — see presentation/auth/LoginScreen.kt.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun authStateChanges(): Flow<UserDto?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { doc -> trySend(doc.toObject(UserDto::class.java)) }
                    .addOnFailureListener { trySend(null) }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String, username: String): UserDto {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        val dto = UserDto(id = uid, username = username, email = email)
        firestore.collection("users").document(uid).set(dto).await()
        return dto
    }

    suspend fun signIn(email: String, password: String): UserDto {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        return firestore.collection("users").document(uid).get().await()
            .toObject(UserDto::class.java) ?: UserDto(id = uid, email = email, username = email.substringBefore("@"))
    }

    suspend fun signInWithGoogleIdToken(idToken: String): UserDto {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return upsertProfileFromFirebaseUser(result.user!!)
    }

    suspend fun signInWithFacebookToken(accessToken: String): UserDto {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val result = auth.signInWithCredential(credential).await()
        return upsertProfileFromFirebaseUser(result.user!!)
    }

    private suspend fun upsertProfileFromFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser): UserDto {
        val ref = firestore.collection("users").document(firebaseUser.uid)
        val existing = ref.get().await().toObject(UserDto::class.java)
        if (existing != null) return existing

        val dto = UserDto(
            id = firebaseUser.uid,
            username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "listener",
            email = firebaseUser.email.orEmpty(),
            profilePhotoUrl = firebaseUser.photoUrl?.toString()
        )
        ref.set(dto).await()
        return dto
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() = auth.signOut()
}
