package com.vibely.pos.shared.data.auth.datasource

import com.vibely.pos.shared.data.auth.dto.AuthResponseDTO
import com.vibely.pos.shared.data.auth.dto.LoginRequestDTO
import com.vibely.pos.shared.data.auth.dto.RefreshTokenRequestDTO
import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Remote data source for authentication API calls.
 *
 * Handles HTTP requests to the backend authentication endpoints.
 *
 * @param httpClient The HTTP client for making requests.
 * @param baseUrl The base URL of the backend API.
 */
class RemoteAuthDataSource(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Performs login request to the backend.
     *
     * POST /auth/login
     *
     * @param request The login request with email and password.
     * @return [Result.Success] with [AuthResponseDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun login(request: LoginRequestDTO): Result<AuthResponseDTO> = Result.runCatching {
        httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponseDTO>()
    }

    /**
     * Fetches current user details from the backend.
     *
     * GET /auth/me
     *
     * @param accessToken The JWT access token for authentication.
     * @return [Result.Success] with [UserDTO] if successful,
     *         [Result.Error] if request fails.
     */
    suspend fun getCurrentUser(accessToken: String): Result<UserDTO> = Result.runCatching {
        httpClient.get("$baseUrl/auth/me") {
            header("Authorization", "Bearer $accessToken")
        }.body<UserDTO>()
    }

    /**
     * Refreshes the access token using a refresh token.
     *
     * POST /auth/refresh
     *
     * @param request The refresh token request.
     * @return [Result.Success] with [AuthResponseDTO] containing new tokens,
     *         [Result.Error] if request fails.
     */
    suspend fun refreshToken(request: RefreshTokenRequestDTO): Result<AuthResponseDTO> = Result.runCatching {
        httpClient.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponseDTO>()
    }

    /**
     * Logs out the user on the backend.
     *
     * POST /auth/logout
     *
     * @param accessToken The JWT access token for authentication.
     * @return [Result.Success] if successful, [Result.Error] if request fails.
     */
    suspend fun logout(accessToken: String): Result<Unit> = Result.runCatching {
        httpClient.post("$baseUrl/auth/logout") {
            header("Authorization", "Bearer $accessToken")
        }
        Unit
    }
}
