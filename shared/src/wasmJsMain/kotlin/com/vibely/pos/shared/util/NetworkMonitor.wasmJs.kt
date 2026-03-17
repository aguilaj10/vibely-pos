package com.vibely.pos.shared.util

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

actual class NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: kotlinx.coroutines.Job? = null

    actual fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch {
            while (isActive) {
                updateOnlineStatus()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    actual fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun updateOnlineStatus() {
        _isOnline.update { window.navigator.onLine }
    }

    companion object {
        private const val CHECK_INTERVAL_MS = 5000L
    }
}
