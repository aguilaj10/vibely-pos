package com.vibely.pos.backend.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.github.jan.supabase.logging.LogLevel as SupabaseLogLevel
import io.ktor.client.plugins.logging.LogLevel as KtorLogLevel

/**
 * Configuration object for Supabase client initialization.
 */
object SupabaseConfig {
    /**
     * The Supabase project URL.
     * Should be set via SUPABASE_URL environment variable.
     */
    private val supabaseUrl: String = System.getenv("SUPABASE_URL")
        ?: error("SUPABASE_URL environment variable is not set")

    /**
     * The Supabase service role key (for backend server use only).
     * Should be set via SUPABASE_SERVICE_ROLE_KEY environment variable.
     */
    private val supabaseServiceKey: String = System.getenv("SUPABASE_SERVICE_ROLE_KEY")
        ?: error("SUPABASE_SERVICE_ROLE_KEY environment variable is not set")

    /**
     * Lazy-initialized singleton Supabase client.
     */
    val client: SupabaseClient by lazy { createClient() }

    /**
     * Creates and configures a Supabase client instance.
     * Uses CIO engine for HTTP client and enables Postgrest module.
     *
     * In debug mode, enables verbose HTTP logging to see full PostgREST queries.
     */
    @OptIn(SupabaseInternal::class)
    private fun createClient(): SupabaseClient {
        val isDebugMode = System.getenv("DEBUG_MODE")?.toBoolean() == true

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseServiceKey
        ) {
            if (isDebugMode) {
                defaultLogLevel = SupabaseLogLevel.DEBUG

                httpConfig {
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = KtorLogLevel.ALL

                        sanitizeHeader { header ->
                            header == HttpHeaders.Authorization ||
                            header.equals("apikey", ignoreCase = true)
                        }
                    }
                }
            }

            install(Postgrest)
            httpEngine = CIO.create()
        }
    }
}
