package com.cadence.music.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cadence.music.presentation.components.SongRow

@Composable
fun LibraryScreen(onSongClick: () -> Unit, onSeeDownloads: () -> Unit, viewModel: LibraryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Library", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Filled.Add, contentDescription = "New collection") }
        }

        ListItem(
            headlineContent = { Text("Offline Downloads") },
            supportingContent = { Text("Manage songs available without internet") },
            leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
            modifier = Modifier.clickableFull(onSeeDownloads)
        )
        HorizontalDivider()

        if (uiState.collections.isNotEmpty()) {
            Text("Collections", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(20.dp))
            uiState.collections.forEach { collection ->
                ListItem(
                    headlineContent = { Text(collection.name) },
                    supportingContent = { Text("${collection.songIds.size} songs") },
                    leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null) }
                )
            }
        }

        Text("Saved Songs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(20.dp))
        if (uiState.savedSongs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("Songs you save will show up here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(uiState.savedSongs) { song ->
                    SongRow(song = song, onClick = { viewModel.playSaved(song); onSongClick() })
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New collection") },
            text = {
                OutlinedTextField(newCollectionName, { newCollectionName = it }, label = { Text("Name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCollectionName.isNotBlank()) viewModel.createCollection(newCollectionName)
                    newCollectionName = ""
                    showCreateDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}

private fun Modifier.clickableFull(onClick: () -> Unit): Modifier =
    this.fillMaxWidth().clickable(onClick = onClick)
