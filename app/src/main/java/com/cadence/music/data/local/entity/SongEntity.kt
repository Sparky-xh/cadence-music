package com.cadence.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local cache of song metadata so the app has something to show offline / on flaky networks. */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumName: String?,
    val coverArtUrl: String?,
    val durationMs: Long,
    val streamUrl: String?,
    val genres: List<String>,
    val isCreatorUpload: Boolean,
    val uploaderId: String?,
    val playCount: Long,
    val releaseDate: Long?,
    val isSaved: Boolean = false,
    val cachedAtMs: Long = System.currentTimeMillis()
)
