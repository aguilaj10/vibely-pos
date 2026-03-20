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
     * Creates and configures a Supabase client instance.
     *
     * @param supabaseUrl The Supabase project URL
     * @param supabaseServiceKey The Supabase service role key
     * @param isDebugMode When true, enables verbose HTTP logging for PostgREST queries
     */
    @OptIn(SupabaseInternal::class)
    fun createClient(supabaseUrl: String, supabaseServiceKey: String, isDebugMode: Boolean): SupabaseClient {
        return createSupabaseClient(supabaseUrl = supabaseUrl, supabaseKey = supabaseServiceKey) {
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
