package com.cadence.music.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** [linesJson] stores a JSON-encoded List<LyricLine> — see data/mapper/LyricsMapper.kt. Keeping
 *  lyrics as one blob column (rather than a child table) keeps "load lyrics for song" a single
 *  indexed read, which matters because it happens on every song change during playback. */
@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val songId: String,
    val linesJson: String,
    val isSynced: Boolean,
    val source: String,
    val isManualOverride: Boolean,
    val lastEditedAt: Long
)
