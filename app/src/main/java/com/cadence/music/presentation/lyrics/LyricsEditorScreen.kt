package com.cadence.music.presentation.lyrics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Two tabs: "Text" for writing/editing the raw lyric lines, and "Sync" for hand-timing them —
 * play the song and tap the flag next to each line exactly when it starts. This mirrors how
 * most community LRC files actually get made, without requiring the user to know LRC syntax.
 */
@Composable
fun LyricsEditorScreen(onDone: () -> Unit, viewModel: LyricsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var textDraft by remember(uiState.lyrics) {
        mutableStateOf(uiState.lyrics?.plainText.orEmpty())
    }
    val timings = remember { mutableStateMapOf<Int, Long?>() }
    val playbackState by viewModel.playerController.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDone) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            Text("Edit Lyrics", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = {
                if (tab == 0) {
                    viewModel.saveManualText(textDraft)
                } else {
                    viewModel.saveLineTimings(timings.map { (index, ms) -> index to ms })
                }
                onDone()
            }) { Text("Save") }
        }

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Text") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Sync timing") })
        }

        when (tab) {
            0 -> OutlinedTextField(
                value = textDraft,
                onValueChange = { textDraft = it },
                placeholder = { Text("Type or paste lyrics, one line at a time") },
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
            else -> SyncTab(
                lines = uiState.lyrics?.lines.orEmpty(),
                positionMs = uiState.positionMs,
                isPlaying = playbackState.isPlaying,
                onTogglePlay = { viewModel.playerController.togglePlayPause() },
                onMark = { index -> timings[index] = uiState.positionMs }
            )
        }
    }
}

@Composable
private fun SyncTab(
    lines: List<com.cadence.music.domain.model.LyricLine>,
    positionMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onMark: (Int) -> Unit
) {
    if (lines.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Add lyric text first on the Text tab before syncing timing", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(onClick = onTogglePlay) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
            }
            Spacer(Modifier.width(12.dp))
            Text(formatMs(positionMs), style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "Tap the flag on each line exactly when it starts playing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(lines) { index, line ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMark(index) }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Mark timing", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(line.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
