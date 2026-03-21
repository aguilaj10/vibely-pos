package com.vibely.pos.backend.di

import com.vibely.pos.backend.auth.DebugAuthProvider
import com.vibely.pos.backend.auth.ProdAuthProvider
import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.config.AppConfig
import com.vibely.pos.backend.config.SupabaseConfig
import com.vibely.pos.backend.data.DatabaseStrategy
import com.vibely.pos.backend.data.datasource.CategoryBackendDataSource
import com.vibely.pos.backend.data.datasource.CustomerBackendDataSource
import com.vibely.pos.backend.data.datasource.PaymentBackendDataSource
import com.vibely.pos.backend.data.datasource.ProductBackendDataSource
import com.vibely.pos.backend.data.datasource.SaleBackendDataSource
import com.vibely.pos.backend.data.room.AppDatabase
import com.vibely.pos.backend.data.room.createDatabase
import com.vibely.pos.backend.data.room.datasource.RoomCategoryDataSource
import com.vibely.pos.backend.data.room.datasource.RoomCustomerDataSource
import com.vibely.pos.backend.data.room.datasource.RoomPaymentDataSource
import com.vibely.pos.backend.data.room.datasource.RoomProductDataSource
import com.vibely.pos.backend.data.room.datasource.RoomSaleDataSource
import com.vibely.pos.backend.data.supabase.SupabaseCategoryDataSource
import com.vibely.pos.backend.data.supabase.SupabaseCustomerDataSource
import com.vibely.pos.backend.data.supabase.SupabasePaymentDataSource
import com.vibely.pos.backend.data.supabase.SupabaseProductDataSource
import com.vibely.pos.backend.data.supabase.SupabaseSaleDataSource
import com.vibely.pos.backend.security.CsrfTokenManager
import com.vibely.pos.backend.security.RateLimiter
import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.backend.services.CategoryService
import com.vibely.pos.backend.services.CurrencyService
import com.vibely.pos.backend.services.CustomerService
import com.vibely.pos.backend.services.DashboardService
import com.vibely.pos.backend.services.InventoryService
import com.vibely.pos.backend.services.PaymentService
import com.vibely.pos.backend.services.ProductService
import com.vibely.pos.backend.services.PurchaseOrderService
import com.vibely.pos.backend.services.ReportService
import com.vibely.pos.backend.services.SaleService
import com.vibely.pos.backend.services.SettingsService
import com.vibely.pos.backend.services.ShiftService
import com.vibely.pos.backend.services.SupplierService
import com.vibely.pos.backend.services.TokenService
import com.vibely.pos.backend.services.UserManagementService
import com.vibely.pos.backend.services.UserRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.Module
import org.koin.dsl.module

private const val ENV_DEBUG_MODE = "DEBUG_MODE"
private const val ENV_JWT_SECRET = "JWT_SECRET"
private const val ENV_ENFORCE_HTTPS = "ENFORCE_HTTPS"
private const val ENV_SUPABASE_URL = "SUPABASE_URL"
private const val ENV_SUPABASE_SERVICE_KEY = "SUPABASE_SERVICE_ROLE_KEY"
private const val DEFAULT_JWT_SECRET = "default-secret-key-change-in-production"

private fun selectedDataSourceModule(): Module =
    when (DatabaseStrategy.current) {
        is DatabaseStrategy.Remote -> createRemoteDataSourceModule()
        is DatabaseStrategy.Local -> createLocalDataSourceModule()
    }

/** Koin sub-module wiring Supabase as the data source for all feature services. */
private fun createRemoteDataSourceModule() =
    module {
        single<ProductBackendDataSource> { SupabaseProductDataSource(get()) }
        single<CategoryBackendDataSource> { SupabaseCategoryDataSource(get()) }
        single<CustomerBackendDataSource> { SupabaseCustomerDataSource(get()) }
        single<SaleBackendDataSource> { SupabaseSaleDataSource(get()) }
        single<PaymentBackendDataSource> { SupabasePaymentDataSource(get()) }
    }

/** Koin sub-module wiring Room/SQLite as the data source for all features. */
private fun createLocalDataSourceModule() =
    module {
        single<AppDatabase> { createDatabase() }

        single { get<AppDatabase>().productDao() }
        single { get<AppDatabase>().categoryDao() }
        single { get<AppDatabase>().customerDao() }
        single { get<AppDatabase>().saleDao() }
        single { get<AppDatabase>().paymentDao() }

        single<ProductBackendDataSource> { RoomProductDataSource(get()) }
        single<CategoryBackendDataSource> { RoomCategoryDataSource(get()) }
        single<CustomerBackendDataSource> { RoomCustomerDataSource(get()) }
        single<SaleBackendDataSource> { RoomSaleDataSource(get(), get()) }
        single<PaymentBackendDataSource> { RoomPaymentDataSource(get(), get()) }
    }

@Suppress("UndocumentedPublicProperty")
val backendModule =
    module {
        single {
            AppConfig(
                debugMode = System.getenv(ENV_DEBUG_MODE)?.toBoolean() == true,
                jwtSecret = System.getenv(ENV_JWT_SECRET) ?: DEFAULT_JWT_SECRET,
                enforceHttps = System.getenv(ENV_ENFORCE_HTTPS)?.toBoolean() ?: false,
                supabaseUrl = System.getenv(ENV_SUPABASE_URL),
                supabaseServiceKey = System.getenv(ENV_SUPABASE_SERVICE_KEY),
            )
        }

        // Register SupabaseClient only when env vars are provided.
        // Required even in Local mode for out-of-scope services (Auth, Dashboard, etc.).
        single<SupabaseClient> {
            val config: AppConfig = get()
            SupabaseConfig.createClient(
                requireNotNull(config.supabaseUrl) {
                    "SUPABASE_URL is required (needed by Auth/Dashboard services)"
                },
                requireNotNull(config.supabaseServiceKey) {
                    "SUPABASE_SERVICE_ROLE_KEY is required (needed by Auth/Dashboard services)"
                },
                config.debugMode,
            )
        }

        // Single decision point: selects Supabase or Room data sources for the 5 feature services
        includes(selectedDataSourceModule())

        single<RouteAuthProvider> {
            val config: AppConfig = get()
            if (config.debugMode) DebugAuthProvider() else ProdAuthProvider()
        }

        single { RateLimiter() }

        single { CsrfTokenManager() }

        single { UserRepository(get()) }

        single { TokenService(get(), get<AppConfig>().jwtSecret) }

        single { AuthService(get(), get()) }

        single { DashboardService(get()) }

        single { ProductService(get()) }

        single { SaleService(get()) }

        single { PaymentService(get()) }

        single { CategoryService(get()) }

        single { InventoryService(get()) }

        single { CustomerService(get()) }

        single { SupplierService(get()) }

        single { PurchaseOrderService(get(), get()) }

        single { ShiftService(get()) }

        single { UserManagementService(get(), get()) }

        single { ReportService(get()) }

        single { SettingsService(get()) }

        single { CurrencyService(get()) }
    }

