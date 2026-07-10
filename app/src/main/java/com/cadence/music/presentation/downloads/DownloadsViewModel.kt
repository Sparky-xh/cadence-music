package com.cadence.music.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.DownloadItem
import com.cadence.music.domain.repository.DownloadRepository
import com.cadence.music.domain.usecase.downloads.RemoveDownloadUseCase
import com.cadence.music.playback.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val removeDownload: RemoveDownloadUseCase,
    val playerController: PlayerController
) : ViewModel() {

    val downloads: StateFlow<List<DownloadItem>> = downloadRepository.observeDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalBytes = MutableStateFlow(0L)
    val totalBytes: StateFlow<Long> = _totalBytes

    init {
        viewModelScope.launch { _totalBytes.value = downloadRepository.totalStorageUsedBytes() }
    }

    fun remove(songId: String) = viewModelScope.launch { removeDownload(songId) }
    fun pause(songId: String) = viewModelScope.launch { downloadRepository.pauseDownload(songId) }
    fun resume(songId: String) = viewModelScope.launch { downloadRepository.resumeDownload(songId) }
    fun clearAll() = viewModelScope.launch { downloadRepository.clearAllDownloads() }

    fun playDownloaded(item: DownloadItem) {
        val completed = downloads.value.filter { it.status == com.cadence.music.domain.model.DownloadStatus.COMPLETED }
        playerController.playQueue(completed.map { it.song }, completed.indexOfFirst { it.song.id == item.song.id }.coerceAtLeast(0))
    }
}
