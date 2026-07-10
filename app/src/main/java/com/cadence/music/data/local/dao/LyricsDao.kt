package com.cadence.music.data.local.dao

import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Dao
import com.cadence.music.data.local.entity.LyricsEntity

@Dao
interface LyricsDao {
    @Upsert
    suspend fun upsert(lyrics: LyricsEntity)

    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    suspend fun getForSong(songId: String): LyricsEntity?

    @Query("SELECT isManualOverride FROM lyrics WHERE songId = :songId")
    suspend fun hasManualOverride(songId: String): Boolean?
}
