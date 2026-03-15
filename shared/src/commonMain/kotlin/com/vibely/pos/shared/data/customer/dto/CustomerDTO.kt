package com.vibely.pos.shared.data.customer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDTO(
    @SerialName("id")
    val id: String,
    @SerialName("code")
    val code: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("loyalty_points")
    val loyaltyPoints: Int = 0,
    @SerialName("loyalty_tier")
    val loyaltyTier: String? = null,
    @SerialName("total_purchases")
    val totalPurchases: Double = 0.0,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
)
