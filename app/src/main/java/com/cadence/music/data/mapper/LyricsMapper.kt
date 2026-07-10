package com.cadence.music.data.mapper

import com.cadence.music.data.local.entity.LyricsEntity
import com.cadence.music.domain.model.LyricLine
import com.cadence.music.domain.model.Lyrics
import com.cadence.music.domain.model.LyricsSource
import org.json.JSONArray
import org.json.JSONObject

/** Hand-rolled JSON (org.json, bundled in Android) instead of pulling in a second serialization
 *  lib just for this one blob column. */
fun List<LyricLine>.toJson(): String {
    val array = JSONArray()
    forEach { line ->
        array.put(JSONObject().apply {
            put("t", line.startTimeMs ?: JSONObject.NULL)
            put("text", line.text)
        })
    }
    return array.toString()
}

fun String.toLyricLines(): List<LyricLine> {
    if (isBlank()) return emptyList()
    val array = JSONArray(this)
    return (0 until array.length()).map { i ->
        val obj = array.getJSONObject(i)
        val t = if (obj.isNull("t")) null else obj.getLong("t")
        LyricLine(startTimeMs = t, text = obj.getString("text"))
    }
}

fun Lyrics.toEntity(isManualOverride: Boolean): LyricsEntity = LyricsEntity(
    songId = songId, linesJson = lines.toJson(), isSynced = isSynced,
    source = source.name, isManualOverride = isManualOverride, lastEditedAt = lastEditedAt
)

fun LyricsEntity.toDomain(): Lyrics = Lyrics(
    songId = songId, lines = linesJson.toLyricLines(), isSynced = isSynced,
    source = LyricsSource.valueOf(source), lastEditedAt = lastEditedAt
)
