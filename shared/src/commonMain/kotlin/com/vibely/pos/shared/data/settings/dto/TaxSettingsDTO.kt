package com.vibely.pos.shared.data.settings.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for tax settings from the backend.
 *
 * Contains tax rate and currency configuration.
 *
 * @param id Unique identifier (UUID from database).
 * @param taxRate Tax percentage rate (e.g., 16.0 for 16%).
 * @param currency ISO 4217 currency code (e.g., "USD", "MXN", "EUR").
 * @param createdAt When settings were created (epoch milliseconds).
 * @param updatedAt When settings were last updated (epoch milliseconds).
 */
@Serializable
data class TaxSettingsDTO(
    @SerialName("id")
    val id: String,

    @SerialName("tax_rate")
    val taxRate: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,
)
