package com.cadence.music.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Slightly asymmetric, generous corners (never fully pill-shaped) — reads as "printed sleeve
 *  corner," a small deliberate break from the pill-buttons-everywhere look of most music apps. */
val CadenceShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
