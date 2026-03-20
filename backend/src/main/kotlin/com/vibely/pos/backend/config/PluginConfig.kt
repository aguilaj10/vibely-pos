package com.vibely.pos.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vibely.pos.backend.auth.RouteAuthProvider
import com.vibely.pos.backend.di.backendModule
import com.vibely.pos.backend.security.CsrfProtectionConfig
import com.vibely.pos.backend.security.CsrfProtectionPlugin
import com.vibely.pos.backend.security.CsrfTokenManager
import com.vibely.pos.backend.security.HttpsEnforcementPlugin
import com.vibely.pos.backend.security.RateLimiter
import com.vibely.pos.backend.security.RateLimitingConfig
import com.vibely.pos.backend.security.RateLimitingPlugin
import com.vibely.pos.backend.security.SecurityHeadersPlugin
import com.vibely.pos.shared.data.auth.dto.AuthResponseDTO
import com.vibely.pos.shared.data.auth.dto.LoginRequestDTO
import com.vibely.pos.shared.data.auth.dto.RefreshTokenRequestDTO
import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.data.currency.dto.CurrencyExchangeRateDTO
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.data.dashboard.dto.ActiveShiftInfoDTO
import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import com.vibely.pos.shared.data.inventory.dto.CategoryDTO
import com.vibely.pos.shared.data.inventory.dto.InventoryTransactionDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderItemDTO
import com.vibely.pos.shared.data.purchaseorder.dto.PurchaseOrderWithItemsDTO
import com.vibely.pos.shared.data.reports.dto.CategoryBreakdownDTO
import com.vibely.pos.shared.data.reports.dto.CustomerAnalyticsDTO
import com.vibely.pos.shared.data.reports.dto.ProductPerformanceDTO
import com.vibely.pos.shared.data.reports.dto.SalesReportDTO
import com.vibely.pos.shared.data.reports.dto.SalesTrendDTO
import com.vibely.pos.shared.data.sales.dto.CategoryNameDTO
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.data.sales.dto.ProductDTO
import com.vibely.pos.shared.data.sales.dto.SaleDTO
import com.vibely.pos.shared.data.sales.dto.SaleItemDTO
import com.vibely.pos.shared.data.settings.dto.ReceiptSettingsDTO
import com.vibely.pos.shared.data.settings.dto.StoreSettingsDTO
import com.vibely.pos.shared.data.settings.dto.TaxSettingsDTO
import com.vibely.pos.shared.data.settings.dto.UserPreferencesDTO
import com.vibely.pos.shared.data.shift.dto.ShiftDTO
import com.vibely.pos.shared.data.shift.dto.ShiftSummaryDTO
import com.vibely.pos.shared.data.supplier.dto.SupplierDTO
import com.vibely.pos.shared.di.sharedModules
import com.vibely.pos.shared.domain.result.Result
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private const val ERROR_KEY = "error"

/**
 * Configures Koin dependency injection.
 */
fun Application.configureKoin() {
    initializeResultSealedClasses()
    initializeSharedSerializers()


    install(Koin) {
        slf4jLogger()
        modules(sharedModules() + backendModule)
    }
}

private fun Application.initializeResultSealedClasses() {
    try {
        val successInstance: Result<Unit> = Result.Success(Unit)
        val errorInstance: Result<Nothing> = Result.Error("init")
        successInstance.javaClass
        errorInstance.javaClass
    } catch (e: ClassNotFoundException) {
        error("Failed to initialize Result sealed classes. ${e.message}")
    }
}

/**
 * Forces early initialization of Kotlin Serialization descriptors and domain classes.
 * This ensures that serializer classes and sealed classes are loaded before Koin attempts to use them,
 * preventing intermittent ClassNotFoundException errors.
 */
private fun initializeSharedSerializers() {
    try {
        // Auth DTOs
        AuthResponseDTO.serializer()
        LoginRequestDTO.serializer()
        RefreshTokenRequestDTO.serializer()
        UserDTO.serializer()

        // Currency DTOs
        CurrencyDTO.serializer()
        CurrencyExchangeRateDTO.serializer()

        // Customer DTOs
        CustomerDTO.serializer()

        // Dashboard DTOs
        DashboardSummaryDTO.serializer()
        LowStockProductDTO.serializer()
        RecentTransactionDTO.serializer()

        // Inventory DTOs
        CategoryDTO.serializer()
        InventoryTransactionDTO.serializer()

        // Purchase Order DTOs
        PurchaseOrderDTO.serializer()
        PurchaseOrderItemDTO.serializer()
        PurchaseOrderWithItemsDTO.serializer()

        // Reports DTOs
        CategoryBreakdownDTO.serializer()
        CustomerAnalyticsDTO.serializer()
        ProductPerformanceDTO.serializer()
        SalesReportDTO.serializer()
        SalesTrendDTO.serializer()

        // Sales DTOs (including nested CategoryNameDTO)
        CategoryNameDTO.serializer()
        PaymentDTO.serializer()
        ProductDTO.serializer()
        SaleDTO.serializer()
        SaleItemDTO.serializer()

        // Settings DTOs
        ReceiptSettingsDTO.serializer()
        StoreSettingsDTO.serializer()
        TaxSettingsDTO.serializer()
        UserPreferencesDTO.serializer()

        // Shift DTOs
        ActiveShiftInfoDTO.serializer()
        ShiftDTO.serializer()
        ShiftSummaryDTO.serializer()

        // Supplier DTOs
        SupplierDTO.serializer()
    } catch (e: ClassNotFoundException) {
        error("Failed to initialize shared serializers. ${e.message}")
    }
}

/**
 * Configures JSON content negotiation.
 */
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

/**
 * Configures CORS for cross-origin requests.
 */
fun Application.configureCORS() {
    install(CORS) {
        allowCredentials = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader("X-Client-Platform")
        allowHeader("X-CSRF-Token")
        exposeHeader("X-CSRF-Token")
        exposeHeader("X-RateLimit-Limit")
        exposeHeader("X-RateLimit-Remaining")
        exposeHeader("X-RateLimit-Reset")

        anyHost()
    }
}

/**
 * Configures request/response logging.
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

/**
 * Configures global error handling.
 */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception at ${call.request.path()}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(ERROR_KEY to (cause.message ?: "Unknown error"))
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Bad request at ${call.request.path()}: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(ERROR_KEY to (cause.message ?: "Invalid request"))
            )
        }
    }
}

/**
 * Configures JWT authentication.
 */
fun Application.configureAuthentication() {
    val authProvider: Lazy<RouteAuthProvider> = inject()
    val appConfig: Lazy<AppConfig> = inject()

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(appConfig.value.jwtSecret))
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        with(authProvider.value) { configure() }
    }
}

/**
 * Configures security hardening plugins.
 */
fun Application.configureSecurity() {
    val rateLimiter: Lazy<RateLimiter> = inject()
    val csrfTokenManager: Lazy<CsrfTokenManager> = inject()
    val appConfig: Lazy<AppConfig> = inject()

    install(SecurityHeadersPlugin)
    install(HttpsEnforcementPlugin) {
        enabled = appConfig.value.enforceHttps
    }
    install(RateLimitingPlugin) {
        RateLimitingConfig(rateLimiter = rateLimiter.value)
    }
    install(CsrfProtectionPlugin) {
        CsrfProtectionConfig(tokenManager = csrfTokenManager.value)
    }
}
