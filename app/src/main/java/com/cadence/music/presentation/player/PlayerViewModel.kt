package com.cadence.music.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.PlaybackState
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.domain.usecase.downloads.DownloadSongUseCase
import com.cadence.music.playback.PlayerController
import com.cadence.music.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerController: PlayerController,
    private val musicRepository: MusicRepository,
    private val downloadSong: DownloadSongUseCase
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerController.state

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        // Media3 does not push continuous position updates, so we poll at a smooth-enough
        // interval for both the seek bar and the lyrics-line highlighter that reads this too.
        viewModelScope.launch {
            while (isActive) {
                _positionMs.value = playerController.currentPositionMs()
                delay(200)
            }
        }
        viewModelScope.launch {
            playbackState.collect { state ->
                state.currentSong?.let { _isSaved.value = musicRepository.isSaved(it.id) }
            }
        }
    }

    fun toggleSave() {
        val song = playbackState.value.currentSong ?: return
        viewModelScope.launch {
            musicRepository.toggleSaved(song)
            _isSaved.value = musicRepository.isSaved(song.id)
        }
    }

    fun download(isPremiumOrAbove: Boolean) {
        val song = playbackState.value.currentSong ?: return
        viewModelScope.launch { downloadSong(song, isPremiumOrAbove) }
    }

    fun startSleepTimer(minutes: Int) = playerController.startSleepTimer(minutes, viewModelScope)
    fun cancelSleepTimer() = playerController.cancelSleepTimer()
}
