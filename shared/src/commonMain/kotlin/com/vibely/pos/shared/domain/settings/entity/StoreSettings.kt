package com.vibely.pos.shared.domain.settings.entity

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing store information and configuration.
 *
 * Encapsulates core business details like name, address, and contact information
 * that are displayed on receipts and used throughout the POS system.
 *
 * @param id Unique identifier (UUID from database).
 * @param storeName Name of the business/store.
 * @param address Physical store address.
 * @param phone Contact phone number.
 * @param email Contact email address.
 * @param createdAt When settings were created.
 * @param updatedAt When settings were last updated.
 */
data class StoreSettings(
    val id: String,
    val storeName: String,
    val address: String,
    val phone: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(storeName.isNotBlank()) { "Store name cannot be blank" }
        require(address.isNotBlank()) { "Address cannot be blank" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.contains("@")) { "Email must contain @" }
    }

    companion object {
        /**
         * Creates a new StoreSettings instance with validation.
         *
         * @throws ValidationException if any field is invalid.
         */
        fun create(
            id: String,
            storeName: String,
            address: String,
            phone: String,
            email: String,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): StoreSettings {
            val trimmedStoreName = storeName.trim()
            val trimmedAddress = address.trim()
            val trimmedPhone = phone.trim()
            val trimmedEmail = email.trim()

            if (!isValidEmail(trimmedEmail)) {
                throw ValidationException(
                    field = "email",
                    message = "Invalid email format: $trimmedEmail",
                )
            }

            if (!isValidPhone(trimmedPhone)) {
                throw ValidationException(
                    field = "phone",
                    message = "Invalid phone format: $trimmedPhone. Must contain digits and may include +, -, (, ), or spaces.",
                )
            }

            return StoreSettings(
                id = id,
                storeName = trimmedStoreName,
                address = trimmedAddress,
                phone = trimmedPhone,
                email = trimmedEmail,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }

        private fun isValidEmail(email: String): Boolean {
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            return emailRegex.matches(email)
        }

        private fun isValidPhone(phone: String): Boolean {
            // Allow digits, spaces, and common separators: + - ( )
            val phoneRegex = Regex("^[\\d\\s+\\-()]+$")
            return phoneRegex.matches(phone) && phone.count { it.isDigit() } >= 7
        }
    }
}
