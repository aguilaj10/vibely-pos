@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")
package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    val code: String,
    val firstName: String,
    val lastName: String,
    val email: String? = null,
    val phone: String? = null,
    val loyaltyPoints: Int = 0,
    val loyaltyTier: String? = null,
    val totalPurchases: Double = 0.0,
    val isActive: Boolean = true
)

@Serializable
data class UpdateCustomerRequest(
    val code: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val loyaltyPoints: Int? = null,
    val loyaltyTier: String? = null,
    val totalPurchases: Double? = null,
    val isActive: Boolean? = null
)

@Serializable
data class AddLoyaltyPointsRequest(
    val points: Int
)
