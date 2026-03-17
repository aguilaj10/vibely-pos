package com.vibely.pos.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing application connectivity state.
 *
 * Monitors network connectivity status and exposes it as a reactive StateFlow.
 * Uses multiple endpoints for reliability checking.
 */
class ConnectivityViewModel : ViewModel() {

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivity = Connectivity {
        autoStart = true
        urls("cloudflare.com", "google.com", "dns.google")
        port = 443
        pollingIntervalMs = 10.seconds
        timeoutMs = 5.seconds
    }

    init {
        viewModelScope.launch {
            connectivity.statusUpdates.collect { status ->
                _isOnline.value = status is Connectivity.Status.Connected
            }
        }
    }
}
