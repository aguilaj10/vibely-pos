package com.vibely.pos.backend.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.response.respondRedirect

@Suppress("LabeledExpression")
private const val STANDARD_HTTPS_PORT = 443

/**
 * Configuration for HTTPS enforcement.
 *
 * @property enabled Whether HTTPS enforcement is active
 * @property httpsPort The HTTPS port to redirect to (default: 443)
 * @property excludePaths Paths excluded from HTTPS enforcement (e.g., health checks)
 */
class HttpsEnforcementConfig(
    var enabled: Boolean = false,
    var httpsPort: Int = STANDARD_HTTPS_PORT,
    var excludePaths: List<String> = listOf("/health", "/")
)

/**
 * Ktor plugin that enforces HTTPS connections in production environments.
 *
 * When enabled, HTTP requests are automatically redirected to HTTPS.
 * Enable via environment variable: ENFORCE_HTTPS=true
 *
 * Excluded paths (like health checks) are not redirected to allow load balancer probes.
 */
val HttpsEnforcementPlugin = createApplicationPlugin(
    name = "HttpsEnforcementPlugin",
    createConfiguration = ::HttpsEnforcementConfig
) {
    val config = pluginConfig
    
    if (!config.enabled) {
        return@createApplicationPlugin
    }
    
    onCall { call ->
        val isHttps = call.request.local.scheme == "https" ||
            call.request.headers["X-Forwarded-Proto"] == "https"
        
        if (!isHttps && !config.excludePaths.any { call.request.local.uri.startsWith(it) }) {
            val host = call.request.host()
            val portPart = if (config.httpsPort == STANDARD_HTTPS_PORT) "" else ":${config.httpsPort}"
            val httpsUrl = "https://$host$portPart${call.request.local.uri}"
            
            call.respondRedirect(httpsUrl, permanent = true)
        }
    }
}
