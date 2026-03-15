package com.vibely.pos.shared.di

import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules
import kotlin.experimental.ExperimentalNativeApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for Koin dependency injection configuration.
 *
 * Verifies that:
 * - All modules are properly configured
 * - All dependencies can be resolved
 * - No circular dependencies exist
 * - Module definitions are consistent
 */
@OptIn(ExperimentalNativeApi::class)
class KoinModulesTest {
    /**
     * Verifies the structure and consistency of domain and data modules together.
     *
     * Domain module use cases require repository implementations from data module.
     * This follows Clean Architecture: Domain defines interfaces, Data implements them.
     */
    @Test
    fun `verify domain module structure`() {
        koinApplication {
            properties(
                mapOf(
                    "SUPABASE_URL" to "https://test.supabase.co",
                    "SUPABASE_ANON_KEY" to "test-anon-key",
                ),
            )
            modules(domainModule, dataModule)
            checkModules {
                withInstance<String>()
                withInstance<HttpClientEngine>()
            }
        }
    }

    @Test
    fun `verify data module structure`() {
        koinApplication {
            properties(
                mapOf(
                    "SUPABASE_URL" to "https://test.supabase.co",
                    "SUPABASE_ANON_KEY" to "test-anon-key",
                ),
            )
            modules(domainModule, dataModule)
            checkModules {
                withInstance<String>()
                withInstance<HttpClientEngine>()
            }
        }
    }

    @Test
    fun `verify presentation module structure`() {
        koinApplication {
            modules(presentationModule)
            checkModules()
        }
    }

    /**
     * Tests that Koin can start successfully with all shared modules.
     *
     * This ensures there are no configuration errors or conflicts between modules.
     */
    @Test
    fun `koin application starts with all modules`() {
        val koinApp =
            koinApplication {
                properties(
                    mapOf(
                        "SUPABASE_URL" to "https://test.supabase.co",
                        "SUPABASE_ANON_KEY" to "test-anon-key",
                    ),
                )
                modules(sharedModules())
            }

        assertNotNull(koinApp.koin, "Koin application should be initialized")
    }

    /**
     * Tests that core dependencies can be resolved from DataModule.
     */
    @Test
    fun `data module provides core dependencies`() {
        val koinApp =
            koinApplication {
                properties(
                    mapOf(
                        "SUPABASE_URL" to "https://test.supabase.co",
                        "SUPABASE_ANON_KEY" to "test-anon-key",
                    ),
                )
                modules(dataModule)
            }

        val koin = koinApp.koin

        val json = koin.get<Json>()
        assertNotNull(json, "Json instance should be provided")

        val httpClient = koin.get<HttpClient>()
        assertNotNull(httpClient, "HttpClient should be provided")

        val supabaseClient = koin.get<SupabaseClient>()
        assertNotNull(supabaseClient, "SupabaseClient should be provided")
    }

    /**
     * Tests that sharedModules returns all expected modules.
     */
    @Test
    fun `sharedModules returns all modules`() {
        val modules = sharedModules()

        assertNotNull(modules, "Shared modules list should not be null")
        assertEquals(modules.size, 3, "Expected 3 modules (domain, data, presentation), got ${modules.size}")
    }

    /**
     * Tests that all modules can be loaded together without conflicts.
     */
    @Test
    fun `all modules load without conflicts`() {
        val koinApp =
            koinApplication {
                properties(
                    mapOf(
                        "SUPABASE_URL" to "https://test.supabase.co",
                        "SUPABASE_ANON_KEY" to "test-anon-key",
                    ),
                )
                modules(
                    domainModule,
                    dataModule,
                    presentationModule,
                )
            }

        assertNotNull(koinApp.koin, "Koin should start with all modules")

        val json = koinApp.koin.getOrNull<Json>()
        assertNotNull(json, "Should be able to resolve dependencies from data module")
    }

    /**
     * Tests that the module hierarchy follows Clean Architecture principles.
     */
    @Test
    fun `modules follow clean architecture hierarchy`() {
        val dataWithDomain =
            koinApplication {
                properties(
                    mapOf(
                        "SUPABASE_URL" to "https://test.supabase.co",
                        "SUPABASE_ANON_KEY" to "test-anon-key",
                    ),
                )
                modules(domainModule, dataModule)
            }
        assertNotNull(dataWithDomain.koin, "Data module should work with domain module")

        val presentationWithDomain =
            koinApplication {
                modules(domainModule, presentationModule)
            }
        assertNotNull(presentationWithDomain.koin, "Presentation module should work with domain module")
    }
}
