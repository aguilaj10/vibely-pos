package com.vibely.pos.shared.util

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual class NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var onlineHandler: (dynamic) -> Unit = {}
    private var offlineHandler: (dynamic) -> Unit = {}

    actual fun startMonitoring() {
        updateOnlineStatus()

        onlineHandler = {
            _isOnline.update { true }
        }
        offlineHandler = {
            _isOnline.update { false }
        }

        window.addEventListener("online", onlineHandler)
        window.addEventListener("offline", offlineHandler)
    }

    actual fun stopMonitoring() {
        window.removeEventListener("online", onlineHandler)
        window.removeEventListener("offline", offlineHandler)
    }

    private fun updateOnlineStatus() {
        _isOnline.update { window.asDynamic().navigator.onLine as Boolean }
    }
}
