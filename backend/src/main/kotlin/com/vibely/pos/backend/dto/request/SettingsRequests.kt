package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

/**
 * Request data classes for Settings operations.
 */

/**
 * Request body for updating store information.
 *
 * @property storeName Name of the business/store.
 * @property address Physical store address.
 * @property phone Contact phone number.
 * @property email Contact email address.
 */
@Serializable
data class UpdateStoreInfoRequest(
    val storeName: String,
    val address: String,
    val phone: String,
    val email: String,
)

/**
 * Request body for updating receipt settings.
 *
 * @property header Text displayed at the top of receipts.
 * @property footer Text displayed at the bottom of receipts.
 * @property logoUrl Optional URL to store logo image.
 * @property showTax Whether to display tax breakdown on receipts.
 */
@Serializable
data class UpdateReceiptSettingsRequest(
    val header: String,
    val footer: String,
    val logoUrl: String? = null,
    val showTax: Boolean,
)

/**
 * Request body for updating tax settings.
 *
 * @property taxRate Tax percentage rate (e.g., 16.0 for 16%).
 * @property currency ISO 4217 currency code (e.g., "USD", "MXN", "EUR").
 */
@Serializable
data class UpdateTaxSettingsRequest(
    val taxRate: Double,
    val currency: String,
)

/**
 * Request body for updating user preferences.
 *
 * @property language ISO 639-1 language code (e.g., "en", "es", "fr").
 * @property theme Application theme preference ("light", "dark", "system").
 * @property enableNotifications Whether to show system notifications.
 * @property autoLogoutTimeoutMinutes Duration of inactivity in minutes before automatic logout.
 */
@Serializable
data class UpdateUserPreferencesRequest(
    val language: String,
    val theme: String,
    val enableNotifications: Boolean,
    val autoLogoutTimeoutMinutes: Int,
)
