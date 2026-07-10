package com.cadence.music.data.local.dao

import androidx.room.*
import com.cadence.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Upsert
    suspend fun upsert(song: SongEntity)

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getById(songId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE isSaved = 1 ORDER BY cachedAtMs DESC")
    fun observeSaved(): Flow<List<SongEntity>>

    @Query("UPDATE songs SET isSaved = :saved WHERE id = :songId")
    suspend fun setSaved(songId: String, saved: Boolean)

    @Query("SELECT isSaved FROM songs WHERE id = :songId")
    suspend fun isSaved(songId: String): Boolean?

    @Query("SELECT * FROM songs WHERE uploaderId = :uploaderId")
    suspend fun getByUploader(uploaderId: String): List<SongEntity>
}
