package com.cadence.music.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * The album-art centerpiece: a circular sleeve that spins continuously while playing, pauses in
 * place otherwise — this is the apps signature visual, replacing the generic square album-art
 * card most players use. A thin "vinyl groove" ring reveals around the artwork.
 */
@Composable
fun VinylArt(
    coverArtUrl: String?,
    isSpinning: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 280.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Restart),
        label = "vinyl_angle"
    )
    val rotation = if (isSpinning) angle else 0f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .rotate(rotation)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
        ) {
            AsyncImage(
                model = coverArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(size * 0.72f)
                    .align(Alignment.Center)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            // Vinyl "spindle hole"
            Box(
                modifier = Modifier
                    .size(size * 0.08f)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
