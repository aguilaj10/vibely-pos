@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
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
    val isActive: Boolean = true
)

@Serializable
data class UpdateCustomerRequest(
    @SerialName("code")
    val code: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("loyalty_points")
    val loyaltyPoints: Int? = null,
    @SerialName("loyalty_tier")
    val loyaltyTier: String? = null,
    @SerialName("total_purchases")
    val totalPurchases: Double? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)

@Serializable
data class AddLoyaltyPointsRequest(
    @SerialName("points")
    val points: Int
)
