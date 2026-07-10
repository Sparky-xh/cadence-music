package com.cadence.music.di

import com.cadence.music.data.repository.*
import com.cadence.music.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
    @Binds @Singleton abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository
    @Binds @Singleton abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
    @Binds @Singleton abstract fun bindCreatorRepository(impl: CreatorRepositoryImpl): CreatorRepository
}
