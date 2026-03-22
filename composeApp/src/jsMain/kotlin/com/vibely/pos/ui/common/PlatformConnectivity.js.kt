package com.vibely.pos.ui.common

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.browser.window
import kotlinx.coroutines.flow.flow

/**
 * Web (JS) connectivity using browser's native `navigator.onLine` API.
 * No HTTP requests are made — avoids cross-origin (CORS) errors entirely.
 */
internal actual fun createConnectivity(): Connectivity {
    val statusFlow = flow {
        emit(
            if (window.navigator.onLine) {
                Connectivity.Status.Connected(metered = false)
            } else {
                Connectivity.Status.Disconnected
            },
        )
    }
    return Connectivity(provider = ConnectivityProvider(statusFlow))
}
