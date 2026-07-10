package com.cadence.music.domain.model

/**
 * Core playable unit. [streamUrl] and [localFilePath] are mutually complementary: a song can
 * have either, both (streamed, then also downloaded), or briefly neither (creator upload still
 * processing). [PlayerController] always prefers [localFilePath] when present so offline
 * playback and downloaded playback share one code path.
 */
data class Song(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumName: String? = null,
    val coverArtUrl: String? = null,
    val durationMs: Long,
    val streamUrl: String? = null,
    val localFilePath: String? = null,
    val genres: List<String> = emptyList(),
    val isCreatorUpload: Boolean = false,
    val uploaderId: String? = null,
    val playCount: Long = 0,
    val releaseDate: Long? = null
) {
    val isDownloaded: Boolean get() = localFilePath != null
    val isPlayable: Boolean get() = streamUrl != null || localFilePath != null
}

enum class DownloadStatus { NOT_DOWNLOADED, QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }

data class DownloadItem(
    val song: Song,
    val status: DownloadStatus,
    val progressPercent: Int = 0,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0
)
