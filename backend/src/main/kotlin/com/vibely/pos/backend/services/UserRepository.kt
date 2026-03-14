package com.vibely.pos.backend.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.vibely.pos.shared.util.TimeUtil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val USERS = "users"
private const val BCRYPT_COST = 12

/**
 * Repository for user database operations.
 *
 * Responsibilities:
 * - User queries (by email, by ID)
 * - Password hashing and verification
 * - User status updates
 */
class UserRepository(
    private val supabaseClient: SupabaseClient,
) {
    /**
     * Fetches a user from the database by email.
     */
    internal suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            val result = supabaseClient.from(USERS)
                .select {
                    filter {
                        eq("email", email)
                    }
                    limit(1)
                }
                .decodeSingle<UserEntity>()
            result
        } catch (e: RestException) {
            null
        }
    }

    /**
     * Fetches a user from the database by ID.
     */
    internal suspend fun getUserById(userId: String): UserEntity? {
        return try {
            val result = supabaseClient.from(USERS)
                .select {
                    filter {
                        eq("id", userId)
                    }
                    limit(1)
                }
                .decodeSingle<UserEntity>()
            result
        } catch (e: RestException) {
            null
        }
    }

    /**
     * Updates the last login timestamp for a user.
     */
    suspend fun updateLastLogin(userId: String) {
        try {
            supabaseClient.from(USERS).update(
                mapOf("last_login_at" to TimeUtil.now().toString())
            ) {
                filter {
                    eq("id", userId)
                }
            }
        } catch (e: RestException) {
            // Log error but don't fail authentication
        }
    }

    /**
     * Hashes a password using BCrypt.
     */
    fun hashPassword(password: String): String {
        print("Hashing password")
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }

    /**
     * Verifies a password against a BCrypt hash.
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
        return result.verified
    }

    /**
     * Database entity for users table.
     */
    @Serializable
    internal data class UserEntity(
        @SerialName("id") val id: String,
        @SerialName("email") val email: String,
        @SerialName("full_name") val fullName: String,
        @SerialName("role") val role: String,
        @SerialName("status") val status: String,
        @SerialName("password_hash") val passwordHash: String,
        @SerialName("created_at") val createdAt: String,
        @SerialName("last_login_at") val lastLoginAt: String? = null,
    )
}
