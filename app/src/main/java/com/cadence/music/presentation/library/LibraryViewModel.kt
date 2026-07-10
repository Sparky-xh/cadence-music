package com.cadence.music.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.Playlist
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.AuthRepository
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val savedSongs: List<Song> = emptyList(),
    val collections: List<Playlist> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val authRepository: AuthRepository,
    val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            if (user == null) return@launch
            combine(
                musicRepository.observeSavedSongs(),
                musicRepository.observeCustomCollections(user.id)
            ) { saved, collections -> LibraryUiState(saved, collections) }
                .collect { _uiState.value = it }
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch
            musicRepository.createCollection(user.id, name)
        }
    }

    fun playSaved(song: Song) {
        val songs = _uiState.value.savedSongs
        playerController.playQueue(songs, songs.indexOf(song).coerceAtLeast(0))
    }
}
