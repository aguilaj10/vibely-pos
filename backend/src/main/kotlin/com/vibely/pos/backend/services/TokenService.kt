package com.vibely.pos.backend.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.vibely.pos.shared.util.TimeUtil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val REFRESH_TOKENS = "refresh_tokens"
private const val TOKEN_BLACKLIST = "token_blacklist"
private const val COLUMN_TOKEN = "token"
private const val COLUMN_USER_ID = "user_id"
private const val COLUMN_EXPIRES_AT = "expires_at"
private const val COLUMN_CREATED_AT = "created_at"
private const val COLUMN_BLACKLISTED_AT = "blacklisted_at"
private const val CLAIM_USER_ID = "userId"
private const val CLAIM_TYPE = "type"
private const val SECONDS_PER_MILLISECOND = 1000

/**
 * Service for handling JWT token operations.
 *
 * Responsibilities:
 * - JWT token generation (access + refresh)
 * - Token validation and verification
 * - Token blacklisting
 * - Refresh token storage and management
 */
class TokenService(
    private val supabaseClient: SupabaseClient,
) {
    private val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"
    private val algorithm = Algorithm.HMAC256(jwtSecret)

    // Token expiration durations
    private val accessTokenExpirationMs = 15.minutes.inWholeMilliseconds
    private val refreshTokenExpirationMs = 7.days.inWholeMilliseconds

    /**
     * Gets the access token expiration time in seconds.
     */
    val accessTokenExpirationSeconds: Long
        get() = accessTokenExpirationMs / SECONDS_PER_MILLISECOND

    /**
     * Generates a JWT access token.
     */
    fun generateAccessToken(userId: String, email: String, role: String): String {
        val now = System.currentTimeMillis()
        val expiresAt = now + accessTokenExpirationMs

        return JWT.create()
            .withClaim(CLAIM_USER_ID, userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withClaim(CLAIM_TYPE, "access")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    /**
     * Generates a JWT refresh token.
     */
    fun generateRefreshToken(userId: String): String {
        val now = System.currentTimeMillis()
        val expiresAt = now + refreshTokenExpirationMs

        return JWT.create()
            .withClaim(CLAIM_USER_ID, userId)
            .withClaim(CLAIM_TYPE, "refresh")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    /**
     * Verifies a refresh token and returns the user ID.
     */
    fun verifyRefreshToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm).build()
            val jwt = verifier.verify(token)
            val tokenType = jwt.getClaim(CLAIM_TYPE).asString()

            if (tokenType != "refresh") {
                return null
            }

            jwt.getClaim(CLAIM_USER_ID).asString()
        } catch (e: JWTVerificationException) {
            null
        }
    }

    /**
     * Checks if an access token is blacklisted.
     */
    suspend fun isTokenBlacklisted(token: String): Boolean {
        val result = supabaseClient.from(TOKEN_BLACKLIST)
            .select(columns = Columns.list(COLUMN_TOKEN)) {
                filter {
                    eq(COLUMN_TOKEN, token)
                }
            }

        return result.data.isNotEmpty()
    }

    /**
     * Blacklists an access token.
     */
    suspend fun blacklistToken(accessToken: String, userId: String) {
        val expiresAt = TimeUtil.now() + 15.minutes

        supabaseClient.from(TOKEN_BLACKLIST).insert(
            mapOf(
                COLUMN_TOKEN to accessToken,
                COLUMN_USER_ID to userId,
                COLUMN_EXPIRES_AT to expiresAt.toString(),
                COLUMN_BLACKLISTED_AT to TimeUtil.now().toString()
            )
        )
    }

    /**
     * Stores a refresh token in the database.
     */
    suspend fun storeRefreshToken(userId: String, refreshToken: String) {
        val expiresAt = TimeUtil.now() + 7.days

        supabaseClient.from(REFRESH_TOKENS).insert(
            mapOf(
                COLUMN_USER_ID to userId,
                COLUMN_TOKEN to refreshToken,
                COLUMN_EXPIRES_AT to expiresAt.toString(),
                COLUMN_CREATED_AT to TimeUtil.now().toString()
            )
        )
    }

    /**
     * Retrieves a refresh token from the database.
     */
    internal suspend fun getRefreshToken(token: String): RefreshTokenEntity? {
        return try {
            val result = supabaseClient.from(REFRESH_TOKENS)
                .select {
                    filter {
                        eq(COLUMN_TOKEN, token)
                    }
                    limit(1)
                }
                .decodeSingle<RefreshTokenEntity>()
            result
        } catch (e: RestException) {
            null
        }
    }

    /**
     * Deletes a refresh token from the database.
     */
    suspend fun deleteRefreshToken(token: String) {
        try {
            supabaseClient.from(REFRESH_TOKENS)
                .delete {
                    filter {
                        eq(COLUMN_TOKEN, token)
                    }
                }
        } catch (e: RestException) {
            // Log error but don't fail
        }
    }

    /**
     * Deletes all refresh tokens for a user.
     */
    suspend fun deleteAllUserRefreshTokens(userId: String) {
        supabaseClient.from(REFRESH_TOKENS)
            .delete {
                filter {
                    eq(COLUMN_USER_ID, userId)
                }
            }
    }

    /**
     * Database entity for refresh_tokens table.
     */
    @Serializable
    internal data class RefreshTokenEntity(
        @SerialName("id") val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("token") val token: String,
        @SerialName("expires_at") val expiresAt: String,
        @SerialName("created_at") val createdAt: String,
    )
}
