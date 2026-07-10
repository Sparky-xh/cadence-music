package com.cadence.music.presentation.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.data.remote.lyrics.LrcParser
import com.cadence.music.domain.model.Lyrics
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.usecase.lyrics.FetchLyricsUseCase
import com.cadence.music.domain.usecase.lyrics.SaveManualLyricsUseCase
import com.cadence.music.domain.usecase.lyrics.SyncLyricsTimingUseCase
import com.cadence.music.playback.PlayerController
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LyricsUiState(
    val song: Song? = null,
    val lyrics: Lyrics? = null,
    val currentLineIndex: Int = -1,
    val positionMs: Long = 0,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

@HiltViewModel
class LyricsViewModel @Inject constructor(
    val playerController: PlayerController,
    private val fetchLyrics: FetchLyricsUseCase,
    private val saveManualLyrics: SaveManualLyricsUseCase,
    private val syncLyricsTiming: SyncLyricsTimingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LyricsUiState())
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    private var loadedForSongId: String? = null

    init {
        viewModelScope.launch {
            while (isActive) {
                val playback = playerController.state.value
                val position = playerController.currentPositionMs()
                val song = playback.currentSong

                if (song != null && song.id != loadedForSongId) {
                    loadedForSongId = song.id
                    loadLyrics(song)
                }

                val lyrics = _uiState.value.lyrics
                val lineIndex = if (lyrics != null) LrcParser.currentLineIndex(lyrics.lines, position) else -1
                _uiState.value = _uiState.value.copy(song = song, positionMs = position, currentLineIndex = lineIndex)

                delay(200)
            }
        }
    }

    private fun loadLyrics(song: Song) {
        _uiState.value = _uiState.value.copy(isLoading = true, notFound = false, lyrics = null)
        viewModelScope.launch {
            when (val result = fetchLyrics(song)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    lyrics = result.data, isLoading = false, notFound = result.data == null
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, notFound = true)
                Resource.Loading -> Unit
            }
        }
    }

    fun saveManualText(rawText: String) {
        val songId = _uiState.value.song?.id ?: return
        viewModelScope.launch {
            saveManualLyrics(songId, rawText)
            _uiState.value.song?.let { loadedForSongId = null } // force reload with the new manual lyrics
        }
    }

    fun saveLineTimings(timings: List<Pair<Int, Long?>>) {
        val songId = _uiState.value.song?.id ?: return
        viewModelScope.launch {
            syncLyricsTiming(songId, timings)
            loadedForSongId = null
        }
    }
}
