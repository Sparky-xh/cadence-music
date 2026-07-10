package com.cadence.music.data.repository

import com.cadence.music.data.local.dao.LyricsDao
import com.cadence.music.data.mapper.toDomain
import com.cadence.music.data.mapper.toEntity
import com.cadence.music.data.mapper.toLyricLines
import com.cadence.music.data.remote.lyrics.LrcParser
import com.cadence.music.data.remote.lyrics.LyricsApiService
import com.cadence.music.domain.model.Lyrics
import com.cadence.music.domain.model.LyricsSource
import com.cadence.music.domain.repository.LyricsRepository
import com.cadence.music.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val api: LyricsApiService,
    private val dao: LyricsDao
) : LyricsRepository {

    override suspend fun getLyrics(
        songId: String, title: String, artistName: String, albumName: String?, durationSec: Int
    ): Resource<Lyrics?> {
        // 1) A manual override (user-authored or user-edited) always wins over fetched lyrics.
        dao.getForSong(songId)?.let { cached ->
            if (cached.isManualOverride) return Resource.Success(cached.toDomain())
        }

        // 2) Try the network; on any failure fall back to whatever is cached (fetched-and-cached
        //    previously, or nothing).
        return runCatching {
            val response = api.getLyrics(title, artistName, albumName, durationSec.takeIf { it > 0 })
            val body = response.body()
            if (!response.isSuccessful || body == null || body.instrumental) return@runCatching null

            val lines = when {
                !body.syncedLyrics.isNullOrBlank() -> LrcParser.parse(body.syncedLyrics)
                !body.plainLyrics.isNullOrBlank() -> LrcParser.parsePlain(body.plainLyrics)
                else -> return@runCatching null
            }
            val lyrics = Lyrics(
                songId = songId,
                lines = lines,
                isSynced = !body.syncedLyrics.isNullOrBlank(),
                source = LyricsSource.FETCHED
            )
            dao.upsert(lyrics.toEntity(isManualOverride = false))
            lyrics
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { Resource.Success(dao.getForSong(songId)?.toDomain()) } // graceful offline fallback
        )
    }

    override suspend fun saveManualLyrics(lyrics: Lyrics): Resource<Unit> = runCatching {
        dao.upsert(lyrics.toEntity(isManualOverride = true))
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not save lyrics") })

    override suspend fun updateLineTimings(songId: String, lines: List<Pair<Int, Long?>>): Resource<Unit> = runCatching {
        val existing = dao.getForSong(songId) ?: error("No lyrics to sync yet — add lyrics text first")
        val currentLines = existing.linesJson.toLyricLines().toMutableList()
        lines.forEach { (index, timestampMs) ->
            if (index in currentLines.indices) {
                currentLines[index] = currentLines[index].copy(startTimeMs = timestampMs)
            }
        }
        val updated = existing.toDomain().copy(lines = currentLines, isSynced = true)
        dao.upsert(updated.toEntity(isManualOverride = true))
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not save timing") })

    override suspend fun hasManualOverride(songId: String): Boolean = dao.hasManualOverride(songId) ?: false
}
