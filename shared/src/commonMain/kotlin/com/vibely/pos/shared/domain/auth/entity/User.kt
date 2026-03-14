package com.vibely.pos.shared.domain.auth.entity

import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.exception.BusinessRuleException
import com.vibely.pos.shared.domain.valueobject.Email
import kotlin.time.Instant

/**
 * Domain entity representing a user in the system.
 *
 * Encapsulates user identity, credentials, and authorization information.
 * Enforces business rules around user status and permissions.
 *
 * @param id Unique identifier (UUID from database).
 * @param email User's email address (used for login).
 * @param fullName User's full name.
 * @param role User's role determining permissions.
 * @param status User's account status.
 * @param createdAt When the user account was created.
 * @param lastLoginAt When the user last logged in (null if never logged in).
 */
data class User(
    val id: String,
    val email: Email,
    val fullName: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant,
    val lastLoginAt: Instant? = null,
) {
    /**
     * Returns true if the user can perform authentication operations.
     *
     * Business Rule: Only users with ACTIVE status can log in.
     */
    fun canAuthenticate(): Boolean = status.canLogin()

    /**
     * Returns true if the user has admin privileges.
     */
    fun isAdmin(): Boolean = role == UserRole.ADMIN

    /**
     * Returns true if the user has manager or higher privileges.
     */
    fun isManagerOrHigher(): Boolean = role in listOf(UserRole.ADMIN, UserRole.MANAGER)

    /**
     * Validates that the user can perform authentication operations.
     *
     * @throws BusinessRuleException if the user cannot authenticate.
     */
    fun requireCanAuthenticate() {
        if (!canAuthenticate()) {
            throw BusinessRuleException(
                rule = "User with status ${status.name} cannot authenticate",
                code = "USER_CANNOT_AUTHENTICATE",
            )
        }
    }

    /**
     * Returns a copy of this user with updated last login timestamp.
     */
    fun withLastLogin(timestamp: Instant): User = copy(lastLoginAt = timestamp)

    /**
     * Returns a copy of this user with updated status.
     *
     * Business Rule: Status transitions must be valid.
     */
    fun withStatus(newStatus: UserStatus): User {
        // Add validation for status transitions if needed
        return copy(status = newStatus)
    }

    companion object {
        /**
         * Creates a new User instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            email: Email,
            fullName: String,
            role: UserRole,
            status: UserStatus,
            createdAt: Instant,
            lastLoginAt: Instant? = null,
        ): User {
            require(id.isNotBlank()) { "User ID cannot be blank" }
            require(fullName.isNotBlank()) { "Full name cannot be blank" }

            return User(
                id = id,
                email = email,
                fullName = fullName,
                role = role,
                status = status,
                createdAt = createdAt,
                lastLoginAt = lastLoginAt,
            )
        }
    }
}
