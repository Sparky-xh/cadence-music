package com.cadence.music.domain.repository

import android.net.Uri
import com.cadence.music.domain.model.CreatorAnalytics
import com.cadence.music.domain.model.Song
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow

data class NewSongDraft(
    val title: String,
    val albumName: String?,
    val genres: List<String>,
    val audioFileUri: Uri,
    val coverArtUri: Uri?,
    val lyricsPlainText: String?
)

interface CreatorRepository {
    /** Emits 0f..1f upload progress, then completes with the created [Song]. */
    fun uploadSong(uploaderId: String, draft: NewSongDraft): Flow<Resource<Song>>

    suspend fun getMySongs(uploaderId: String): Resource<List<Song>>
    suspend fun deleteSong(songId: String): Resource<Unit>
    suspend fun getAnalytics(uploaderId: String): Resource<List<CreatorAnalytics>>
    suspend fun updateSongMetadata(songId: String, title: String, genres: List<String>): Resource<Unit>
}
