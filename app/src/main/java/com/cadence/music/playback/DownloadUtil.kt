package com.cadence.music.playback

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.Download
import java.io.File
import java.util.concurrent.Executors

/**
 * Builds the single, app-wide [SimpleCache] + [DownloadManager] pair used for offline playback.
 * NoOpCacheEvictor means downloads are never auto-evicted for space — deletion only happens via
 * explicit user action in the Downloads screen (matches the "manage downloaded files" spec
 * requirement: the user, not the OS, decides what gets freed).
 */
object DownloadUtil {
    @Volatile private var cache: SimpleCache? = null
    @Volatile private var downloadManager: DownloadManager? = null

    fun getDownloadDirectory(context: Context): File = File(context.filesDir, "cadence_offline")

    fun getCache(context: Context): SimpleCache = cache ?: synchronized(this) {
        cache ?: SimpleCache(
            getDownloadDirectory(context),
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context)
        ).also { cache = it }
    }

    fun getDownloadManager(context: Context): DownloadManager = downloadManager ?: synchronized(this) {
        downloadManager ?: DownloadManager(
            context,
            StandaloneDatabaseProvider(context),
            getCache(context),
            DefaultHttpDataSource.Factory(),
            Executors.newFixedThreadPool(3)
        ).also { it.maxParallelDownloads = 3; downloadManager = it }
    }

    /** True once Media3 reports a completed download for [songId]; used to resolve the local
     *  file path before Rooms copy of download state has caught up. */
    fun localPathIfDownloaded(context: Context, songId: String): String? {
        val download = runCatching { getDownloadManager(context).downloadIndex.getDownload(songId) }.getOrNull()
        return if (download?.state == Download.STATE_COMPLETED) download.request.uri.toString() else null
    }
}
