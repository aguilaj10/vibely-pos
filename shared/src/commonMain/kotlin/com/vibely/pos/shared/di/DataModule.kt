package com.vibely.pos.shared.di

import com.vibely.pos.shared.data.auth.datasource.InMemoryAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.RemoteAuthDataSource
import com.vibely.pos.shared.data.auth.repository.AuthRepositoryImpl
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for the Data layer.
 *
 * Provides concrete implementations of repository interfaces defined in the domain layer,
 * as well as data sources (remote APIs, local databases, caches).
 *
 * Includes configuration for:
 * - Supabase client (Postgrest and Storage)
 * - HTTP client for API communication
 * - JSON serialization
 *
 * As repositories and data sources are implemented, register them here:
 * ```
 * val dataModule = module {
 *     singleOf(::ProductRepositoryImpl) { bind<ProductRepository>() }
 *     singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }
 *     singleOf(::RemoteProductDataSource)
 * }
 * ```
 */
val dataModule =
    module {
        // JSON Configuration
        single {
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        }

        // HTTP Client for Supabase and general API calls
        single {
            HttpClient {
                install(ContentNegotiation) {
                    json(get())
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }

        // Supabase Client
        single<SupabaseClient> {
            createSupabaseClient(
                supabaseUrl = getProperty("SUPABASE_URL", ""),
                supabaseKey = getProperty("SUPABASE_ANON_KEY", ""),
            ) {
                install(Postgrest)
                install(Storage)
            }
        }

        // Auth data sources
        singleOf(::InMemoryAuthDataSource) { bind<LocalAuthDataSource>() }
        single {
            RemoteAuthDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Auth repository
        singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

        // Other repository implementations will be registered here
        // Example:
        // singleOf(::ProductRepositoryImpl) { bind<ProductRepository>() }
        // singleOf(::OrderRepositoryImpl) { bind<OrderRepository>() }
    }
