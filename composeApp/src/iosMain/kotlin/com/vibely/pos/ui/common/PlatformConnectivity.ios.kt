package com.vibely.pos.ui.common

import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.ConnectivityProvider
import kotlinx.coroutines.flow.flow

/**
 * iOS connectivity using a conservative "assume connected" default.
 *
 * Full NWPathMonitor integration can replace this when the iOS target
 * is actively developed. For now this prevents compile errors on
 * iosArm64 / iosSimulatorArm64 targets.
 */
internal actual fun createConnectivity(): Connectivity {
    val statusFlow = flow {
        emit(Connectivity.Status.Connected(metered = false))
    }
    return Connectivity(provider = ConnectivityProvider(statusFlow))
}
