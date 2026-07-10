package com.cadence.music.domain.usecase.auth

import com.cadence.music.domain.model.User
import com.cadence.music.domain.repository.AuthRepository
import com.cadence.music.util.Resource
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, username: String): Resource<User> {
        if (username.isBlank()) return Resource.Error("Username cannot be empty")
        if (password.length < 8) return Resource.Error("Password must be at least 8 characters")
        return repo.signUpWithEmail(email.trim(), password, username.trim())
    }
}

class SignInWithEmailUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> =
        repo.signInWithEmail(email.trim(), password)
}

class SignInWithGoogleUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(idToken: String): Resource<User> = repo.signInWithGoogle(idToken)
}

class SignInWithFacebookUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(accessToken: String): Resource<User> = repo.signInWithFacebook(accessToken)
}

class SignOutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.signOut()
}
