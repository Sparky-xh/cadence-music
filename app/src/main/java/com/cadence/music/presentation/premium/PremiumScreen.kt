package com.cadence.music.presentation.premium

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cadence.music.domain.model.SubscriptionTier

private val premiumBenefits = listOf(
    "Unlimited offline downloads",
    "Higher-quality audio streaming",
    "No interruptions between songs",
    "Priority access to new independent releases"
)
private val creatorBenefits = listOf(
    "Everything in Premium",
    "Upload your own original songs",
    "Custom artist profile",
    "Creator dashboard with play + download analytics"
)

@Composable
fun PremiumScreen(onBack: () -> Unit, onUpgraded: () -> Unit, viewModel: PremiumViewModel = hiltViewModel()) {
    val isUpgrading by viewModel.isUpgrading.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
        Spacer(Modifier.height(8.dp))
        Text("Cadence Premium", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            "Support independent artists and unlock the full experience",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        PlanCard(
            title = "Premium", price = "\$4.99/mo", benefits = premiumBenefits,
            isLoading = isUpgrading, onSelect = { viewModel.upgrade(SubscriptionTier.PREMIUM, onUpgraded) }
        )
        Spacer(Modifier.height(16.dp))
        PlanCard(
            title = "Creator", price = "\$9.99/mo", benefits = creatorBenefits,
            isLoading = isUpgrading, onSelect = { viewModel.upgrade(SubscriptionTier.CREATOR, onUpgraded) }
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "Billing goes through Google Play. Cancel anytime from Play Store subscription settings.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanCard(title: String, price: String, benefits: List<String>, isLoading: Boolean, onSelect: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(price, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(benefit, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSelect, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Choose $title")
            }
        }
    }
}
