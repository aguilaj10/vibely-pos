package com.vibely.pos.backend.config

/**
 * Holds all resolved environment configuration for the application.
 *
 * Built once at startup in the Koin module; injected wherever
 * environment-specific values are needed.
 *
 * @property debugMode Whether the application is running in debug mode (DEBUG_MODE env var)
 * @property jwtSecret Secret used to sign and verify JWT tokens (JWT_SECRET env var)
 * @property enforceHttps Whether HTTP requests should be redirected to HTTPS (ENFORCE_HTTPS env var)
 * @property supabaseUrl Supabase project URL (SUPABASE_URL env var)
 * @property supabaseServiceKey Supabase service role key (SUPABASE_SERVICE_ROLE_KEY env var)
 */
data class AppConfig(
    val debugMode: Boolean,
    val jwtSecret: String,
    val enforceHttps: Boolean,
    val supabaseUrl: String,
    val supabaseServiceKey: String,
)
