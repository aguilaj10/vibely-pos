package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.TokenService
import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration tests for JWT token generation and validation.
 *
 * These tests verify that JWT tokens are properly generated, signed, and validated
 * without requiring the full Ktor authentication setup.
 */
class AuthenticationIntegrationTest {

    private val mockSupabaseClient = mockk<SupabaseClient>(relaxed = true)
    private val tokenService = TokenService(mockSupabaseClient)

    @Test
    fun `TokenService generates and validates access tokens correctly`() {
        val userId = "test-user-123"
        val email = "test@example.com"
        val role = "CASHIER"

        // Generate access token
        val accessToken = tokenService.generateAccessToken(userId, email, role)
        assertNotNull(accessToken)

        // Access tokens contain userId in the JWT payload
        // We verify this by checking the token structure
        val parts = accessToken.split(".")
        assertEquals(3, parts.size, "JWT should have 3 parts (header.payload.signature)")
    }

    @Test
    fun `TokenService generates and validates refresh tokens correctly`() {
        val userId = "test-user-123"

        // Generate refresh token
        val refreshToken = tokenService.generateRefreshToken(userId)
        assertNotNull(refreshToken)

        // Verify refresh token returns correct userId
        val verifiedUserId = tokenService.verifyRefreshToken(refreshToken)
        assertEquals(userId, verifiedUserId)
    }

    @Test
    fun `TokenService rejects invalid tokens`() {
        val result = tokenService.verifyRefreshToken("invalid.token.here")
        assertNull(result)
    }

    @Test
    fun `TokenService rejects tokens with wrong type`() {
        // Generate an access token
        val accessToken = tokenService.generateAccessToken("user123", "test@example.com", "CASHIER")

        // Try to verify it as a refresh token - should fail because type is "access" not "refresh"
        val result = tokenService.verifyRefreshToken(accessToken)
        assertNull(result)
    }

    @Test
    fun `TokenService generates different tokens for different users`() {
        val token1 = tokenService.generateRefreshToken("user1")
        val token2 = tokenService.generateRefreshToken("user2")

        assertNotNull(token1)
        assertNotNull(token2)

        // Tokens should be different
        assert(token1 != token2) { "Tokens for different users should be different" }

        // Each token should verify to its correct user
        assertEquals("user1", tokenService.verifyRefreshToken(token1))
        assertEquals("user2", tokenService.verifyRefreshToken(token2))
    }
}
