package com.vibely.pos.backend.di

import com.vibely.pos.backend.auth.DebugAuthProvider
import com.vibely.pos.backend.auth.ProdAuthProvider
import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.config.AppConfig
import com.vibely.pos.backend.config.SupabaseConfig
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
import org.koin.dsl.module

private const val ENV_DEBUG_MODE = "DEBUG_MODE"
private const val ENV_JWT_SECRET = "JWT_SECRET"
private const val ENV_ENFORCE_HTTPS = "ENFORCE_HTTPS"
private const val ENV_SUPABASE_URL = "SUPABASE_URL"
private const val ENV_SUPABASE_SERVICE_KEY = "SUPABASE_SERVICE_ROLE_KEY"
private const val DEFAULT_JWT_SECRET = "default-secret-key-change-in-production"

@Suppress("UndocumentedPublicProperty")
val backendModule =
    module {
        single {
            AppConfig(
                debugMode = System.getenv(ENV_DEBUG_MODE)?.toBoolean() == true,
                jwtSecret = System.getenv(ENV_JWT_SECRET) ?: DEFAULT_JWT_SECRET,
                enforceHttps = System.getenv(ENV_ENFORCE_HTTPS)?.toBoolean() ?: false,
                supabaseUrl = System.getenv(ENV_SUPABASE_URL)
                    ?: error("$ENV_SUPABASE_URL environment variable is not set"),
                supabaseServiceKey = System.getenv(ENV_SUPABASE_SERVICE_KEY)
                    ?: error("$ENV_SUPABASE_SERVICE_KEY environment variable is not set"),
            )
        }

        single<SupabaseClient> {
            val config: AppConfig = get()
            SupabaseConfig.createClient(config.supabaseUrl, config.supabaseServiceKey, config.debugMode)
        }

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
