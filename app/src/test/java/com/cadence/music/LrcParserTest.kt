package com.cadence.music

import com.cadence.music.data.remote.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parses standard mm colon ss dot xx timestamps`() {
        val lrc = "[00:12.34] First line\n[00:15.00] Second line"
        val lines = LrcParser.parse(lrc)

        assertEquals(2, lines.size)
        assertEquals(12_340L, lines[0].startTimeMs)
        assertEquals("First line", lines[0].text)
        assertEquals(15_000L, lines[1].startTimeMs)
    }

    @Test
    fun `parses two digit hundredths correctly not as milliseconds`() {
        // A common bug: treating ".34" as 34ms instead of 340ms.
        val lines = LrcParser.parse("[01:02.34] test")
        assertEquals(62_340L, lines[0].startTimeMs)
    }

    @Test
    fun `lines without timestamps are treated as plain unsynced text`() {
        val lines = LrcParser.parsePlain("line one\nline two\n\nline three")
        assertEquals(3, lines.size)
        assertNull(lines[0].startTimeMs)
    }

    @Test
    fun `currentLineIndex returns the last line whose timestamp has passed`() {
        val lines = LrcParser.parse("[00:05.00] a\n[00:10.00] b\n[00:20.00] c")

        assertEquals(-1, LrcParser.currentLineIndex(lines, positionMs = 1_000)) // before any line starts
        assertEquals(0, LrcParser.currentLineIndex(lines, positionMs = 7_000))
        assertEquals(1, LrcParser.currentLineIndex(lines, positionMs = 15_000))
        assertEquals(2, LrcParser.currentLineIndex(lines, positionMs = 999_000))
    }

    @Test
    fun `round trips through toLrcString`() {
        val original = LrcParser.parse("[00:05.50] hello world")
        val serialized = LrcParser.toLrcString(original)
        val reparsed = LrcParser.parse(serialized)

        assertEquals(original, reparsed)
    }
}
