package com.vibely.pos.backend.security

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond

@Suppress("LabeledExpression")
private const val ERROR_KEY = "error"
private const val CSRF_TOKEN_HEADER = "X-CSRF-Token"
private const val CSRF_TOKEN_RESPONSE_HEADER = "X-CSRF-Token"

/**
 * Configuration for CSRF protection.
 *
 * @property tokenManager CSRF token manager instance
 * @property exemptPaths Paths that are exempt from CSRF validation
 * @property exemptMethods HTTP methods that are exempt from CSRF validation
 */
data class CsrfProtectionConfig(
    val tokenManager: CsrfTokenManager = CsrfTokenManager(),
    val exemptPaths: List<String> = listOf("/api/auth/login", "/api/auth/refresh", "/health", "/"),
    val exemptMethods: Set<HttpMethod> = setOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)
)

/**
 * Ktor plugin that provides CSRF protection for state-changing requests.
 *
 * CSRF tokens are required for POST, PUT, PATCH, DELETE requests.
 * Tokens are validated from the X-CSRF-Token header.
 * GET requests and authenticated JWT requests are exempt (JWT provides inherent CSRF protection).
 *
 * Token lifecycle:
 * - Generated on successful login
 * - Included in X-CSRF-Token response header
 * - Must be sent in X-CSRF-Token request header for state-changing operations
 * - Invalidated on logout
 */
val CsrfProtectionPlugin = createApplicationPlugin(
    name = "CsrfProtectionPlugin",
    createConfiguration = ::CsrfProtectionConfig
) {
    val tokenManager = pluginConfig.tokenManager
    val exemptPaths = pluginConfig.exemptPaths
    val exemptMethods = pluginConfig.exemptMethods
    
    onCall { call ->
        val path = call.request.path()
        val method = call.request.local.method
        
        if (exemptMethods.contains(method) || exemptPaths.any { path.startsWith(it) }) {
            return@onCall
        }
        
        val principal = call.principal<JWTPrincipal>()
        if (principal == null) {
            return@onCall
        }
        
        val userId = principal.payload.getClaim("userId")?.asString()
        if (userId == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf(ERROR_KEY to "User ID not found in token")
            )
            return@onCall
        }
        
        val csrfToken = call.request.headers[CSRF_TOKEN_HEADER]
        
        if (!tokenManager.validateToken(userId, csrfToken)) {
            call.respond(
                HttpStatusCode.Forbidden,
                mapOf(ERROR_KEY to "Invalid or missing CSRF token")
            )
            return@onCall
        }
    }
    
    onCallRespond { call, _ ->
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()
        
        if (userId != null) {
            val token = tokenManager.getToken(userId) ?: tokenManager.generateToken(userId)
            call.response.header(CSRF_TOKEN_RESPONSE_HEADER, token)
        }
    }
}
