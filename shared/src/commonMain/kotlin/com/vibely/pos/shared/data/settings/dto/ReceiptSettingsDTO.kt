package com.vibely.pos.shared.data.settings.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for receipt settings from the backend.
 *
 * Contains receipt formatting configuration including header/footer text,
 * logo URL, and tax display preferences.
 *
 * @param id Unique identifier (UUID from database).
 * @param header Text displayed at the top of receipts.
 * @param footer Text displayed at the bottom of receipts.
 * @param logoUrl Optional URL to store logo image.
 * @param showTax Whether to display tax breakdown on receipts.
 * @param createdAt When settings were created (epoch milliseconds).
 * @param updatedAt When settings were last updated (epoch milliseconds).
 */
@Serializable
data class ReceiptSettingsDTO(
    @SerialName("id")
    val id: String,

    @SerialName("header")
    val header: String,

    @SerialName("footer")
    val footer: String,

    @SerialName("logo_url")
    val logoUrl: String?,

    @SerialName("show_tax")
    val showTax: Boolean,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,
)
