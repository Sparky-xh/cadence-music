package com.cadence.music.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.HistoryEntry
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.AuthRepository
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.domain.usecase.music.GetRecommendedSongsUseCase
import com.cadence.music.domain.usecase.music.RecordPlayUseCase
import com.cadence.music.playback.PlayerController
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recommended: List<Song> = emptyList(),
    val recentlyPlayed: List<HistoryEntry> = emptyList(),
    val savedSongs: List<Song> = emptyList(),
    val username: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommended: GetRecommendedSongsUseCase,
    private val musicRepository: MusicRepository,
    private val authRepository: AuthRepository,
    private val recordPlay: RecordPlayUseCase,
    val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        playerController.connect()
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val recommendedResult = getRecommended(user?.favoriteGenreIds.orEmpty(), user?.favoriteArtistIds.orEmpty())
            val recommended = (recommendedResult as? Resource.Success)?.data.orEmpty()

            combine(
                musicRepository.observeRecentlyPlayed(limit = 10),
                musicRepository.observeSavedSongs()
            ) { recent, saved -> recent to saved }.collect { (recent, saved) ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    recommended = recommended,
                    recentlyPlayed = recent,
                    savedSongs = saved,
                    username = user?.username.orEmpty()
                )
            }
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        val startIndex = queue.indexOf(song).coerceAtLeast(0)
        playerController.playQueue(queue, startIndex)
        viewModelScope.launch { recordPlay(song, 0) }
    }
}
