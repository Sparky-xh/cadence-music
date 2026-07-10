package com.cadence.music.data.local.dao

import androidx.room.*
import com.cadence.music.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Upsert
    suspend fun upsert(download: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY queuedAtMs DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE songId = :songId")
    fun observeOne(songId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE songId = :songId")
    suspend fun getOnce(songId: String): DownloadEntity?

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()

    @Query("SELECT COALESCE(SUM(totalBytes), 0) FROM downloads WHERE status = 'COMPLETED'")
    suspend fun totalBytesUsed(): Long
}
