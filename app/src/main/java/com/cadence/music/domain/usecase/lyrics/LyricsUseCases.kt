package com.cadence.music.domain.usecase.lyrics

import com.cadence.music.domain.model.Lyrics
import com.cadence.music.domain.model.LyricLine
import com.cadence.music.domain.model.LyricsSource
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.LyricsRepository
import com.cadence.music.util.Resource
import javax.inject.Inject

class FetchLyricsUseCase @Inject constructor(private val repo: LyricsRepository) {
    suspend operator fun invoke(song: Song): Resource<Lyrics?> = repo.getLyrics(
        songId = song.id,
        title = song.title,
        artistName = song.artistName,
        albumName = song.albumName,
        durationSec = (song.durationMs / 1000).toInt()
    )
}

/** Used by both "add lyrics from scratch" and "edit existing lyrics" flows. */
class SaveManualLyricsUseCase @Inject constructor(private val repo: LyricsRepository) {
    suspend operator fun invoke(songId: String, rawText: String): Resource<Unit> {
        val lines = rawText.lines().map { LyricLine(startTimeMs = null, text = it) }
        return repo.saveManualLyrics(Lyrics(songId, lines, isSynced = false, source = LyricsSource.MANUAL))
    }
}

/** Persists timestamps captured by tapping "mark" on each line during playback in the sync editor. */
class SyncLyricsTimingUseCase @Inject constructor(private val repo: LyricsRepository) {
    suspend operator fun invoke(songId: String, lineIndexToTimestampMs: List<Pair<Int, Long?>>): Resource<Unit> =
        repo.updateLineTimings(songId, lineIndexToTimestampMs)
}
