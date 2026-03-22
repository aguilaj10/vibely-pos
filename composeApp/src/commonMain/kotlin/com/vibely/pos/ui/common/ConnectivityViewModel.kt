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
 * Monitors network connectivity using platform-native detection via [createConnectivity].
 * Each platform provides a CORS-safe implementation:
 * - Android / JVM: HTTP polling to external hosts.
 * - Web (JS / WasmJs): browser `navigator.onLine` API — no HTTP pings.
 */
class ConnectivityViewModel : ViewModel() {

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivity = createConnectivity()

    init {
        viewModelScope.launch {
            connectivity.statusUpdates.collect { status ->
                _isOnline.value = status is Connectivity.Status.Connected
            }
        }
    }
}
