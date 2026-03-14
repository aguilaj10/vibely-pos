package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.shared.data.auth.dto.LoginRequestDTO
import com.vibely.pos.shared.data.auth.dto.RefreshTokenRequestDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

// Error message constants
private const val ERROR_INVALID_CREDENTIALS = "Invalid email or password"
private const val ERROR_INVALID_TOKEN = "Invalid token"
private const val ERROR_NO_TOKEN = "No token provided"
private const val ERROR_TOKEN_REVOKED = "Token has been revoked"
private const val ERROR_USER_NOT_FOUND = "User not found"
private const val ERROR_REFRESH_REQUIRED = "Refresh token is required"
private const val ERROR_INVALID_REFRESH = "Invalid or expired refresh token"
private const val ERROR_EMAIL_PASSWORD_REQUIRED = "Email and password are required"

private const val ERROR = "error"

/**
 * Configures authentication routes.
 *
 * Endpoints:
 * - POST /api/auth/login - Authenticate and issue tokens
 * - POST /api/auth/logout - Invalidate session
 * - GET /api/auth/me - Get current authenticated user
 * - POST /api/auth/refresh - Refresh access token
 */
fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/login") { call.handleLogin(authService) }
        authenticate("auth-jwt") {
            post("/logout") { call.handleLogout(authService) }
            get("/me") { call.handleGetCurrentUser(authService) }
        }
        post("/refresh") { call.handleRefreshToken(authService) }
    }
}

/**
 * POST /api/auth/login
 * Authenticates a user with email and password.
 */
private suspend fun ApplicationCall.handleLogin(authService: AuthService) {
    val request = receive<LoginRequestDTO>()

    if (request.email.isBlank() || request.password.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR to ERROR_EMAIL_PASSWORD_REQUIRED))
        return
    }

    val authResponse = authService.login(request.email, request.password)
    if (authResponse == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR to ERROR_INVALID_CREDENTIALS))
        return
    }

    respond(HttpStatusCode.OK, authResponse)
}

/**
 * POST /api/auth/logout
 * Logs out the current user by blacklisting their access token.
 */
private suspend fun ApplicationCall.handleLogout(authService: AuthService) {
    val userId = extractUserId() ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR to ERROR_INVALID_TOKEN))
        return
    }

    val token = extractBearerToken() ?: run {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR to ERROR_NO_TOKEN))
        return
    }

    authService.logout(token, userId)
    respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
}

/**
 * GET /api/auth/me
 * Returns the current authenticated user's information.
 */
private suspend fun ApplicationCall.handleGetCurrentUser(authService: AuthService) {
    val userId = extractUserId() ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR to ERROR_INVALID_TOKEN))
        return
    }

    val token = extractBearerToken()
    if (token != null && authService.isTokenBlacklisted(token)) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR to ERROR_TOKEN_REVOKED))
        return
    }

    val user = authService.getCurrentUser(userId)
    if (user == null) {
        respond(HttpStatusCode.NotFound, mapOf(ERROR to ERROR_USER_NOT_FOUND))
        return
    }

    respond(HttpStatusCode.OK, user)
}

/**
 * POST /api/auth/refresh
 * Refreshes an access token using a refresh token.
 */
private suspend fun ApplicationCall.handleRefreshToken(authService: AuthService) {
    val request = receive<RefreshTokenRequestDTO>()

    if (request.refreshToken.isBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf(ERROR to ERROR_REFRESH_REQUIRED))
        return
    }

    val authResponse = authService.refreshAccessToken(request.refreshToken)
    if (authResponse == null) {
        respond(HttpStatusCode.Unauthorized, mapOf(ERROR to ERROR_INVALID_REFRESH))
        return
    }

    respond(HttpStatusCode.OK, authResponse)
}

/**
 * Extracts user ID from JWT principal.
 */
private fun ApplicationCall.extractUserId(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()

/**
 * Extracts bearer token from Authorization header.
 */
private fun ApplicationCall.extractBearerToken(): String? =
    request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
