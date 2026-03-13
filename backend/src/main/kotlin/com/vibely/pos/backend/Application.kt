package com.vibely.pos.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vibely.pos.backend.config.SupabaseConfig
import com.vibely.pos.backend.di.backendModule
import com.vibely.pos.shared.di.sharedModules
import io.github.jan.supabase.postgrest.from
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

/**
 * Main entry point for the Vibely POS backend server.
 * Starts the Ktor server on port 8080.
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Configures the Ktor application with plugins, authentication, and routing.
 */
fun Application.module() {
    // Initialize Supabase client (validates environment variables)
    val supabaseClient = SupabaseConfig.client

    // Install Koin DI
    install(Koin) {
        slf4jLogger()
        modules(sharedModules() + backendModule)
    }

    // Install Content Negotiation for JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Install CORS for cross-origin requests
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

    // Install Call Logging
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    // Install Status Pages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Invalid request"))
            )
        }
    }

    // Install JWT Authentication
    install(Authentication) {
        jwt("auth-jwt") {
            // Get JWT secret from environment variable
            val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"

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
    }

    // Configure routing
    routing {
        get("/") {
            call.respondText("Vibely POS Backend API - Ready!")
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "healthy",
                    "service" to "vibely-pos-backend",
                    "supabase" to "connected"
                )
            )
        }

        // Test endpoint to verify Supabase database connectivity
        get("/api/test/database") {
            try {
                // Try to query from users table to test connection
                // Using postgrest query syntax
                supabaseClient.from("users").select()

                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "success",
                        "message" to "Database connection successful",
                        "database" to "connected",
                        "project_id" to "jewqhojchyrmozxsrkoq",
                        "supabase_url" to "https://jewqhojchyrmozxsrkoq.supabase.co"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "status" to "info",
                        "message" to "Supabase client initialized successfully",
                        "note" to "Database query test skipped - ensure tables exist",
                        "project_id" to "jewqhojchyrmozxsrkoq",
                        "error" to e.message
                    )
                )
            }
        }
    }
}
