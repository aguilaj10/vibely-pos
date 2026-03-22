@file:OptIn(ExperimentalWasmJsInterop::class)

package com.vibely.pos.ui.common

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.flow.flow

/**
 * Web (WasmJs) connectivity using browser's native `navigator.onLine` API.
 * No HTTP requests are made — avoids cross-origin (CORS) errors entirely.
 */
internal actual fun createConnectivity(): Connectivity {
    val statusFlow = flow {
        emit(
            if (navigatorOnLine() != 0) {
                Connectivity.Status.Connected(metered = false)
            } else {
                Connectivity.Status.Disconnected
            },
        )
    }
    return Connectivity(provider = ConnectivityProvider(statusFlow))
}

@JsFun("() => navigator.onLine ? 1 : 0")
private external fun navigatorOnLine(): Int
