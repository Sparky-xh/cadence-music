package com.cadence.music.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cadence.music.domain.model.Genre

@Composable
fun GenreChip(genre: Genre, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = runCatching { Color(android.graphics.Color.parseColor(genre.accentColorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Text(
        text = genre.displayName,
        color = if (selected) MaterialTheme.colorScheme.background else accent,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .background(
                color = if (selected) accent else accent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
