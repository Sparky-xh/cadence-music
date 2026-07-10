package com.cadence.music.presentation.creator

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.CreatorAnalytics
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.AuthRepository
import com.cadence.music.domain.repository.NewSongDraft
import com.cadence.music.domain.usecase.creator.GetCreatorAnalyticsUseCase
import com.cadence.music.domain.usecase.creator.UploadSongUseCase
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatorDashboardUiState(
    val isLoading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val analytics: List<CreatorAnalytics> = emptyList()
)

@HiltViewModel
class CreatorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getAnalytics: GetCreatorAnalyticsUseCase,
    private val uploadSongUseCase: UploadSongUseCase,
    private val creatorRepository: com.cadence.music.domain.repository.CreatorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatorDashboardUiState())
    val uiState: StateFlow<CreatorDashboardUiState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<Song>?>(null)
    val uploadState: StateFlow<Resource<Song>?> = _uploadState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val songsResult = creatorRepository.getMySongs(user.id)
            val analyticsResult = getAnalytics(user.id)
            _uiState.value = CreatorDashboardUiState(
                isLoading = false,
                songs = (songsResult as? Resource.Success)?.data.orEmpty(),
                analytics = (analyticsResult as? Resource.Success)?.data.orEmpty()
            )
        }
    }

    fun uploadSong(title: String, albumName: String?, genres: List<String>, audioUri: Uri, coverUri: Uri?, lyricsText: String?) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            val draft = NewSongDraft(title, albumName, genres, audioUri, coverUri, lyricsText)
            uploadSongUseCase(user.id, draft).collect { result ->
                _uploadState.value = result
                if (result is Resource.Success) loadDashboard()
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = null
    }
}
