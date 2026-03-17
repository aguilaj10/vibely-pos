package com.vibely.pos.backend.security

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.response.header

/**
 * Configuration for security headers.
 *
 * @property contentSecurityPolicy CSP directive to prevent XSS attacks
 * @property strictTransportSecurity HSTS header to enforce HTTPS
 * @property xFrameOptions Prevent clickjacking attacks
 * @property xContentTypeOptions Prevent MIME sniffing
 * @property xXssProtection Legacy XSS protection for older browsers
 * @property referrerPolicy Control referrer information leakage
 */
data class SecurityHeadersConfig(
    val contentSecurityPolicy: String = "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline'; " +
        "style-src 'self' 'unsafe-inline'; " +
        "img-src 'self' data: https:; " +
        "font-src 'self' data:;",
    val strictTransportSecurity: String = "max-age=31536000; includeSubDomains",
    val xFrameOptions: String = "DENY",
    val xContentTypeOptions: String = "nosniff",
    val xXssProtection: String = "1; mode=block",
    val referrerPolicy: String = "strict-origin-when-cross-origin"
)

/**
 * Ktor plugin that adds security headers to all HTTP responses.
 *
 * Headers included:
 * - Content-Security-Policy: Mitigates XSS attacks by controlling resource loading
 * - Strict-Transport-Security: Forces HTTPS connections (HSTS)
 * - X-Frame-Options: Prevents clickjacking by controlling iframe embedding
 * - X-Content-Type-Options: Prevents MIME type sniffing
 * - X-XSS-Protection: Legacy XSS filter for older browsers
 * - Referrer-Policy: Controls referrer information in cross-origin requests
 */
val SecurityHeadersPlugin = createApplicationPlugin(
    name = "SecurityHeadersPlugin",
    createConfiguration = ::SecurityHeadersConfig
) {
    val config = pluginConfig
    
    onCallRespond { call, _ ->
        call.response.header("Content-Security-Policy", config.contentSecurityPolicy)
        call.response.header("Strict-Transport-Security", config.strictTransportSecurity)
        call.response.header("X-Frame-Options", config.xFrameOptions)
        call.response.header("X-Content-Type-Options", config.xContentTypeOptions)
        call.response.header("X-XSS-Protection", config.xXssProtection)
        call.response.header("Referrer-Policy", config.referrerPolicy)
    }
}
