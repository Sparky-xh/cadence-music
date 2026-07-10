package com.cadence.music.domain.usecase.creator

import com.cadence.music.domain.model.CreatorAnalytics
import com.cadence.music.domain.model.Song
import com.cadence.music.domain.repository.CreatorRepository
import com.cadence.music.domain.repository.NewSongDraft
import com.cadence.music.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadSongUseCase @Inject constructor(private val repo: CreatorRepository) {
    operator fun invoke(uploaderId: String, draft: NewSongDraft): Flow<Resource<Song>> {
        require(draft.title.isNotBlank()) { "Title required" }
        return repo.uploadSong(uploaderId, draft)
    }
}

class GetCreatorAnalyticsUseCase @Inject constructor(private val repo: CreatorRepository) {
    suspend operator fun invoke(uploaderId: String): Resource<List<CreatorAnalytics>> = repo.getAnalytics(uploaderId)
}
