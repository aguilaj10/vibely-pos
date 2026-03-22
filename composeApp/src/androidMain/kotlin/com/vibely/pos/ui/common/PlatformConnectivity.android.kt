package com.vibely.pos.ui.common

import dev.jordond.connectivity.Connectivity
import kotlin.time.Duration.Companion.seconds

internal actual fun createConnectivity(): Connectivity = Connectivity {
    autoStart = true
    urls("cloudflare.com", "google.com", "dns.google")
    port = 443
    pollingIntervalMs = 10.seconds
    timeoutMs = 5.seconds
}
