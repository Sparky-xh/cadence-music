package com.cadence.music.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cadence.music.domain.model.Song
import com.cadence.music.presentation.components.SongRow

/**
 * Home reads as a stack of curated "shelves" (Recommended, Recently Played, Saved) rather than a
 * single algorithmic grid — each shelf is its own horizontally-scrolling row of art-forward
 * cards, closer to a record-store staff-picks wall than a typical vertical song list.
 */
@Composable
fun HomeScreen(
    onSongClick: () -> Unit,
    onSeeDownloads: () -> Unit,
    onSeePremium: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Good to see you" + if (uiState.username.isNotBlank()) ", ${uiState.username}" else "", style = MaterialTheme.typography.headlineMedium)
                    Text("Here is what is playing in your world", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    IconButton(onClick = onSeeDownloads) { Icon(Icons.Filled.Download, contentDescription = "Downloads") }
                    IconButton(onClick = onSeePremium) { Icon(Icons.Filled.WorkspacePremium, contentDescription = "Premium") }
                }
            }
        }

        if (uiState.recommended.isNotEmpty()) {
            item { ShelfHeader("For You") }
            item {
                ShelfRow(uiState.recommended) { song ->
                    viewModel.playSong(song, uiState.recommended)
                    onSongClick()
                }
            }
        }

        if (uiState.recentlyPlayed.isNotEmpty()) {
            item { ShelfHeader("Recently Played") }
            item {
                ShelfRow(uiState.recentlyPlayed.map { it.song }) { song ->
                    viewModel.playSong(song, uiState.recentlyPlayed.map { it.song })
                    onSongClick()
                }
            }
        }

        if (uiState.savedSongs.isNotEmpty()) {
            item { ShelfHeader("Saved Songs") }
            items(uiState.savedSongs) { song ->
                SongRow(song = song, onClick = {
                    viewModel.playSong(song, uiState.savedSongs)
                    onSongClick()
                })
            }
        }

        if (uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ShelfHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 20.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun ShelfRow(songs: List<Song>, onClick: (Song) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(songs) { song ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick(song) }
            ) {
                AsyncImage(
                    model = song.coverArtUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(6.dp))
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artistName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
