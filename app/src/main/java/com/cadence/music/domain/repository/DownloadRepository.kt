package com.cadence.music.domain.repository

import com.cadence.music.domain.model.DownloadItem
import com.cadence.music.domain.model.Song
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun observeDownloads(): Flow<List<DownloadItem>>
    fun observeDownload(songId: String): Flow<DownloadItem?>
    suspend fun enqueueDownload(song: Song): Resource<Unit>
    suspend fun pauseDownload(songId: String)
    suspend fun resumeDownload(songId: String)
    suspend fun removeDownload(songId: String): Resource<Unit>
    suspend fun totalStorageUsedBytes(): Long
    suspend fun clearAllDownloads(): Resource<Unit>
}
