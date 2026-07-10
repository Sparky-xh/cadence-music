package com.cadence.music.data.local.db

import androidx.room.TypeConverter

/** Simple comma-join converter for the small string lists we store (genres, song-id lists).
 *  Values are pre-sanitized (genre ids / song ids never contain commas), so no escaping needed. */
class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(raw: String): List<String> = if (raw.isBlank()) emptyList() else raw.split(",")
}
