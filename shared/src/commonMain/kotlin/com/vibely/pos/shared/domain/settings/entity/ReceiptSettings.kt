package com.vibely.pos.shared.domain.settings.entity

import com.vibely.pos.shared.domain.exception.ValidationException
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing receipt printing and display configuration.
 *
 * Controls how receipts appear to customers, including header/footer text,
 * logo display, and tax information visibility.
 *
 * @param id Unique identifier (UUID from database).
 * @param header Text displayed at the top of receipts (e.g., "Thank you for shopping with us!").
 * @param footer Text displayed at the bottom of receipts (e.g., "Return policy: 30 days with receipt").
 * @param logoUrl Optional URL or path to store logo image.
 * @param showTax Whether to display tax breakdown on receipts.
 * @param createdAt When settings were created.
 * @param updatedAt When settings were last updated.
 */
data class ReceiptSettings(
    val id: String,
    val header: String,
    val footer: String,
    val logoUrl: String?,
    val showTax: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(header.length <= MAX_HEADER_LENGTH) {
            "Receipt header cannot exceed $MAX_HEADER_LENGTH characters"
        }
        require(footer.length <= MAX_FOOTER_LENGTH) {
            "Receipt footer cannot exceed $MAX_FOOTER_LENGTH characters"
        }
    }

    companion object {
        private const val MAX_HEADER_LENGTH = 500
        private const val MAX_FOOTER_LENGTH = 500

        /**
         * Creates a new ReceiptSettings instance with validation.
         *
         * @throws ValidationException if any field is invalid.
         */
        fun create(
            id: String,
            header: String = "",
            footer: String = "",
            logoUrl: String? = null,
            showTax: Boolean = true,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): ReceiptSettings {
            val trimmedHeader = header.trim()
            val trimmedFooter = footer.trim()
            val trimmedLogoUrl = logoUrl?.trim()

            if (trimmedLogoUrl != null && !isValidUrl(trimmedLogoUrl)) {
                throw ValidationException(
                    field = "logoUrl",
                    message = "Invalid logo URL format: $trimmedLogoUrl",
                )
            }

            return ReceiptSettings(
                id = id,
                header = trimmedHeader,
                footer = trimmedFooter,
                logoUrl = trimmedLogoUrl,
                showTax = showTax,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }

        private fun isValidUrl(url: String): Boolean {
            val urlRegex = Regex("^(https?://|/)[^\\s]+$")
            return urlRegex.matches(url)
        }
    }
}
