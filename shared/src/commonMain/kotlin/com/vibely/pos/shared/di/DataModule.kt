package com.vibely.pos.shared.di

import com.vibely.pos.shared.Platform
import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.RemoteAuthDataSource
import com.vibely.pos.shared.data.auth.repository.AuthRepositoryImpl
import com.vibely.pos.shared.data.auth.storage.PlatformAuthStorageFactory
import com.vibely.pos.shared.data.auth.storage.configurePlatformHttpClient
import com.vibely.pos.shared.data.currency.datasource.RemoteCurrencyDataSource
import com.vibely.pos.shared.data.currency.repository.CurrencyRepositoryImpl
import com.vibely.pos.shared.data.customer.datasource.RemoteCustomerDataSource
import com.vibely.pos.shared.data.customer.repository.CustomerRepositoryImpl
import com.vibely.pos.shared.data.dashboard.datasource.RemoteDashboardDataSource
import com.vibely.pos.shared.data.dashboard.repository.DashboardRepositoryImpl
import com.vibely.pos.shared.data.inventory.datasource.RemoteCategoryDataSource
import com.vibely.pos.shared.data.inventory.datasource.RemoteInventoryDataSource
import com.vibely.pos.shared.data.inventory.repository.CategoryRepositoryImpl
import com.vibely.pos.shared.data.inventory.repository.InventoryRepositoryImpl
import com.vibely.pos.shared.data.purchaseorder.datasource.RemotePurchaseOrderDataSource
import com.vibely.pos.shared.data.purchaseorder.repository.PurchaseOrderRepositoryImpl
import com.vibely.pos.shared.data.reports.datasource.RemoteReportsDataSource
import com.vibely.pos.shared.data.reports.repository.ReportsRepositoryImpl
import com.vibely.pos.shared.data.sales.datasource.RemotePaymentDataSource
import com.vibely.pos.shared.data.sales.datasource.RemoteProductDataSource
import com.vibely.pos.shared.data.sales.datasource.RemoteSaleDataSource
import com.vibely.pos.shared.data.sales.repository.PaymentRepositoryImpl
import com.vibely.pos.shared.data.sales.repository.ProductRepositoryImpl
import com.vibely.pos.shared.data.sales.repository.SaleRepositoryImpl
import com.vibely.pos.shared.data.shift.datasource.RemoteShiftDataSource
import com.vibely.pos.shared.data.shift.repository.ShiftRepositoryImpl
import com.vibely.pos.shared.data.supplier.datasource.RemoteSupplierDataSource
import com.vibely.pos.shared.data.supplier.repository.SupplierRepositoryImpl
import com.vibely.pos.shared.data.user.datasource.RemoteUserDataSource
import com.vibely.pos.shared.data.user.repository.UserRepositoryImpl
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.currency.repository.CurrencyRepository
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.inventory.repository.CategoryRepository
import com.vibely.pos.shared.domain.inventory.repository.InventoryRepository
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.reports.repository.ReportsRepository
import com.vibely.pos.shared.domain.sales.repository.PaymentRepository
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.shift.repository.ShiftRepository
import com.vibely.pos.shared.domain.supplier.repository.SupplierRepository
import com.vibely.pos.shared.domain.user.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
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
            val isDebugMode = getProperty("DEBUG_MODE", "false") == "true"
            val localAuthDataSource: LocalAuthDataSource = get()
            val platformName = Platform.name

            HttpClient {
                configurePlatformHttpClient()

                install(ContentNegotiation) {
                    json(get())
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }

                defaultRequest {
                    header("X-Client-Platform", platformName)
                }

                // Install Auth plugin for Bearer token
                install(Auth) {
                    bearer {
                        cacheTokens = false
                        loadTokens {
                            // In debug mode, send a special debug token
                            if (isDebugMode) {
                                println("⚠️ DEBUG MODE: Using debug access token")
                                BearerTokens(
                                    accessToken = "debug-access-token",
                                    refreshToken = "debug-refresh-token",
                                )
                            } else {
                                val storedToken = localAuthDataSource.getToken().getOrNull()
                                if (storedToken == null || storedToken.isExpired()) {
                                    null
                                } else {
                                    BearerTokens(
                                        accessToken = storedToken.accessToken,
                                        refreshToken = storedToken.refreshToken,
                                    )
                                }
                            }
                        }
                    }
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
        single<LocalAuthDataSource> { PlatformAuthStorageFactory.createLocalAuthDataSource() }
        single {
            RemoteAuthDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Auth repository
        singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

        // Dashboard data sources
        single {
            RemoteDashboardDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Dashboard repository
        singleOf(::DashboardRepositoryImpl) { bind<DashboardRepository>() }

        // Sales data sources
        single {
            RemoteProductDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }
        single {
            RemoteSaleDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Sales repositories
        singleOf(::ProductRepositoryImpl) { bind<ProductRepository>() }
        singleOf(::SaleRepositoryImpl) { bind<SaleRepository>() }

        // Payment data source
        single {
            RemotePaymentDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Payment repository
        singleOf(::PaymentRepositoryImpl) { bind<PaymentRepository>() }

        // Inventory data sources
        single {
            RemoteInventoryDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }
        single {
            RemoteCategoryDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Inventory repositories
        singleOf(::InventoryRepositoryImpl) { bind<InventoryRepository>() }
        singleOf(::CategoryRepositoryImpl) { bind<CategoryRepository>() }

        // Customer data sources
        single {
            RemoteCustomerDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Customer repositories
        singleOf(::CustomerRepositoryImpl) { bind<CustomerRepository>() }

        // Supplier data sources
        single {
            RemoteSupplierDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Supplier repositories
        singleOf(::SupplierRepositoryImpl) { bind<SupplierRepository>() }

        // PurchaseOrder data sources
        single {
            RemotePurchaseOrderDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // PurchaseOrder repositories
        singleOf(::PurchaseOrderRepositoryImpl) { bind<PurchaseOrderRepository>() }

        // Shift data sources
        single {
            RemoteShiftDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Shift repositories
        singleOf(::ShiftRepositoryImpl) { bind<ShiftRepository>() }

        // User data sources
        single {
            RemoteUserDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // User repositories
        singleOf(::UserRepositoryImpl) { bind<UserRepository>() }

        // Reports data sources
        single {
            RemoteReportsDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Reports repositories
        singleOf(::ReportsRepositoryImpl) { bind<ReportsRepository>() }

        // Currency data sources
        single {
            RemoteCurrencyDataSource(
                httpClient = get(),
                baseUrl = getProperty("API_BASE_URL", "http://localhost:8080"),
            )
        }

        // Currency repositories
        singleOf(::CurrencyRepositoryImpl) { bind<CurrencyRepository>() }
    }
