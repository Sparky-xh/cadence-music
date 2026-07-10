package com.cadence.music.presentation.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cadence.music.presentation.components.VinylArt
import com.cadence.music.presentation.components.WaveformSeekBar
import com.cadence.music.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen player. The gesture zones are the headline interaction: the album-art area is
 * split into an invisible left/right half. Double-tapping either half seeks by
 * [Constants.MIN_SEEK_GESTURE_MS]; press-and-holding the right half ramps playback to
 * [Constants.FAST_FORWARD_SPEED] until released. A small on-screen icon flashes to confirm each
 * gesture since there is no visible button to provide that feedback otherwise.
 */
@Composable
fun PlayerScreen(onBack: () -> Unit, onOpenLyrics: () -> Unit, viewModel: PlayerViewModel = hiltViewModel()) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val controller = viewModel.playerController

    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var gestureFeedback by remember { mutableStateOf<GestureFeedback?>(null) }
    var isBoosted by remember { mutableStateOf(false) }

    val song = playbackState.currentSong

    LaunchedEffect(gestureFeedback) {
        if (gestureFeedback != null) {
            delay(500)
            gestureFeedback = null
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse") }
            Text("Now Playing", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = { showSleepSheet = true }) { Icon(Icons.Outlined.Bedtime, contentDescription = "Sleep timer") }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .pointerInput(song?.id) {
                    detectTapGestures(
                        onPress = { offset ->
                            val isRightHalf = offset.x > size.width / 2
                            if (isRightHalf) {
                                val boostJob = scope.launch {
                                    delay(350)
                                    controller.temporarilyBoostSpeed(true, Constants.NORMAL_SPEED, Constants.FAST_FORWARD_SPEED)
                                    isBoosted = true
                                }
                                tryAwaitRelease()
                                boostJob.cancel()
                                if (isBoosted) {
                                    controller.temporarilyBoostSpeed(false, Constants.NORMAL_SPEED, Constants.FAST_FORWARD_SPEED)
                                    isBoosted = false
                                }
                            } else {
                                tryAwaitRelease()
                            }
                        },
                        onDoubleTap = { offset ->
                            val isRightHalf = offset.x > size.width / 2
                            if (isRightHalf) {
                                controller.seekBy(Constants.MIN_SEEK_GESTURE_MS)
                                gestureFeedback = GestureFeedback.FORWARD
                            } else {
                                controller.seekBy(-Constants.MIN_SEEK_GESTURE_MS)
                                gestureFeedback = GestureFeedback.REWIND
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            VinylArt(coverArtUrl = song?.coverArtUrl, isSpinning = playbackState.isPlaying)

            AnimatedVisibility(visible = gestureFeedback != null, enter = fadeIn(), exit = fadeOut()) {
                GestureFeedbackIcon(gestureFeedback)
            }
            AnimatedVisibility(visible = isBoosted, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.TopEnd)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Text("2x", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(song?.title ?: "Nothing playing", style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                Text(song?.artistName.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            IconButton(onClick = viewModel::toggleSave) {
                Icon(
                    if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        WaveformSeekBar(
            progress = if (playbackState.durationMs > 0) positionMs.toFloat() / playbackState.durationMs else 0f,
            songSeed = song?.id ?: "none",
            onSeek = { fraction -> controller.seekTo((fraction * playbackState.durationMs).toLong()) }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(positionMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatMs(playbackState.durationMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = controller::toggleShuffle) {
                Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle", tint = if (playbackState.isShuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current)
            }
            IconButton(onClick = controller::skipToPrevious) { Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp)) }
            FilledIconButton(onClick = controller::togglePlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = controller::skipToNext) { Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp)) }
            IconButton(onClick = controller::cycleRepeatMode) {
                Icon(
                    if (playbackState.repeatMode == com.cadence.music.domain.model.RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (playbackState.repeatMode != com.cadence.music.domain.model.RepeatMode.OFF) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { showSpeedSheet = true }) { Text("${playbackState.playbackSpeed}x") }
            TextButton(onClick = onOpenLyrics) { Icon(Icons.Outlined.Notes, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Lyrics") }
            TextButton(onClick = { viewModel.download(isPremiumOrAbove = true) }) {
                Icon(if (song?.isDownloaded == true) Icons.Filled.DownloadDone else Icons.Outlined.Download, contentDescription = null)
                Spacer(Modifier.width(4.dp)); Text(if (song?.isDownloaded == true) "Downloaded" else "Download")
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showSpeedSheet) {
        ModalBottomSheet(onDismissRequest = { showSpeedSheet = false }) {
            Text("Playback speed", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
            Constants.PLAYBACK_SPEED_OPTIONS.forEach { speed ->
                ListItem(
                    headlineContent = { Text("${speed}x") },
                    trailingContent = { if (speed == playbackState.playbackSpeed) Icon(Icons.Filled.Check, contentDescription = null) },
                    modifier = Modifier.clickableRow { controller.setPlaybackSpeed(speed); showSpeedSheet = false }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showSleepSheet) {
        ModalBottomSheet(onDismissRequest = { showSleepSheet = false }) {
            Text("Sleep timer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
            if (playbackState.sleepTimerEndsAtMs != null) {
                ListItem(
                    headlineContent = { Text("Cancel timer") },
                    leadingContent = { Icon(Icons.Filled.Close, contentDescription = null) },
                    modifier = Modifier.clickableRow { viewModel.cancelSleepTimer(); showSleepSheet = false }
                )
                HorizontalDivider()
            }
            Constants.SLEEP_TIMER_PRESETS_MIN.forEach { minutes ->
                ListItem(
                    headlineContent = { Text("$minutes minutes") },
                    modifier = Modifier.clickableRow { viewModel.startSleepTimer(minutes); showSleepSheet = false }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private enum class GestureFeedback { REWIND, FORWARD }

@Composable
private fun GestureFeedbackIcon(feedback: GestureFeedback?) {
    if (feedback == null) return
    Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.5f)) {
        Icon(
            if (feedback == GestureFeedback.REWIND) Icons.Filled.FastRewind else Icons.Filled.FastForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(20.dp).size(40.dp)
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
