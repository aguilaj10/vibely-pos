package com.vibely.pos.backend.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer

/**
 * Configures test authentication for unit tests.
 *
 * This provides a simple bearer token authentication that accepts tokens
 * in the format "test-user-{userId}" for testing authenticated routes
 * without the overhead of real JWT verification.
 *
 * Example usage:
 * ```
 * testApplication {
 *     application {
 *         configureTestAuthentication()
 *         routing { authRoutes(mockAuthService) }
 *     }
 *
 *     client.get("/api/auth/me") {
 *         bearerAuth("test-user-123")
 *     }
 * }
 * ```
 */
fun Application.configureTestAuthentication() {
    install(Authentication) {
        bearer("auth-jwt") {
            authenticate { credential ->
                // Accept any token in format "test-user-{id}"
                val userId = credential.token.removePrefix("test-user-")
                if (userId.isNotEmpty() && userId != credential.token) {
                    UserIdPrincipal(userId)
                } else {
                    null
                }
            }
        }
    }
}
