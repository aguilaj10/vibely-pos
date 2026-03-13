package com.vibely.pos.backend

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Main entry point for the Vibely POS backend server.
 * Starts the Ktor server on port 8080.
 */
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Configures the Ktor application with routing and endpoints.
 */
fun Application.module() {
    routing {
        get("/") {
            call.respondText("Vibely POS Backend API - Ready!")
        }
        get("/health") {
            call.respondText("OK")
        }
    }
}
