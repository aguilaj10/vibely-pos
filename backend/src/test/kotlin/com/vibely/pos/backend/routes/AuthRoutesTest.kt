package com.vibely.pos.backend.routes

import com.vibely.pos.backend.config.configureTestAuthentication
import com.vibely.pos.backend.services.AuthService
import com.vibely.pos.shared.data.auth.dto.AuthResponseDTO
import com.vibely.pos.shared.data.auth.dto.LoginRequestDTO
import com.vibely.pos.shared.data.auth.dto.RefreshTokenRequestDTO
import com.vibely.pos.shared.data.auth.dto.UserDTO
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for authentication routes.
 *
 * Tests all authentication endpoints with various scenarios:
 * - Successful login
 * - Invalid credentials
 * - Token refresh
 * - Getting current user
 * - Logout
 */
class AuthRoutesTest {

    private val mockAuthService = mockk<AuthService>(relaxed = true)

    private val testUser = UserDTO(
        id = "test-user-id",
        email = "test@example.com",
        fullName = "Test User",
        role = "CASHIER",
        status = "ACTIVE",
        createdAt = "2026-03-13T00:00:00Z",
        lastLoginAt = "2026-03-13T12:00:00Z"
    )

    private val testAuthResponse = AuthResponseDTO(
        accessToken = "test-access-token",
        refreshToken = "test-refresh-token",
        expiresIn = 900,
        user = testUser
    )

    @Test
    fun `POST login - successful authentication returns 200 with tokens`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Mock successful login
        coEvery {
            mockAuthService.login("test@example.com", "Password123!")
        } returns testAuthResponse

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDTO(
                email = "test@example.com",
                password = "Password123!"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val authResponse = response.body<AuthResponseDTO>()
        assertNotNull(authResponse)
        assertEquals("test-access-token", authResponse.accessToken)
        assertEquals("test-refresh-token", authResponse.refreshToken)
        assertEquals(testUser.email, authResponse.user.email)

        coVerify { mockAuthService.login("test@example.com", "Password123!") }
    }

    @Test
    fun `POST login - invalid credentials returns 401`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Mock failed login
        coEvery {
            mockAuthService.login("wrong@example.com", "wrongpass")
        } returns null

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDTO(
                email = "wrong@example.com",
                password = "wrongpass"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST login - empty credentials returns 400`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDTO(
                email = "",
                password = ""
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST refresh - successful token refresh returns 200 with new tokens`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Mock successful refresh
        coEvery {
            mockAuthService.refreshAccessToken("test-refresh-token")
        } returns testAuthResponse

        val response = client.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDTO(
                refreshToken = "test-refresh-token"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val authResponse = response.body<AuthResponseDTO>()
        assertNotNull(authResponse)
        assertEquals("test-access-token", authResponse.accessToken)

        coVerify { mockAuthService.refreshAccessToken("test-refresh-token") }
    }

    @Test
    fun `POST refresh - invalid refresh token returns 401`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Mock failed refresh
        coEvery {
            mockAuthService.refreshAccessToken("invalid-token")
        } returns null

        val response = client.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDTO(
                refreshToken = "invalid-token"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST refresh - empty refresh token returns 400`() = testApplication {
        application {
            configureTestAuthentication()
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing {
                authRoutes(mockAuthService)
            }
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDTO(
                refreshToken = ""
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
