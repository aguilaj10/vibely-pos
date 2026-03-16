package com.vibely.pos.backend.routes

import com.vibely.pos.backend.config.configureTestAuthentication
import com.vibely.pos.backend.dto.request.UpdateReceiptSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateStoreInfoRequest
import com.vibely.pos.backend.dto.request.UpdateTaxSettingsRequest
import com.vibely.pos.backend.dto.request.UpdateUserPreferencesRequest
import com.vibely.pos.backend.services.SettingsService
import com.vibely.pos.shared.data.settings.dto.ReceiptSettingsDTO
import com.vibely.pos.shared.data.settings.dto.StoreSettingsDTO
import com.vibely.pos.shared.data.settings.dto.TaxSettingsDTO
import com.vibely.pos.shared.data.settings.dto.UserPreferencesDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SettingsRoutesTest {

    private val mockSettingsService = mockk<SettingsService>(relaxed = true)

    private val testStoreSettings = StoreSettingsDTO(
        id = "settings-store-1",
        storeName = "Test Store",
        address = "123 Test Street",
        phone = "+1234567890",
        email = "test@store.com",
        createdAt = 1700000000000,
        updatedAt = 1700000000000
    )

    private val testReceiptSettings = ReceiptSettingsDTO(
        id = "settings-receipt-1",
        header = "Welcome to Test Store",
        footer = "Thank you for shopping with us",
        logoUrl = "https://example.com/logo.png",
        showTax = true,
        createdAt = 1700000000000,
        updatedAt = 1700000000000
    )

    private val testTaxSettings = TaxSettingsDTO(
        id = "settings-tax-1",
        taxRate = 16.0,
        currency = "USD",
        createdAt = 1700000000000,
        updatedAt = 1700000000000
    )

    private val testUserPreferences = UserPreferencesDTO(
        id = "user-123",
        language = "en",
        theme = "dark",
        enableNotifications = true,
        autoLogoutTimeoutMinutes = 30,
        createdAt = 1700000000000,
        updatedAt = 1700000000000
    )

    private fun Application.configureTestSettingsRoutes(settingsService: SettingsService) {
        configureTestAuthentication()
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        routing {
            settingsRoutes(settingsService)
        }
    }

    private fun ApplicationTestBuilder.createTestClientWithJson(): HttpClient {
        return createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    @Test
    fun `GET store settings - successful request returns 200 with settings`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.getStoreSettings() } returns Result.Success(testStoreSettings)

        val response = client.get("/api/settings/store") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val settings = response.body<StoreSettingsDTO>()
        assertNotNull(settings)
        assertEquals("Test Store", settings.storeName)
        assertEquals("123 Test Street", settings.address)

        coVerify { mockSettingsService.getStoreSettings() }
    }

    @Test
    fun `GET store settings - unauthorized request returns 401`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val response = client.get("/api/settings/store")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT store settings - successful request returns 200`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.updateStoreSettings(any()) } returns Result.Success(Unit)

        val request = UpdateStoreInfoRequest(
            storeName = "Updated Store",
            address = "456 Updated Street",
            phone = "+0987654321",
            email = "updated@store.com"
        )

        val response = client.put("/api/settings/store") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockSettingsService.updateStoreSettings(any()) }
    }

    @Test
    fun `PUT store settings - missing storeName returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateStoreInfoRequest(
            storeName = "",
            address = "456 Updated Street",
            phone = "+0987654321",
            email = "updated@store.com"
        )

        val response = client.put("/api/settings/store") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT store settings - missing address returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateStoreInfoRequest(
            storeName = "Test Store",
            address = "",
            phone = "+0987654321",
            email = "updated@store.com"
        )

        val response = client.put("/api/settings/store") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET receipt settings - successful request returns 200 with settings`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.getReceiptSettings() } returns Result.Success(testReceiptSettings)

        val response = client.get("/api/settings/receipt") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val settings = response.body<ReceiptSettingsDTO>()
        assertNotNull(settings)
        assertEquals("Welcome to Test Store", settings.header)
        assertEquals(true, settings.showTax)

        coVerify { mockSettingsService.getReceiptSettings() }
    }

    @Test
    fun `GET receipt settings - unauthorized request returns 401`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val response = client.get("/api/settings/receipt")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT receipt settings - successful request returns 200`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.updateReceiptSettings(any()) } returns Result.Success(Unit)

        val request = UpdateReceiptSettingsRequest(
            header = "New Header",
            footer = "New Footer",
            logoUrl = "https://example.com/new-logo.png",
            showTax = false
        )

        val response = client.put("/api/settings/receipt") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockSettingsService.updateReceiptSettings(any()) }
    }

    @Test
    fun `PUT receipt settings - missing header returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateReceiptSettingsRequest(
            header = "",
            footer = "New Footer",
            logoUrl = null,
            showTax = true
        )

        val response = client.put("/api/settings/receipt") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT receipt settings - missing footer returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateReceiptSettingsRequest(
            header = "New Header",
            footer = "",
            logoUrl = null,
            showTax = true
        )

        val response = client.put("/api/settings/receipt") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET tax settings - successful request returns 200 with settings`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.getTaxSettings() } returns Result.Success(testTaxSettings)

        val response = client.get("/api/settings/tax") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val settings = response.body<TaxSettingsDTO>()
        assertNotNull(settings)
        assertEquals(16.0, settings.taxRate)
        assertEquals("USD", settings.currency)

        coVerify { mockSettingsService.getTaxSettings() }
    }

    @Test
    fun `GET tax settings - unauthorized request returns 401`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val response = client.get("/api/settings/tax")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT tax settings - successful request returns 200`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.updateTaxSettings(any()) } returns Result.Success(Unit)

        val request = UpdateTaxSettingsRequest(
            taxRate = 20.0,
            currency = "EUR"
        )

        val response = client.put("/api/settings/tax") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockSettingsService.updateTaxSettings(any()) }
    }

    @Test
    fun `PUT tax settings - negative taxRate returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateTaxSettingsRequest(
            taxRate = -5.0,
            currency = "USD"
        )

        val response = client.put("/api/settings/tax") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT tax settings - missing currency returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateTaxSettingsRequest(
            taxRate = 10.0,
            currency = ""
        )

        val response = client.put("/api/settings/tax") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET user preferences - successful request returns 200 with preferences`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.getUserPreferences("123") } returns Result.Success(testUserPreferences)

        val response = client.get("/api/settings/preferences") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val preferences = response.body<UserPreferencesDTO>()
        assertNotNull(preferences)
        assertEquals("en", preferences.language)
        assertEquals("dark", preferences.theme)

        coVerify { mockSettingsService.getUserPreferences("123") }
    }

    @Test
    fun `GET user preferences - unauthorized request returns 401`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val response = client.get("/api/settings/preferences")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT user preferences - successful request returns 200`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.updateUserPreferences(any(), any()) } returns Result.Success(Unit)

        val request = UpdateUserPreferencesRequest(
            language = "es",
            theme = "light",
            enableNotifications = false,
            autoLogoutTimeoutMinutes = 60
        )

        val response = client.put("/api/settings/preferences") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockSettingsService.updateUserPreferences(any(), any()) }
    }

    @Test
    fun `PUT user preferences - missing language returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateUserPreferencesRequest(
            language = "",
            theme = "light",
            enableNotifications = true,
            autoLogoutTimeoutMinutes = 30
        )

        val response = client.put("/api/settings/preferences") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT user preferences - missing theme returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        val request = UpdateUserPreferencesRequest(
            language = "en",
            theme = "",
            enableNotifications = true,
            autoLogoutTimeoutMinutes = 30
        )

        val response = client.put("/api/settings/preferences") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET store settings - service error returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.getStoreSettings() } returns Result.Error("Database error")

        val response = client.get("/api/settings/store") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT store settings - service error returns 400`() = testApplication {
        application { configureTestSettingsRoutes(mockSettingsService) }
        val client = createTestClientWithJson()

        coEvery { mockSettingsService.updateStoreSettings(any()) } returns Result.Error("Update failed")

        val request = UpdateStoreInfoRequest(
            storeName = "Test Store",
            address = "123 Test Street",
            phone = "+1234567890",
            email = "test@store.com"
        )

        val response = client.put("/api/settings/store") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
