package com.cadence.music.data.mapper

import com.cadence.music.data.local.entity.SongEntity
import com.cadence.music.data.remote.dto.SongDto
import com.cadence.music.domain.model.Song

fun SongDto.toDomain(localFilePath: String? = null): Song = Song(
    id = id, title = title, artistId = artistId, artistName = artistName, albumName = albumName,
    coverArtUrl = coverArtUrl, durationMs = durationMs, streamUrl = streamUrl,
    localFilePath = localFilePath, genres = genres, isCreatorUpload = isCreatorUpload,
    uploaderId = uploaderId, playCount = playCount, releaseDate = releaseDate
)

fun SongEntity.toDomain(localFilePath: String? = null): Song = Song(
    id = id, title = title, artistId = artistId, artistName = artistName, albumName = albumName,
    coverArtUrl = coverArtUrl, durationMs = durationMs, streamUrl = streamUrl,
    localFilePath = localFilePath, genres = genres, isCreatorUpload = isCreatorUpload,
    uploaderId = uploaderId, playCount = playCount, releaseDate = releaseDate, isSaved = isSaved
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id, title = title, artistId = artistId, artistName = artistName, albumName = albumName,
    coverArtUrl = coverArtUrl, durationMs = durationMs, streamUrl = streamUrl, genres = genres,
    isCreatorUpload = isCreatorUpload, uploaderId = uploaderId, playCount = playCount,
    releaseDate = releaseDate
)

fun SongDto.toEntity(): SongEntity = toDomain().toEntity()
