package com.cadence.music.di

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.cadence.music.playback.DownloadUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideDownloadManager(@ApplicationContext context: Context): DownloadManager =
        DownloadUtil.getDownloadManager(context)

    /**
     * A CacheDataSource.Factory that transparently reads from the offline cache when a song is
     * downloaded and falls through to the network otherwise — this is what lets one playback
     * code path (PlayerController) serve both streamed and downloaded songs without branching.
     */
    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(@ApplicationContext context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(DownloadUtil.getCache(context))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        cacheDataSourceFactory: CacheDataSource.Factory
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .build()
}
