package com.cadence.music.data.remote.firebase

import com.cadence.music.data.remote.dto.ArtistDto
import com.cadence.music.data.remote.dto.SongDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore layout:
 *   /songs/{songId}            -> SongDto
 *   /artists/{artistId}        -> ArtistDto
 *   /genres/{genreId}          -> { displayName, accentColorHex }
 *   /users/{uid}/collections/{id} -> user-created playlists
 * Recommended composite indexes: songs(genres ARRAY_CONTAINS, playCount DESC) for the "For You"
 * shelves, and songs(uploaderId, releaseDate DESC) for the creator dashboard.
 */
@Singleton
class FirestoreMusicDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getSongsByGenre(genreId: String, limit: Long = 30): List<SongDto> =
        firestore.collection("songs")
            .whereArrayContains("genres", genreId)
            .orderBy("playCount")
            .limit(limit)
            .get().await()
            .toObjects(SongDto::class.java)

    suspend fun searchSongsByTitlePrefix(query: String): List<SongDto> =
        // Firestore has no native full-text search; prefix range query is the common workaround.
        // For real fuzzy search, mirror writes into Algolia/Typesense (out of scope here).
        firestore.collection("songs")
            .orderBy("title")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(30)
            .get().await()
            .toObjects(SongDto::class.java)

    suspend fun getSongById(songId: String): SongDto? =
        firestore.collection("songs").document(songId).get().await().toObject(SongDto::class.java)

    suspend fun getArtistById(artistId: String): ArtistDto? =
        firestore.collection("artists").document(artistId).get().await().toObject(ArtistDto::class.java)

    suspend fun getSongsByUploader(uploaderId: String): List<SongDto> =
        firestore.collection("songs")
            .whereEqualTo("uploaderId", uploaderId)
            .orderBy("releaseDate")
            .get().await()
            .toObjects(SongDto::class.java)

    suspend fun createSong(dto: SongDto) {
        firestore.collection("songs").document(dto.id).set(dto).await()
    }

    suspend fun deleteSong(songId: String) {
        firestore.collection("songs").document(songId).delete().await()
    }

    suspend fun incrementPlayCount(songId: String) {
        firestore.collection("songs").document(songId)
            .update("playCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }
}
