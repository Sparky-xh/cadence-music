package com.cadence.music.di

import android.content.Context
import androidx.room.Room
import com.cadence.music.data.local.dao.*
import com.cadence.music.data.local.db.CadenceDatabase
import com.cadence.music.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CadenceDatabase =
        Room.databaseBuilder(context, CadenceDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration() // fine for a v1 app; add real Migrations before shipping v2 schema changes
            .build()

    @Provides fun provideSongDao(db: CadenceDatabase): SongDao = db.songDao()
    @Provides fun provideLyricsDao(db: CadenceDatabase): LyricsDao = db.lyricsDao()
    @Provides fun provideDownloadDao(db: CadenceDatabase): DownloadDao = db.downloadDao()
    @Provides fun providePlaylistDao(db: CadenceDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideHistoryDao(db: CadenceDatabase): HistoryDao = db.historyDao()
}
