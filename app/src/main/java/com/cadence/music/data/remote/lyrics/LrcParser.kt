package com.cadence.music.data.remote.lyrics

import com.cadence.music.domain.model.LyricLine

/**
 * Parses the standard LRC line format `[mm:ss.xx] lyric text` (also tolerates `[mm:ss.xxx]` and
 * multiple timestamp tags stacked on one line, e.g. karaoke-style `[00:12.00][00:45.00] La la`).
 * This is also reused by the manual sync editor: when a user hand-times a line, we serialize it
 * back through [toLrcString] so manual and fetched lyrics share one on-disk representation.
 */
object LrcParser {

    private val TIMESTAMP_REGEX = Regex("""\[(\d{2}):(\d{2})([.:]\d{1,3})?]""")

    fun parse(lrc: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrc.lineSequence().forEach { rawLine ->
            val matches = TIMESTAMP_REGEX.findAll(rawLine).toList()
            if (matches.isEmpty()) {
                val text = rawLine.trim()
                if (text.isNotEmpty()) lines += LyricLine(startTimeMs = null, text = text)
                return@forEach
            }
            val text = rawLine.substring(matches.last().range.last + 1).trim()
            matches.forEach { match ->
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val fraction = match.groupValues[3].trimStart('.', ':')
                val fractionMs = when (fraction.length) {
                    0 -> 0L
                    1 -> fraction.toLong() * 100
                    2 -> fraction.toLong() * 10
                    else -> fraction.take(3).toLong()
                }
                val startMs = (minutes * 60_000) + (seconds * 1000) + fractionMs
                lines += LyricLine(startTimeMs = startMs, text = text)
            }
        }
        return lines.sortedWith(compareBy(nullsLast()) { it.startTimeMs })
    }

    fun parsePlain(plain: String): List<LyricLine> =
        plain.lineSequence().filter { it.isNotBlank() }.map { LyricLine(null, it.trim()) }.toList()

    fun toLrcString(lines: List<LyricLine>): String = lines.joinToString("\n") { line ->
        if (line.startTimeMs == null) line.text else "[${formatTimestamp(line.startTimeMs)}] ${line.text}"
    }

    private fun formatTimestamp(ms: Long): String {
        val minutes = ms / 60_000
        val seconds = (ms % 60_000) / 1000
        val hundredths = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
    }

    /** Index of the line that should be highlighted for the given playback position. */
    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int {
        var result = -1
        for (i in lines.indices) {
            val start = lines[i].startTimeMs ?: continue
            if (start <= positionMs) result = i else break
        }
        return result
    }
}
