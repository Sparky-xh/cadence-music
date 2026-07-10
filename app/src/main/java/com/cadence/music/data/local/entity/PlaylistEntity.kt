package com.cadence.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerId: String,
    val coverArtUrl: String?,
    val songIds: List<String>,
    val isSystemCollection: Boolean
)
