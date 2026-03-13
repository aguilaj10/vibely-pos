package com.vibely.pos.backend.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.cio.CIO

/**
 * Configuration object for Supabase client initialization.
 */
object SupabaseConfig {
    /**
     * The Supabase project URL.
     * Should be set via SUPABASE_URL environment variable.
     */
    private val supabaseUrl: String = System.getenv("SUPABASE_URL")
        ?: throw IllegalStateException("SUPABASE_URL environment variable is not set")

    /**
     * The Supabase service role key (for backend server use only).
     * Should be set via SUPABASE_SERVICE_ROLE_KEY environment variable.
     */
    private val supabaseServiceKey: String = System.getenv("SUPABASE_SERVICE_ROLE_KEY")
        ?: throw IllegalStateException("SUPABASE_SERVICE_ROLE_KEY environment variable is not set")

    /**
     * Creates and configures a Supabase client instance.
     * Uses CIO engine for HTTP client and enables Postgrest module.
     */
    fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseServiceKey
        ) {
            install(Postgrest)

            // Configure HTTP client engine
            httpEngine = CIO.create()
        }
    }

    /**
     * Lazy-initialized singleton Supabase client.
     */
    val client: SupabaseClient by lazy { createClient() }
}
