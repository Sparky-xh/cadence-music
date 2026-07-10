package com.cadence.music.domain.repository

import com.cadence.music.domain.model.Artist
import com.cadence.music.domain.model.Genre
import com.cadence.music.domain.model.HistoryEntry
import com.cadence.music.domain.model.Playlist
import com.cadence.music.domain.model.Song
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getAvailableGenres(): Resource<List<Genre>>
    suspend fun getRecommendedSongs(favoriteGenreIds: List<String>, favoriteArtistIds: List<String>): Resource<List<Song>>
    suspend fun getSongsByGenre(genreId: String): Resource<List<Song>>
    suspend fun searchSongs(query: String): Resource<List<Song>>
    suspend fun searchArtists(query: String): Resource<List<Artist>>
    suspend fun getSongById(songId: String): Resource<Song>
    suspend fun getArtistById(artistId: String): Resource<Artist>

    fun observeRecentlyPlayed(limit: Int = 30): Flow<List<HistoryEntry>>
    suspend fun recordPlay(song: Song, msPlayed: Long)

    fun observeSavedSongs(): Flow<List<Song>>
    suspend fun toggleSaved(song: Song)
    suspend fun isSaved(songId: String): Boolean

    fun observeCustomCollections(userId: String): Flow<List<Playlist>>
    suspend fun createCollection(userId: String, name: String): Resource<Playlist>
    suspend fun addSongToCollection(collectionId: String, songId: String): Resource<Unit>
    suspend fun removeSongFromCollection(collectionId: String, songId: String): Resource<Unit>
}
