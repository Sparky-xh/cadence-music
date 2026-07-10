package com.cadence.music.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cadence.music.domain.model.SubscriptionTier

@Composable
fun ProfileScreen(
    onSeePremium: () -> Unit,
    onSeeCreatorDashboard: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (user?.profilePhotoUrl != null) {
                AsyncImage(
                    model = user?.profilePhotoUrl, contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(user?.username ?: "", style = MaterialTheme.typography.headlineMedium)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AssistChip(onClick = {}, label = { Text(user?.subscriptionTier?.name ?: "FREE") })
            }
        }

        Spacer(Modifier.height(32.dp))

        if (user?.subscriptionTier != SubscriptionTier.PREMIUM && user?.subscriptionTier != SubscriptionTier.CREATOR) {
            ListItem(
                headlineContent = { Text("Go Premium") },
                supportingContent = { Text("Unlimited downloads, no ads, higher quality audio") },
                leadingContent = { Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable(onClick = onSeePremium)
            )
            HorizontalDivider()
        }

        ListItem(
            headlineContent = { Text(if (user?.canUseCreatorTools == true) "Creator Dashboard" else "Become a Creator") },
            supportingContent = { Text("Upload your own music, add lyrics, and track listens") },
            leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onSeeCreatorDashboard)
        )
        HorizontalDivider()

        ListItem(headlineContent = { Text("Favorite Genres") }, leadingContent = { Icon(Icons.Filled.Tune, contentDescription = null) })
        ListItem(headlineContent = { Text("Notification Settings") }, leadingContent = { Icon(Icons.Filled.Notifications, contentDescription = null) })
        ListItem(headlineContent = { Text("Audio Quality") }, leadingContent = { Icon(Icons.Filled.HighQuality, contentDescription = null) })

        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = { viewModel.onSignOut(onSignedOut) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) { Text("Sign out") }
        Spacer(Modifier.height(12.dp))
    }
}
