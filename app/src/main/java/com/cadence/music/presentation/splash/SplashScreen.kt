package com.cadence.music.presentation.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    sealed class Destination { data object Login : Destination(); data object Onboarding : Destination(); data object Home : Destination() }

    var destination by mutableStateOf<Destination?>(null)
        private set

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            destination = when {
                user == null -> Destination.Login
                !user.hasCompletedOnboarding -> Destination.Onboarding
                else -> Destination.Home
            }
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.destination) {
        when (viewModel.destination) {
            SplashViewModel.Destination.Login -> onNavigateToLogin()
            SplashViewModel.Destination.Onboarding -> onNavigateToOnboarding()
            SplashViewModel.Destination.Home -> onNavigateToHome()
            null -> Unit
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Cadence", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
        }
    }
}
