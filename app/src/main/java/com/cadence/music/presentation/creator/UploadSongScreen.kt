package com.cadence.music.presentation.creator

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cadence.music.presentation.components.GenreChip
import com.cadence.music.util.Resource

@Composable
fun UploadSongScreen(onUploaded: () -> Unit, onBack: () -> Unit, viewModel: CreatorViewModel = hiltViewModel()) {
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var albumName by remember { mutableStateOf("") }
    var lyricsText by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> audioUri = uri }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> coverUri = uri }

    LaunchedEffect(uploadState) {
        if (uploadState is Resource.Success) {
            viewModel.resetUploadState()
            onUploaded()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Upload a Song", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = { audioPicker.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.AudioFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (audioUri != null) "Audio file selected" else "Select audio file")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { coverPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (coverUri != null) "Cover art selected" else "Select cover image (optional)")
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(title, { title = it }, label = { Text("Song title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(albumName, { albumName = it }, label = { Text("Album (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        Text("Genres", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        FlowRowGenres(selectedGenres) { genreId ->
            selectedGenres = if (genreId in selectedGenres) selectedGenres - genreId else selectedGenres + genreId
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            lyricsText, { lyricsText = it },
            label = { Text("Lyrics (optional)") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        if (uploadState is Resource.Error) {
            Spacer(Modifier.height(8.dp))
            Text((uploadState as Resource.Error).message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                audioUri?.let {
                    viewModel.uploadSong(title, albumName.ifBlank { null }, selectedGenres.toList(), it, coverUri, lyricsText.ifBlank { null })
                }
            },
            enabled = title.isNotBlank() && audioUri != null && uploadState !is Resource.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (uploadState is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Publish song")
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** A handful of fixed genre chips for tagging an upload — reuses the same Genre model + chip
 *  component as onboarding, so genre tags are consistent across the whole app. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowGenres(selected: Set<String>, onToggle: (String) -> Unit) {
    val genreOptions = listOf(
        "indie" to "Indie", "lofi" to "Lo-fi", "electronic" to "Electronic", "hiphop" to "Hip-Hop",
        "acoustic" to "Acoustic", "rock" to "Rock", "jazz" to "Jazz", "classical" to "Classical"
    )
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        genreOptions.forEach { (id, label) ->
            GenreChip(
                genre = com.cadence.music.domain.model.Genre(id, label, "#E8A33D"),
                selected = id in selected,
                onClick = { onToggle(id) }
            )
        }
    }
}
