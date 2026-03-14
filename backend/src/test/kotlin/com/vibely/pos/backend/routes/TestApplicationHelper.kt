package com.vibely.pos.backend.routes

import com.vibely.pos.backend.config.configureTestAuthentication
import com.vibely.pos.backend.services.AuthService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json

/**
 * Helper to configure test application with authentication and content negotiation.
 *
 * Reduces boilerplate in route tests by providing a standard setup.
 */
fun Application.configureTestApplication(authService: AuthService) {
    configureTestAuthentication()
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    routing {
        authRoutes(authService)
    }
}

/**
 * Creates a test HTTP client with JSON content negotiation configured.
 */
fun ApplicationTestBuilder.createTestClient(): HttpClient {
    return createClient {
        install(ClientContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
}
