package com.vibely.pos.shared.data.settings.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for store settings from the backend.
 *
 * Contains store information including name, address, and contact details.
 *
 * @param id Unique identifier (UUID from database).
 * @param storeName Name of the business/store.
 * @param address Physical store address.
 * @param phone Contact phone number.
 * @param email Contact email address.
 * @param createdAt When settings were created (epoch milliseconds).
 * @param updatedAt When settings were last updated (epoch milliseconds).
 */
@Serializable
data class StoreSettingsDTO(
    @SerialName("id")
    val id: String,

    @SerialName("store_name")
    val storeName: String,

    @SerialName("address")
    val address: String,

    @SerialName("phone")
    val phone: String,

    @SerialName("email")
    val email: String,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long,
)
