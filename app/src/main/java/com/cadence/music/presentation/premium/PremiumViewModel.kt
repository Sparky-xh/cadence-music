package com.cadence.music.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadence.music.domain.model.SubscriptionTier
import com.cadence.music.domain.repository.UserRepository
import com.cadence.music.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isUpgrading = MutableStateFlow(false)
    val isUpgrading: StateFlow<Boolean> = _isUpgrading.asStateFlow()

    /**
     * NOTE: a real purchase flow goes through the Play Billing Library (BillingClient), which
     * confirms payment before this is ever called, then a server verifies the purchase token.
     * This directly flips the Firestore flag so the rest of the app (paywalls, download caps,
     * creator-tool gating) has something real to react to during development.
     */
    fun upgrade(tier: SubscriptionTier, onDone: () -> Unit) {
        _isUpgrading.value = true
        viewModelScope.launch {
            userRepository.upgradeSubscription(tier)
            _isUpgrading.value = false
            onDone()
        }
    }
}
