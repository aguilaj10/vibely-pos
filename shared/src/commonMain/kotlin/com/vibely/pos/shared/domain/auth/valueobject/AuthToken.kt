package com.vibely.pos.shared.domain.auth.valueobject

import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.util.TimeUtil
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Value object representing authentication tokens (access + refresh).
 *
 * JWT tokens issued by the backend after successful authentication.
 * - Access token: Short-lived, used for API requests
 * - Refresh token: Long-lived, used to obtain new access tokens
 *
 * @param accessToken The JWT access token.
 * @param refreshToken The JWT refresh token.
 * @param expiresAt When the access token expires.
 */
@ConsistentCopyVisibility
data class AuthToken private constructor(val accessToken: String, val refreshToken: String, val expiresAt: Instant) {
    /**
     * Returns true if the access token has expired.
     */
    fun isExpired(): Boolean {
        val now = TimeUtil.now()
        return now >= expiresAt
    }

    /**
     * Returns true if the token will expire within the given duration.
     */
    fun willExpireSoon(thresholdSeconds: Long = 300): Boolean {
        val now = TimeUtil.now()
        val threshold = now.plus(thresholdSeconds.seconds)
        return threshold >= expiresAt
    }

    companion object {
        /**
         * Creates an [AuthToken] from token strings and expiration timestamp.
         *
         * @param accessToken The JWT access token string.
         * @param refreshToken The JWT refresh token string.
         * @param expiresAt The expiration timestamp.
         * @throws ValidationException if tokens are invalid.
         */
        fun create(accessToken: String, refreshToken: String, expiresAt: Instant): AuthToken {
            if (accessToken.isBlank()) {
                throw ValidationException(
                    field = "accessToken",
                    message = "Access token cannot be blank",
                )
            }

            if (refreshToken.isBlank()) {
                throw ValidationException(
                    field = "refreshToken",
                    message = "Refresh token cannot be blank",
                )
            }

            return AuthToken(accessToken, refreshToken, expiresAt)
        }

        /**
         * Creates an [AuthToken] from token strings and expiration duration in seconds.
         *
         * @param accessToken The JWT access token string.
         * @param refreshToken The JWT refresh token string.
         * @param expiresInSeconds How many seconds until the token expires.
         * @throws ValidationException if tokens are invalid.
         */
        fun create(accessToken: String, refreshToken: String, expiresInSeconds: Long): AuthToken {
            val now = TimeUtil.now()
            val expiresAt = now.plus(expiresInSeconds.seconds)
            return create(accessToken, refreshToken, expiresAt)
        }
    }
}
