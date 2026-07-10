package com.cadence.music.data.local.entity

import androidx.room.Entity

/** Composite key on (songId, playedAt): the same song can legitimately appear many times. */
@Entity(tableName = "history", primaryKeys = ["songId", "playedAt"])
data class HistoryEntity(
    val songId: String,
    val playedAt: Long,
    val msPlayed: Long
)
