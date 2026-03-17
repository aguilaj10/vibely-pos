package com.vibely.pos.shared.util

internal actual object PlatformNetworkMonitorFactory {
    actual fun create(): NetworkMonitor = NetworkMonitor()
}
