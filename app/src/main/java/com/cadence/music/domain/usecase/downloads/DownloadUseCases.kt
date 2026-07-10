package com.cadence.music.domain.usecase.downloads

import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.DownloadRepository
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.util.Constants
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DownloadSongUseCase @Inject constructor(
    private val downloadRepo: DownloadRepository,
    private val musicRepo: MusicRepository
) {
    suspend operator fun invoke(song: Song, isPremiumOrAbove: Boolean): Resource<Unit> {
        if (!song.isPlayable) return Resource.Error("This song has no playable source yet")
        if (!isPremiumOrAbove) {
            val currentCount = downloadRepo.observeDownloads().first().size
            if (currentCount >= Constants.FREE_DOWNLOAD_LIMIT) {
                return Resource.Error("Free plan allows ${Constants.FREE_DOWNLOAD_LIMIT} downloads. Upgrade to Premium for unlimited offline songs.")
            }
        }
        return downloadRepo.enqueueDownload(song)
    }
}

class RemoveDownloadUseCase @Inject constructor(private val repo: DownloadRepository) {
    suspend operator fun invoke(songId: String): Resource<Unit> = repo.removeDownload(songId)
}
