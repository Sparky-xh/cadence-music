package com.cadence.music.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.User
import com.cadence.music.domain.usecase.auth.*
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signUp: SignUpUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithFacebook: SignInWithFacebookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailLogin(email: String, password: String) = launchAuthCall { signInWithEmail(email, password) }

    fun onEmailSignup(email: String, password: String, username: String) =
        launchAuthCall { signUp(email, password, username) }

    fun onGoogleIdToken(idToken: String) = launchAuthCall { signInWithGoogle(idToken) }

    /** Also the handler wired to the "Continue with Instagram" button — see LoginScreen for why
     *  both buttons resolve to the same Facebook Login flow. */
    fun onFacebookAccessToken(token: String) = launchAuthCall { signInWithFacebook(token) }

    private fun launchAuthCall(block: suspend () -> Resource<User>) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = block()) {
                is Resource.Success -> _uiState.value = AuthUiState(loggedInUser = result.data)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
