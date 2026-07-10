package com.cadence.music.data.repository

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import com.cadence.music.data.local.dao.DownloadDao
import com.cadence.music.data.local.dao.SongDao
import com.cadence.music.data.local.entity.DownloadEntity
import com.cadence.music.data.mapper.toDomain
import com.cadence.music.data.mapper.toEntity
import com.cadence.music.domain.model.DownloadItem
import com.cadence.music.domain.model.DownloadStatus
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.DownloadRepository
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges our domain-level [DownloadItem] model to Media3s [DownloadManager], which owns the
 * actual file transfer, retry-on-reconnect, and foreground-notification plumbing (see
 * playback/DownloadUtil.kt for how the DownloadManager singleton + cache are configured). Room
 * mirrors DownloadManagers state so the rest of the app can just observe a Flow<List<DownloadItem>>
 * without touching Media3 types.
 */
@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadDao: DownloadDao,
    private val songDao: SongDao
) : DownloadRepository {

    override fun observeDownloads(): Flow<List<DownloadItem>> = downloadDao.observeAll().map { entities ->
        entities.mapNotNull { entity ->
            songDao.getById(entity.songId)?.let { song ->
                DownloadItem(
                    song = song.toDomain(localFilePath = entity.localFilePath),
                    status = runCatching { DownloadStatus.valueOf(entity.status) }.getOrDefault(DownloadStatus.NOT_DOWNLOADED),
                    progressPercent = entity.progressPercent,
                    bytesDownloaded = entity.bytesDownloaded,
                    totalBytes = entity.totalBytes
                )
            }
        }
    }

    override fun observeDownload(songId: String): Flow<DownloadItem?> = downloadDao.observeOne(songId).map { entity ->
        entity ?: return@map null
        val song = songDao.getById(songId) ?: return@map null
        DownloadItem(
            song = song.toDomain(localFilePath = entity.localFilePath),
            status = runCatching { DownloadStatus.valueOf(entity.status) }.getOrDefault(DownloadStatus.NOT_DOWNLOADED),
            progressPercent = entity.progressPercent,
            bytesDownloaded = entity.bytesDownloaded,
            totalBytes = entity.totalBytes
        )
    }

    override suspend fun enqueueDownload(song: Song): Resource<Unit> = runCatching {
        val uri = requireNotNull(song.streamUrl?.let(android.net.Uri::parse)) { "No stream URL to download" }
        val request = DownloadRequest.Builder(song.id, uri)
            .setMimeType(androidx.media3.common.MimeTypes.AUDIO_MP4)
            .build()
        songDao.upsert(song.toEntity())
        downloadDao.upsert(
            DownloadEntity(
                songId = song.id, localFilePath = null, status = DownloadStatus.QUEUED.name,
                progressPercent = 0, bytesDownloaded = 0, totalBytes = 0,
                queuedAtMs = System.currentTimeMillis()
            )
        )
        downloadManager.addDownload(request)
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not start download") })

    override suspend fun pauseDownload(songId: String) {
        downloadManager.setStopReason(songId, Download.STATE_STOPPED)
        downloadDao.upsert(downloadEntityWithStatus(songId, DownloadStatus.PAUSED))
    }

    override suspend fun resumeDownload(songId: String) {
        downloadManager.setStopReason(songId, Download.STOP_REASON_NONE)
        downloadDao.upsert(downloadEntityWithStatus(songId, DownloadStatus.DOWNLOADING))
    }

    override suspend fun removeDownload(songId: String): Resource<Unit> = runCatching {
        downloadManager.removeDownload(songId)
        downloadDao.delete(songId)
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not remove download") })

    override suspend fun totalStorageUsedBytes(): Long = downloadDao.totalBytesUsed()

    override suspend fun clearAllDownloads(): Resource<Unit> = runCatching {
        downloadManager.currentDownloads.forEach { downloadManager.removeDownload(it.request.id) }
        downloadDao.clearAll()
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not clear downloads") })

    private suspend fun downloadEntityWithStatus(songId: String, status: DownloadStatus): DownloadEntity {
        val existing = downloadDao.getOnce(songId)
        // Preserve known progress/bytes when just flipping pause/resume state; DownloadUtil's
        // DownloadManager.Listener (registered at app start) is the source of truth for
        // progress and will overwrite this again on its next callback.
        return existing?.copy(status = status.name)
            ?: DownloadEntity(songId, null, status.name, 0, 0, 0, System.currentTimeMillis())
    }
}
