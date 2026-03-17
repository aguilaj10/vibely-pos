package com.vibely.pos.shared.util

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: kotlinx.coroutines.Job? = null

    private val testHosts = listOf("8.8.8.8", "1.1.1.1")
    private val testPort = 53
    private val checkIntervalMs = 5000L

    actual fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch {
            while (isActive) {
                checkConnectivity()
                delay(checkIntervalMs)
            }
        }
    }

    actual fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun checkConnectivity() {
        val isConnected = testHosts.any { host ->
            try {
                val address = java.net.InetAddress.getByAddress(
                    host,
                    java.net.InetAddress.getByName(host).address,
                )
                address.isReachable(3000)
            } catch (e: Exception) {
                false
            }
        }
        _isOnline.update { isConnected }
    }
}
