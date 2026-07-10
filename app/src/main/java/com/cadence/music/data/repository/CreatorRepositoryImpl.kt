package com.cadence.music.data.repository

import com.cadence.music.data.local.dao.SongDao
import com.cadence.music.data.mapper.toDomain
import com.cadence.music.data.mapper.toEntity
import com.cadence.music.data.remote.dto.SongDto
import com.cadence.music.data.remote.firebase.FirebaseStorageDataSource
import com.cadence.music.data.remote.firebase.FirestoreMusicDataSource
import com.cadence.music.data.remote.lyrics.LrcParser
import com.cadence.music.domain.model.CreatorAnalytics
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.CreatorRepository
import com.cadence.music.domain.repository.LyricsRepository
import com.cadence.music.domain.repository.NewSongDraft
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatorRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorageDataSource,
    private val firestoreMusic: FirestoreMusicDataSource,
    private val songDao: SongDao,
    private val lyricsRepository: LyricsRepository
) : CreatorRepository {

    override fun uploadSong(uploaderId: String, draft: NewSongDraft): Flow<Resource<Song>> = flow {
        emit(Resource.Loading)
        val songId = UUID.randomUUID().toString()

        val coverUrl = draft.coverArtUri?.let { storage.uploadCoverArt(uploaderId, songId, it) }

        var audioUrl: String? = null
        storage.uploadAudio(uploaderId, songId, draft.audioFileUri).collect { (_, url) ->
            if (url != null) audioUrl = url
        }
        val finalAudioUrl = audioUrl ?: run {
            emit(Resource.Error("Upload failed — no file URL returned"))
            return@flow
        }

        val dto = SongDto(
            id = songId,
            title = draft.title,
            artistId = uploaderId,
            artistName = "", // resolved from the creator profile on read; kept blank on write
            albumName = draft.albumName,
            coverArtUrl = coverUrl,
            durationMs = 0, // populated by a server-side Cloud Function that probes the uploaded
                            // audio (client-side duration probing before upload is also an option
                            // — see README "Stage 7" notes)
            streamUrl = finalAudioUrl,
            genres = draft.genres,
            isCreatorUpload = true,
            uploaderId = uploaderId,
            playCount = 0,
            releaseDate = System.currentTimeMillis()
        )
        firestoreMusic.createSong(dto)
        songDao.upsert(dto.toEntity())

        draft.lyricsPlainText?.takeIf { it.isNotBlank() }?.let { text ->
            val lines = LrcParser.parsePlain(text)
            lyricsRepository.saveManualLyrics(
                com.cadence.music.domain.model.Lyrics(
                    songId = songId, lines = lines, isSynced = false,
                    source = com.cadence.music.domain.model.LyricsSource.CREATOR_PROVIDED
                )
            )
        }

        emit(Resource.Success(dto.toDomain()))
    }

    override suspend fun getMySongs(uploaderId: String): Resource<List<Song>> = runCatching {
        firestoreMusic.getSongsByUploader(uploaderId).map { it.toDomain() }
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Could not load your songs") })

    override suspend fun deleteSong(songId: String): Resource<Unit> = runCatching {
        firestoreMusic.deleteSong(songId)
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not delete song") })

    override suspend fun getAnalytics(uploaderId: String): Resource<List<CreatorAnalytics>> = runCatching {
        // Play-count-over-time buckets need a dedicated /analytics collection written by a
        // scheduled Cloud Function; this returns totals-only until that pipeline exists.
        firestoreMusic.getSongsByUploader(uploaderId).map { dto ->
            CreatorAnalytics(
                songId = dto.id, title = dto.title, totalPlays = dto.playCount,
                uniqueListeners = dto.playCount, downloads = 0, plays7d = List(7) { 0L }
            )
        }
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Could not load analytics") })

    override suspend fun updateSongMetadata(songId: String, title: String, genres: List<String>): Resource<Unit> = runCatching {
        val existing = firestoreMusic.getSongById(songId) ?: error("Song not found")
        firestoreMusic.createSong(existing.copy(title = title, genres = genres)) // set() upserts
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not update song") })
}
