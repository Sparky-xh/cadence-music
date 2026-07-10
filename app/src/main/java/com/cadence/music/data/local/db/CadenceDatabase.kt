package com.cadence.music.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cadence.music.data.local.dao.*
import com.cadence.music.data.local.entity.*

@Database(
    entities = [
        SongEntity::class,
        LyricsEntity::class,
        DownloadEntity::class,
        PlaylistEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CadenceDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun downloadDao(): DownloadDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
}
