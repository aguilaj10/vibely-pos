package com.vibely.pos.backend.di

import com.vibely.pos.backend.config.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module

/**
 * Koin module for backend-specific dependencies.
 *
 * Provides:
 * - [SupabaseClient] singleton via [SupabaseConfig]
 * - Backend-specific services and repositories
 */
val backendModule =
    module {
        // Supabase client singleton
        single<SupabaseClient> { SupabaseConfig.client }

        // Backend-specific services will be registered here
        // Example:
        // singleOf(::AuthService)
        // singleOf(::PaymentService)
    }
