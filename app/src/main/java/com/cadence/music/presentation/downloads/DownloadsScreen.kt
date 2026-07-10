package com.cadence.music.presentation.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cadence.music.domain.model.DownloadItem
import com.cadence.music.domain.model.DownloadStatus

@Composable
fun DownloadsScreen(onBack: () -> Unit, onSongClick: () -> Unit, viewModel: DownloadsViewModel = hiltViewModel()) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val totalBytes by viewModel.totalBytes.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Downloads", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { showClearConfirm = true }, enabled = downloads.isNotEmpty()) { Text("Clear all") }
        }

        Text(
            "${downloads.size} songs · ${formatBytes(totalBytes)} used",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 12.dp)
        )

        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Songs you download will show up here for offline listening", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(downloads, key = { it.song.id }) { item ->
                    DownloadRow(
                        item = item,
                        onClick = { if (item.status == DownloadStatus.COMPLETED) { viewModel.playDownloaded(item); onSongClick() } },
                        onPauseResume = {
                            if (item.status == DownloadStatus.DOWNLOADING) viewModel.pause(item.song.id) else viewModel.resume(item.song.id)
                        },
                        onRemove = { viewModel.remove(item.song.id) }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all downloads?") },
            text = { Text("This removes every offline song from this device. You can download them again anytime.") },
            confirmButton = { TextButton(onClick = { viewModel.clearAll(); showClearConfirm = false }) { Text("Clear all") } },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DownloadRow(item: DownloadItem, onClick: () -> Unit, onPauseResume: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.song.coverArtUrl, contentDescription = null,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShapeCompat()),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            when (item.status) {
                DownloadStatus.COMPLETED -> Text(item.song.artistName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                DownloadStatus.FAILED -> Text("Download failed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                else -> LinearProgressIndicator(
                    progress = { item.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }
        if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.PAUSED) {
            IconButton(onClick = onPauseResume) {
                Icon(if (item.status == DownloadStatus.DOWNLOADING) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Pause/Resume")
            }
        }
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Remove") }
    }
}

@Composable
private fun RoundedCornerShapeCompat() = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb > 1024) "%.1f GB".format(mb / 1024) else "%.0f MB".format(mb)
}
