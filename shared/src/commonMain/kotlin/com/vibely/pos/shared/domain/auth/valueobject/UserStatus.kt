package com.vibely.pos.shared.domain.auth.valueobject

/**
 * Enum representing the status of a user account.
 *
 * - [Active]: User can log in and perform operations
 * - [Inactive]: User account is deactivated (soft delete), cannot log in
 * - [Suspended]: User is temporarily blocked, cannot log in
 */
enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    ;

    /**
     * Returns true if the user can log in (only ACTIVE status).
     */
    fun canLogin(): Boolean = this == ACTIVE

    companion object {
        /**
         * Parses a status from a string value, case-insensitive.
         *
         * @param value The string representation of the status.
         * @return The corresponding [UserStatus], or null if not found.
         */
        fun fromString(value: String?): UserStatus? = when (value?.uppercase()) {
            "ACTIVE" -> ACTIVE
            "INACTIVE" -> INACTIVE
            "SUSPENDED" -> SUSPENDED
            else -> null
        }
    }
}
