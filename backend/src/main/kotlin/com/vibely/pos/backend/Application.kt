package com.vibely.pos.backend

import com.vibely.pos.backend.config.SupabaseConfig
import com.vibely.pos.backend.config.configureAuthentication
import com.vibely.pos.backend.config.configureCORS
import com.vibely.pos.backend.config.configureCallLogging
import com.vibely.pos.backend.config.configureContentNegotiation
import com.vibely.pos.backend.config.configureKoin
import com.vibely.pos.backend.config.configureStatusPages
import com.vibely.pos.backend.routes.authRoutes
import com.vibely.pos.backend.routes.categoryRoutes
import com.vibely.pos.backend.routes.dashboardRoutes
import com.vibely.pos.backend.routes.inventoryRoutes
import com.vibely.pos.backend.routes.productRoutes
import com.vibely.pos.backend.routes.salesRoutes
import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.backend.services.CategoryService
import com.vibely.pos.backend.services.DashboardService
import com.vibely.pos.backend.services.InventoryService
import com.vibely.pos.backend.services.ProductService
import com.vibely.pos.backend.services.SaleService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

// Constants
private const val SERVER_PORT = 8080
private const val SERVER_HOST = "0.0.0.0"
private const val ERROR_KEY = "error"
private const val STATUS_KEY = "status"
private const val MESSAGE_KEY = "message"
private const val SUPABASE_PROJECT_ID = "jewqhojchyrmozxsrkoq"
private const val SUPABASE_URL = "https://jewqhojchyrmozxsrkoq.supabase.co"

/**
 * Main entry point for the Vibely POS backend server.
 * Starts the Ktor server on port 8080.
 */
fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = SERVER_HOST, module = Application::module)
        .start(wait = true)
}

/**
 * Configures the Ktor application with plugins, authentication, and routing.
 */
fun Application.module() {
    // Initialize Supabase client (validates environment variables)
    val supabaseClient = SupabaseConfig.client

    // Install all plugins
    configureKoin()
    configureContentNegotiation()
    configureCORS()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication()

    // Configure routing
    configureRouting(supabaseClient)
}

/**
 * Configures application routing with health checks and test endpoints.
 */
private fun Application.configureRouting(supabaseClient: SupabaseClient) {
    val authService: Lazy<AuthService> = inject()
    val dashboardService: Lazy<DashboardService> = inject()
    val productService: Lazy<ProductService> = inject()
    val saleService: Lazy<SaleService> = inject()
    val categoryService: Lazy<CategoryService> = inject()
    val inventoryService: Lazy<InventoryService> = inject()
    
    routing {
        get("/") {
            call.respondText("Vibely POS Backend API - Ready!")
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    STATUS_KEY to "healthy",
                    "service" to "vibely-pos-backend",
                    "supabase" to "connected"
                )
            )
        }

        // Test endpoint to verify Supabase database connectivity
        get("/api/test/database") {
            handleDatabaseTest(supabaseClient)
        }

        // Authentication routes
        authRoutes(authService.value)

        // Dashboard routes
        dashboardRoutes(dashboardService.value)

        // Product routes
        productRoutes(productService.value)

        // Sales routes
        salesRoutes(saleService.value)

        // Category routes
        categoryRoutes(categoryService.value)

        // Inventory routes
        inventoryRoutes(inventoryService.value)
    }
}

/**
 * Handles the database connectivity test endpoint.
 */
private suspend fun RoutingContext.handleDatabaseTest(supabaseClient: SupabaseClient) {
    try {
        // Try to query from users table to test connection
        supabaseClient.from("users").select()

        call.respond(
            HttpStatusCode.OK,
            mapOf(
                STATUS_KEY to "success",
                MESSAGE_KEY to "Database connection successful",
                "database" to "connected",
                "project_id" to SUPABASE_PROJECT_ID,
                "supabase_url" to SUPABASE_URL
            )
        )
    } catch (e: RestException) {
        call.respond(
            HttpStatusCode.OK,
            mapOf(
                STATUS_KEY to "info",
                MESSAGE_KEY to "Supabase client initialized successfully",
                "note" to "Database query test skipped - ensure tables exist",
                "project_id" to SUPABASE_PROJECT_ID,
                ERROR_KEY to (e.message ?: "Unknown error")
            )
        )
    } catch (e: IllegalStateException) {
        call.respond(
            HttpStatusCode.InternalServerError,
            mapOf(
                STATUS_KEY to ERROR_KEY,
                MESSAGE_KEY to "Configuration error",
                ERROR_KEY to (e.message ?: "Unknown configuration error")
            )
        )
    }
}
