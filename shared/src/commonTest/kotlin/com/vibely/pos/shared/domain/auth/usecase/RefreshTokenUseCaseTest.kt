package com.vibely.pos.shared.domain.auth.usecase

import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RefreshTokenUseCaseTest {

    // Mock repository for testing
    private class MockAuthRepository : AuthRepository {
        var refreshTokenResult: Result<AuthToken>? = null
        var storeTokenResult: Result<Unit> = Result.Success(Unit)
        var lastStoredToken: AuthToken? = null

        override suspend fun login(credentials: Credentials): Result<AuthToken> = Result.Error("Not implemented")

        override suspend fun logout(): Result<Unit> = Result.Success(Unit)

        override suspend fun getCurrentUser(): Result<User?> = Result.Success(null)

        override suspend fun refreshToken(): Result<AuthToken> = refreshTokenResult ?: Result.Error("Not configured")

        override suspend fun getStoredToken(): Result<AuthToken?> = Result.Success(null)

        override suspend fun storeToken(token: AuthToken): Result<Unit> {
            lastStoredToken = token
            return storeTokenResult
        }

        override suspend fun clearStoredToken(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return new token when refresh succeeds`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val newToken = AuthToken.create(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            expiresInSeconds = 3600,
        )
        mockRepo.refreshTokenResult = Result.Success(newToken)

        val useCase = RefreshTokenUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals(newToken.accessToken, result.data.accessToken)
        assertEquals(newToken.refreshToken, result.data.refreshToken)
    }

    @Test
    fun `invoke should store new token after successful refresh`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val newToken = AuthToken.create(
            accessToken = "new-access",
            refreshToken = "new-refresh",
            expiresInSeconds = 3600,
        )
        mockRepo.refreshTokenResult = Result.Success(newToken)

        val useCase = RefreshTokenUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<AuthToken>>(result)
        assertEquals(newToken, mockRepo.lastStoredToken)
    }

    @Test
    fun `invoke should return error when refresh fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.refreshTokenResult = Result.Error(
            message = "Invalid refresh token",
            code = "INVALID_REFRESH_TOKEN",
        )

        val useCase = RefreshTokenUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Invalid refresh token", result.message)
        assertEquals("INVALID_REFRESH_TOKEN", result.code)
    }

    @Test
    fun `invoke should return error when no refresh token available`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        mockRepo.refreshTokenResult = Result.Error(
            message = "No refresh token available",
            code = "NO_REFRESH_TOKEN",
        )

        val useCase = RefreshTokenUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("No refresh token available", result.message)
        assertEquals("NO_REFRESH_TOKEN", result.code)
    }

    @Test
    fun `invoke should return error if token storage fails`() = runTest {
        // Given
        val mockRepo = MockAuthRepository()
        val newToken = AuthToken.create(
            accessToken = "access",
            refreshToken = "refresh",
            expiresInSeconds = 3600,
        )
        mockRepo.refreshTokenResult = Result.Success(newToken)
        mockRepo.storeTokenResult = Result.Error("Storage failed")

        val useCase = RefreshTokenUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
