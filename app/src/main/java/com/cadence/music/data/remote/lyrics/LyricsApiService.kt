package com.cadence.music.data.remote.lyrics

import com.cadence.music.data.remote.dto.LrcLibResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for lrclib.net, a free, key-less community lyrics database that returns
 * both plain and LRC-timestamped ("synced") lyrics. This is the default provider so the app
 * works out of the box with zero API keys; swap in Musixmatch/Genius here later if you want a
 * bigger catalog (both require developer accounts + different response shapes).
 */
interface LyricsApiService {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") durationSec: Int? = null
    ): Response<LrcLibResponseDto>

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String? = null
    ): Response<List<LrcLibResponseDto>>
}
