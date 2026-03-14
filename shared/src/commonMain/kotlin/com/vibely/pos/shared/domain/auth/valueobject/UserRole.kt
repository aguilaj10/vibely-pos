package com.vibely.pos.shared.domain.auth.valueobject

/**
 * Enum representing the role of a user in the system.
 *
 * Defines authorization levels and permissions:
 * - [Admin]: Full system access, can manage users and all resources
 * - [Manager]: Can manage inventory, view reports, manage staff permissions
 * - [Cashier]: Can process sales, handle transactions, manage cash register
 * - [Warehouse]: Can manage inventory, receive/dispatch stock
 * - [Viewer]: Read-only access to reports and data
 */
enum class UserRole {
    ADMIN,
    MANAGER,
    CASHIER,
    WAREHOUSE,
    VIEWER,
    ;

    /**
     * Returns a human-readable display name for the role.
     */
    fun displayName(): String = when (this) {
        ADMIN -> "Administrator"
        MANAGER -> "Manager"
        CASHIER -> "Cashier"
        WAREHOUSE -> "Warehouse Staff"
        VIEWER -> "Viewer"
    }

    companion object {
        /**
         * Parses a role from a string value, case-insensitive.
         *
         * @param value The string representation of the role.
         * @return The corresponding [UserRole], or null if not found.
         */
        fun fromString(value: String?): UserRole? = when (value?.uppercase()) {
            "ADMIN" -> ADMIN
            "MANAGER" -> MANAGER
            "CASHIER" -> CASHIER
            "WAREHOUSE" -> WAREHOUSE
            "VIEWER" -> VIEWER
            else -> null
        }
    }
}
