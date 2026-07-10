package com.cadence.music.data.repository

import com.cadence.music.data.local.dao.HistoryDao
import com.cadence.music.data.local.dao.PlaylistDao
import com.cadence.music.data.local.dao.SongDao
import com.cadence.music.data.local.entity.HistoryEntity
import com.cadence.music.data.local.entity.PlaylistEntity
import com.cadence.music.data.mapper.toDomain
import com.cadence.music.data.mapper.toEntity
import com.cadence.music.data.remote.firebase.FirestoreMusicDataSource
import com.cadence.music.domain.model.*
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val remote: FirestoreMusicDataSource,
    private val songDao: SongDao,
    private val historyDao: HistoryDao,
    private val playlistDao: PlaylistDao
) : MusicRepository {

    // Static bootstrap list so the "For You" shelves have sensible groupings even before a
    // Firestore /genres collection is populated. Safe to delete once real genre docs exist.
    override suspend fun getAvailableGenres(): Resource<List<Genre>> = Resource.Success(
        listOf(
            Genre("indie", "Indie", "#E8A33D"),
            Genre("lofi", "Lo-fi", "#8AA6A3"),
            Genre("electronic", "Electronic", "#C46A57"),
            Genre("hiphop", "Hip-Hop", "#B98CCE"),
            Genre("acoustic", "Acoustic", "#D9B36C"),
            Genre("rock", "Rock", "#A8524A"),
            Genre("jazz", "Jazz", "#6E8B74"),
            Genre("classical", "Classical", "#9C8ECF")
        )
    )

    override suspend fun getRecommendedSongs(favoriteGenreIds: List<String>, favoriteArtistIds: List<String>): Resource<List<Song>> =
        runCatching {
            val genreIds = favoriteGenreIds.ifEmpty { listOf("indie", "lofi", "electronic") }
            val songs = genreIds.flatMap { remote.getSongsByGenre(it, limit = 10) }.map { it.toDomain() }
            songDao.upsertAll(songs.map { it.toEntity() })
            songs.distinctBy { it.id }
        }.fold({ Resource.Success(it) }, { fallbackToCache { songDao.search("") } })

    override suspend fun getSongsByGenre(genreId: String): Resource<List<Song>> = runCatching {
        remote.getSongsByGenre(genreId).map { it.toDomain() }
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Could not load songs") })

    override suspend fun searchSongs(query: String): Resource<List<Song>> = runCatching {
        remote.searchSongsByTitlePrefix(query).map { it.toDomain() }
    }.fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { fallbackToCache { songDao.search(query) } } // offline-friendly search fallback
    )

    override suspend fun searchArtists(query: String): Resource<List<Artist>> = Resource.Success(emptyList())

    override suspend fun getSongById(songId: String): Resource<Song> = runCatching {
        remote.getSongById(songId)?.toDomain() ?: songDao.getById(songId)?.toDomain()
        ?: error("Song not found")
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Song not found") })

    override suspend fun getArtistById(artistId: String): Resource<Artist> = runCatching {
        val dto = remote.getArtistById(artistId) ?: error("Artist not found")
        Artist(dto.id, dto.name, dto.avatarUrl, dto.bio, dto.isIndependent, dto.followerCount, dto.monthlyListeners)
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Artist not found") })

    override fun observeRecentlyPlayed(limit: Int): Flow<List<HistoryEntry>> =
        historyDao.observeRecent(limit).map { entries ->
            entries.mapNotNull { entry ->
                songDao.getById(entry.songId)?.let { HistoryEntry(it.toDomain(), entry.playedAt, entry.msPlayed) }
            }
        }

    override suspend fun recordPlay(song: Song, msPlayed: Long) {
        songDao.upsert(song.toEntity())
        historyDao.insert(HistoryEntity(song.id, System.currentTimeMillis(), msPlayed))
        runCatching { remote.incrementPlayCount(song.id) }
    }

    override fun observeSavedSongs(): Flow<List<Song>> = songDao.observeSaved().map { list -> list.map { it.toDomain() } }

    override suspend fun toggleSaved(song: Song) {
        songDao.upsert(song.toEntity())
        val currentlySaved = songDao.isSaved(song.id) ?: false
        songDao.setSaved(song.id, !currentlySaved)
    }

    override suspend fun isSaved(songId: String): Boolean = songDao.isSaved(songId) ?: false

    override fun observeCustomCollections(userId: String): Flow<List<Playlist>> =
        playlistDao.observeForOwner(userId).map { list ->
            list.map { Playlist(it.id, it.name, it.ownerId, it.coverArtUrl, it.songIds, it.isSystemCollection) }
        }

    override suspend fun createCollection(userId: String, name: String): Resource<Playlist> = runCatching {
        val entity = PlaylistEntity(UUID.randomUUID().toString(), name, userId, null, emptyList(), false)
        playlistDao.upsert(entity)
        Playlist(entity.id, entity.name, entity.ownerId, entity.coverArtUrl, entity.songIds, entity.isSystemCollection)
    }.fold({ Resource.Success(it) }, { Resource.Error(it.message ?: "Could not create collection") })

    override suspend fun addSongToCollection(collectionId: String, songId: String): Resource<Unit> = runCatching {
        val existing = playlistDao.getById(collectionId) ?: error("Collection not found")
        playlistDao.upsert(existing.copy(songIds = (existing.songIds + songId).distinct()))
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not add song") })

    override suspend fun removeSongFromCollection(collectionId: String, songId: String): Resource<Unit> = runCatching {
        val existing = playlistDao.getById(collectionId) ?: error("Collection not found")
        playlistDao.upsert(existing.copy(songIds = existing.songIds - songId))
    }.fold({ Resource.Success(Unit) }, { Resource.Error(it.message ?: "Could not remove song") })

    private suspend inline fun fallbackToCache(crossinline block: suspend () -> List<com.cadence.music.data.local.entity.SongEntity>): Resource<List<Song>> =
        runCatching { block().map { it.toDomain() } }
            .fold({ Resource.Success(it) }, { Resource.Error("You are offline and no cached results are available") })
}
