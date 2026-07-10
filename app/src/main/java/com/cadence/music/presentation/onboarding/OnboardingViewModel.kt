package com.cadence.music.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.Genre
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.domain.repository.UserRepository
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val genres: List<Genre> = emptyList(),
    val selectedGenreIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = musicRepository.getAvailableGenres()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(genres = result.data)
                else -> Unit
            }
        }
    }

    fun toggleGenre(genreId: String) {
        val current = _uiState.value.selectedGenreIds
        _uiState.value = _uiState.value.copy(
            selectedGenreIds = if (genreId in current) current - genreId else current + genreId
        )
    }

    fun finishOnboarding() {
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            userRepository.setFavoriteGenres(_uiState.value.selectedGenreIds.toList())
            userRepository.markOnboardingComplete()
            _uiState.value = _uiState.value.copy(isSaving = false, isComplete = true)
        }
    }
}
