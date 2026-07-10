package com.cadence.music.domain.usecase.music

import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.MusicRepository
import com.cadence.music.util.Resource
import javax.inject.Inject

class GetRecommendedSongsUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(favoriteGenreIds: List<String>, favoriteArtistIds: List<String>): Resource<List<Song>> =
        repo.getRecommendedSongs(favoriteGenreIds, favoriteArtistIds)
}

class SearchSongsUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(query: String): Resource<List<Song>> {
        if (query.isBlank()) return Resource.Success(emptyList())
        return repo.searchSongs(query.trim())
    }
}

class ToggleSavedSongUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(song: Song) = repo.toggleSaved(song)
}

class RecordPlayUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(song: Song, msPlayed: Long) = repo.recordPlay(song, msPlayed)
}
