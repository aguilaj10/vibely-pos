package com.vibely.pos.backend.services

import com.vibely.pos.shared.data.auth.dto.AuthResponseDTO
import com.vibely.pos.shared.data.auth.dto.UserDTO
import com.vibely.pos.shared.util.TimeUtil
import kotlin.time.Instant

private const val ACTIVE = "ACTIVE"

/**
 * Service for handling authentication operations.
 *
 * Responsibilities:
 * - User authentication (login)
 * - Session management (logout)
 * - Token refresh
 * - Current user retrieval
 */
class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
) {
    /**
     * Authenticates a user with email and password.
     *
     * @param email User's email address.
     * @param password User's plain text password.
     * @return [AuthResponseDTO] containing tokens and user info, or null if authentication fails.
     */
    suspend fun login(email: String, password: String): AuthResponseDTO? {
        // Fetch user from database by email
        val user = userRepository.getUserByEmail(email) ?: return null

        // Verify password against stored hash && Check user status
        if (!userRepository.verifyPassword(password, user.passwordHash) || user.status != ACTIVE) {
            return null
        }

        // Update last login timestamp
        userRepository.updateLastLogin(user.id)

        // Generate tokens
        val accessToken = tokenService.generateAccessToken(user.id, user.email, user.role)
        val refreshToken = tokenService.generateRefreshToken(user.id)

        // Store refresh token in database
        tokenService.storeRefreshToken(user.id, refreshToken)

        // Convert to UserDTO
        val userDTO = user.toUserDTO()

        return AuthResponseDTO(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = tokenService.accessTokenExpirationSeconds,
            user = userDTO
        )
    }

    /**
     * Logs out a user by blacklisting their access token.
     *
     * @param accessToken The JWT access token to invalidate.
     * @param userId The ID of the user logging out.
     */
    suspend fun logout(accessToken: String, userId: String) {
        // Add token to blacklist
        tokenService.blacklistToken(accessToken, userId)

        // Delete refresh tokens for this user
        tokenService.deleteAllUserRefreshTokens(userId)
    }

    /**
     * Gets the current authenticated user by user ID.
     *
     * @param userId The user's ID from JWT token.
     * @return [UserDTO] if user exists, null otherwise.
     */
    suspend fun getCurrentUser(userId: String): UserDTO? {
        val user = userRepository.getUserById(userId) ?: return null
        return user.toUserDTO()
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token.
     * @return [AuthResponseDTO] with new tokens, or null if refresh fails.
     */
    suspend fun refreshAccessToken(refreshToken: String): AuthResponseDTO? {
        // Verify and decode refresh token
        val userId = tokenService.verifyRefreshToken(refreshToken)
        // Check if refresh token exists in database
        val storedToken = tokenService.getRefreshToken(refreshToken)
        // Get user details
        val user = userRepository.getUserById(userId ?: "")

        // Check user status
        val isUserInactive = user == null || storedToken == null
        val isStatusInactive = user?.status != ACTIVE
        if (isUserInactive || isStatusInactive) {
            return null
        }

        // Check if token is expired
        val expiresAt = Instant.parse(storedToken.expiresAt)
        if (TimeUtil.now() >= expiresAt) {
            // Delete expired token
            tokenService.deleteRefreshToken(refreshToken)
            return null
        }

        // Generate new tokens
        val newAccessToken = tokenService.generateAccessToken(user.id, user.email, user.role)
        val newRefreshToken = tokenService.generateRefreshToken(user.id)

        // Delete old refresh token and store new one
        tokenService.deleteRefreshToken(refreshToken)
        tokenService.storeRefreshToken(user.id, newRefreshToken)

        // Convert to UserDTO
        val userDTO = user.toUserDTO()

        return AuthResponseDTO(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = tokenService.accessTokenExpirationSeconds,
            user = userDTO
        )
    }

    /**
     * Checks if an access token is blacklisted.
     *
     * @param token The JWT access token.
     * @return True if blacklisted, false otherwise.
     */
    suspend fun isTokenBlacklisted(token: String): Boolean = tokenService.isTokenBlacklisted(token)

    /**
     * Hashes a password using BCrypt.
     */
    fun hashPassword(password: String): String = userRepository.hashPassword(password)

    /**
     * Converts UserEntity to UserDTO.
     */
    private fun UserRepository.UserEntity.toUserDTO(): UserDTO {
        return UserDTO(
            id = id,
            email = email,
            fullName = fullName,
            role = role,
            status = status,
            createdAt = createdAt,
            lastLoginAt = lastLoginAt
        )
    }
}
