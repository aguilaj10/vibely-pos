package com.vibely.pos.backend.di

import com.vibely.pos.backend.config.SupabaseConfig
import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.backend.services.TokenService
import com.vibely.pos.backend.services.UserRepository
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

        // User repository
        single { UserRepository(get()) }

        // Token service
        single { TokenService(get()) }

        // Authentication service
        single { AuthService(get(), get()) }
    }
