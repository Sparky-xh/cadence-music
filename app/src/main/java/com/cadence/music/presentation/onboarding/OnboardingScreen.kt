package com.cadence.music.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cadence.music.presentation.components.GenreChip

/** First-run genre picker. Kept to a single screen (rather than the usual multi-step "genres,
 *  then artists, then a final confirm" flow) — the fewer taps between signup and first song
 *  played, the better, and favorite artists can be refined later from Search + long-press. */
@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onFinished()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("What do you want to hear?", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Pick a few genres — we use these to shape your Home feed. You can change this anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.genres) { genre ->
                GenreChip(
                    genre = genre,
                    selected = genre.id in uiState.selectedGenreIds,
                    onClick = { viewModel.toggleGenre(genre.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = viewModel::finishOnboarding,
            enabled = uiState.selectedGenreIds.isNotEmpty() && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Continue (${uiState.selectedGenreIds.size} selected)")
        }
        Spacer(Modifier.height(8.dp))
    }
}
