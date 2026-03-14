package com.vibely.pos.shared.data.auth.repository

import com.vibely.pos.shared.data.auth.datasource.LocalAuthDataSource
import com.vibely.pos.shared.data.auth.datasource.RemoteAuthDataSource
import com.vibely.pos.shared.data.auth.dto.LoginRequestDTO
import com.vibely.pos.shared.data.auth.dto.RefreshTokenRequestDTO
import com.vibely.pos.shared.data.auth.mapper.AuthTokenMapper
import com.vibely.pos.shared.data.auth.mapper.UserMapper
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.repository.AuthRepository
import com.vibely.pos.shared.domain.auth.valueobject.AuthToken
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.flatMap
import com.vibely.pos.shared.domain.result.map

/**
 * Implementation of [AuthRepository] using remote and local data sources.
 *
 * Coordinates authentication operations between the backend API and local storage.
 *
 * @param remoteDataSource Remote data source for backend API calls.
 * @param localDataSource Local data source for token storage.
 */
class AuthRepositoryImpl(private val remoteDataSource: RemoteAuthDataSource, private val localDataSource: LocalAuthDataSource) : AuthRepository {

    override suspend fun login(credentials: Credentials): Result<AuthToken> {
        val request = LoginRequestDTO(
            email = credentials.email.value,
            password = credentials.password,
        )

        return remoteDataSource.login(request)
            .map { response -> AuthTokenMapper.toDomain(response) }
    }

    override suspend fun logout(): Result<Unit> {
        // First, try to get the stored token to notify backend
        return localDataSource.getToken()
            .flatMap { token ->
                // If we have a token, try to notify backend
                if (token != null) {
                    remoteDataSource.logout(token.accessToken)
                } else {
                    Result.Success(Unit)
                }
            }
            .flatMap {
                // Always clear local token, even if backend notification fails
                localDataSource.clearToken()
            }
    }

    override suspend fun getCurrentUser(): Result<User?> = localDataSource.getToken()
        .flatMap { token ->
            if (token == null) {
                // No token stored, user is not authenticated
                Result.Success(null)
            } else if (token.isExpired()) {
                // Token is expired, return null (caller should refresh or re-login)
                Result.Success(null)
            } else {
                // Token is valid, fetch user from backend
                remoteDataSource.getCurrentUser(token.accessToken)
                    .map { userDTO -> UserMapper.toDomain(userDTO) }
            }
        }

    override suspend fun refreshToken(): Result<AuthToken> = localDataSource.getToken()
        .flatMap { token ->
            if (token == null) {
                Result.Error(
                    message = "No refresh token available",
                    code = "NO_REFRESH_TOKEN",
                )
            } else {
                val request = RefreshTokenRequestDTO(refreshToken = token.refreshToken)
                remoteDataSource.refreshToken(request)
                    .map { response -> AuthTokenMapper.toDomain(response) }
            }
        }

    override suspend fun getStoredToken(): Result<AuthToken?> = localDataSource.getToken()

    override suspend fun storeToken(token: AuthToken): Result<Unit> = localDataSource.storeToken(token)

    override suspend fun clearStoredToken(): Result<Unit> = localDataSource.clearToken()
}
