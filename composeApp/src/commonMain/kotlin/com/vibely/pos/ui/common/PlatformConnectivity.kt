package com.vibely.pos.ui.common

import dev.jordond.connectivity.Connectivity

/**
 * Creates a platform-appropriate [Connectivity] instance.
 *
 * - Android / JVM: HTTP polling to external hosts for reliable detection.
 * - JS / WasmJs: Uses browser's `navigator.onLine` API via a Flow provider.
 *   HTTP polling is intentionally excluded to avoid cross-origin (CORS) errors.
 */
internal expect fun createConnectivity(): Connectivity
