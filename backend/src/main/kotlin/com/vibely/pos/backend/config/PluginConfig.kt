package com.vibely.pos.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vibely.pos.backend.di.backendModule
import com.vibely.pos.shared.di.sharedModules
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

private const val ERROR_KEY = "error"

/**
 * Configures Koin dependency injection.
 */
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(sharedModules() + backendModule)
    }
}

/**
 * Configures JSON content negotiation.
 */
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

/**
 * Configures CORS for cross-origin requests.
 */
fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // Allow any host for development (should be restricted in production)
        anyHost()
    }
}

/**
 * Configures request/response logging.
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

/**
 * Configures global error handling.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(ERROR_KEY to (cause.message ?: "Unknown error"))
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(ERROR_KEY to (cause.message ?: "Invalid request"))
            )
        }
    }
}

/**
 * Configures JWT authentication.
 */
fun Application.configureAuthentication() {
    val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true
    val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        // Debug auth - simple bearer that accepts debug token
        if (isDebugMode) {
            runDebugMode()
        }
    }
}

private fun AuthenticationConfig.runDebugMode() {
    println("⚠️ DEBUG MODE ENABLED ON BACKEND - Accepting debug-access-token via debug-bearer auth")
    bearer("debug-bearer") {
        authenticate { tokenCredential ->
            if (tokenCredential.token == "debug-access-token") {
                UserIdPrincipal("debug-user-123")
            } else {
                null
            }
        }
    }
}
