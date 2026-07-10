package com.cadence.music.presentation.lyrics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.WindowInsets
import com.cadence.music.domain.model.Lyrics
import kotlinx.coroutines.launch

/**
 * Auto-scrolling, karaoke-style lyrics screen. The current line is scaled up and rendered at
 * full opacity while past/future lines fade — a softer, more musical take on the usual bold-vs-
 * gray highlight most players use.
 */
@Composable
fun LyricsScreen(onBack: () -> Unit, onEditLyrics: () -> Unit, viewModel: LyricsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.currentLineIndex) {
        if (uiState.currentLineIndex >= 0) {
            scope.launch {
                listState.animateScrollToItem(
                    index = (uiState.currentLineIndex - 2).coerceAtLeast(0)
                )
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.song?.title ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(uiState.song?.artistName ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEditLyrics) { Icon(Icons.Filled.Edit, contentDescription = "Edit lyrics") }
        }

        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.notFound || uiState.lyrics == null -> EmptyLyricsState(onEditLyrics)
            else -> LyricsList(uiState.lyrics!!, uiState.currentLineIndex, listState)
        }
    }
}

@Composable
private fun LyricsList(lyrics: Lyrics, currentLineIndex: Int, listState: androidx.compose.foundation.lazy.LazyListState) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 200.dp, horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val isCurrent = index == currentLineIndex
            val scale by animateFloatAsState(if (isCurrent) 1.08f else 1f, animationSpec = tween(250), label = "line_scale")
            val alpha by animateFloatAsState(
                when {
                    isCurrent -> 1f
                    !lyrics.isSynced -> 0.85f // unsynced lyrics: no fade choreography, just readable
                    else -> 0.4f
                },
                animationSpec = tween(250), label = "line_alpha"
            )
            Text(
                text = line.text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            )
        }
    }
}

@Composable
private fun EmptyLyricsState(onAddLyrics: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No lyrics yet", style = MaterialTheme.typography.titleLarge)
            Text(
                "Be the first to add lyrics for this song",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )
            Button(onClick = onAddLyrics) { Text("Add lyrics") }
        }
    }
}
