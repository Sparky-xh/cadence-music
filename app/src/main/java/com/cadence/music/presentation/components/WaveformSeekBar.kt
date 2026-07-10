package com.cadence.music.presentation.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A bar-style seek control that reads as an audio waveform instead of a bare progress line —
 * the "amplitude" values are deterministically pseudo-randomized per song (seeded on songId) so
 * they are stable across recompositions without needing real decoded waveform data. Dragging or
 * tapping anywhere seeks; the filled portion (played) uses the primary color, the rest a muted
 * surface tone.
 */
@Composable
fun WaveformSeekBar(
    progress: Float, // 0f..1f
    songSeed: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 48
) {
    val amplitudes = remember(songSeed) {
        val random = Random(songSeed.hashCode())
        List(barCount) { 0.25f + random.nextFloat() * 0.75f }
    }
    var dragProgress by remember { mutableStateOf<Float?>(null) }
    val displayedProgress = dragProgress ?: progress

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(songSeed) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(songSeed) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        dragProgress?.let(onSeek)
                        dragProgress = null
                    }
                )
            }
    ) {
        val barWidth = size.width / (barCount * 1.6f)
        val gap = barWidth * 0.6f
        val playedBars = (displayedProgress * barCount).roundToInt()

        for (i in 0 until barCount) {
            val amplitude = amplitudes[i]
            val barHeight = size.height * amplitude
            val x = i * (barWidth + gap)
            val color = if (i < playedBars) Color(0xFFE8A33D) else Color(0xFF8A7B6C).copy(alpha = 0.4f)
            drawLine(
                color = color,
                start = Offset(x, (size.height - barHeight) / 2),
                end = Offset(x, (size.height + barHeight) / 2),
                strokeWidth = barWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
