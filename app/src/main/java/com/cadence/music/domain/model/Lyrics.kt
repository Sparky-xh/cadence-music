package com.cadence.music.domain.model

/** A single line of lyrics. [startTimeMs] is null for plain/unsynced text. */
data class LyricLine(
    val startTimeMs: Long?,
    val text: String
)

enum class LyricsSource { FETCHED, MANUAL, CREATOR_PROVIDED }

data class Lyrics(
    val songId: String,
    val lines: List<LyricLine>,
    val isSynced: Boolean,
    val source: LyricsSource,
    val lastEditedAt: Long = System.currentTimeMillis()
) {
    val plainText: String get() = lines.joinToString("\n") { it.text }
}
