package com.cadence.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val localFilePath: String?,
    val status: String, // matches DownloadStatus.name
    val progressPercent: Int,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val queuedAtMs: Long
)
