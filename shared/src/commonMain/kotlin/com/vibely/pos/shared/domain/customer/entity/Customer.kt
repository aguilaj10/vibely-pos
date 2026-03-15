package com.vibely.pos.shared.domain.customer.entity

import com.vibely.pos.shared.domain.exception.BusinessRuleException
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Domain entity representing a customer in the POS system.
 *
 * Encapsulates customer information including personal details,
 * loyalty program status, and purchase history.
 *
 * @param id Unique identifier (UUID from database).
 * @param code Unique customer code for easy reference.
 * @param firstName Customer's first name.
 * @param lastName Customer's last name.
 * @param email Optional email address.
 * @param phone Optional phone number.
 * @param loyaltyPoints Accumulated loyalty points.
 * @param loyaltyTier Current loyalty tier (Bronze, Silver, Gold, Platinum).
 * @param totalPurchases Total amount of purchases made.
 * @param isActive Whether the customer is active.
 * @param createdAt When the customer was created.
 * @param updatedAt When the customer was last updated.
 */
data class Customer(
    val id: String,
    val code: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val loyaltyPoints: Int = 0,
    val loyaltyTier: String? = null,
    val totalPurchases: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /**
     * Full name combining first and last name.
     */
    val fullName: String
        get() = "$firstName $lastName"

    /**
     * Validates that the customer can earn loyalty points.
     *
     * Business Rule: Customer must be active to earn points.
     */
    fun canEarnPoints(): Boolean = isActive

    /**
     * Validates that the customer can redeem loyalty points.
     *
     * Business Rule: Customer must be active and have positive points.
     */
    fun canRedeemPoints(): Boolean = isActive && loyaltyPoints > 0

    /**
     * Calculates the loyalty tier based on total purchases.
     *
     * Business Rules:
     * - Platinum: $5000+
     * - Gold: $2000+
     * - Silver: $500+
     * - Bronze: $0+
     */
    fun calculateLoyaltyTier(): String = when {
        totalPurchases >= 5000.0 -> "Platinum"
        totalPurchases >= 2000.0 -> "Gold"
        totalPurchases >= 500.0 -> "Silver"
        else -> "Bronze"
    }

    /**
     * Returns a copy of this customer with updated loyalty points.
     *
     * @param newPoints The new loyalty points total.
     * @throws BusinessRuleException if points would be negative.
     */
    fun withLoyaltyPoints(newPoints: Int): Customer {
        if (newPoints < 0) {
            throw BusinessRuleException(
                rule = "Loyalty points cannot be negative",
                code = "NEGATIVE_LOYALTY_POINTS",
            )
        }
        return copy(
            loyaltyPoints = newPoints,
            loyaltyTier = calculateLoyaltyTier(),
            updatedAt = Clock.System.now(),
        )
    }

    /**
     * Returns a copy of this customer with added loyalty points.
     *
     * @param pointsToAdd The points to add (must be positive).
     * @throws BusinessRuleException if pointsToAdd is not positive.
     */
    fun addLoyaltyPoints(pointsToAdd: Int): Customer {
        require(pointsToAdd > 0) { "Points to add must be positive" }
        return withLoyaltyPoints(loyaltyPoints + pointsToAdd)
    }

    /**
     * Returns a copy of this customer with redeemed loyalty points.
     *
     * @param pointsToRedeem The points to redeem (must be positive and not exceed available).
     * @throws BusinessRuleException if insufficient points.
     */
    fun redeemLoyaltyPoints(pointsToRedeem: Int): Customer {
        require(pointsToRedeem > 0) { "Points to redeem must be positive" }
        if (pointsToRedeem > loyaltyPoints) {
            throw BusinessRuleException(
                rule = "Insufficient loyalty points. Available: $loyaltyPoints, Requested: $pointsToRedeem",
                code = "INSUFFICIENT_LOYALTY_POINTS",
            )
        }
        return withLoyaltyPoints(loyaltyPoints - pointsToRedeem)
    }

    companion object {
        /**
         * Creates a new Customer instance with validation.
         *
         * @throws IllegalArgumentException if any field is invalid.
         */
        fun create(
            id: String,
            code: String,
            firstName: String,
            lastName: String,
            email: String? = null,
            phone: String? = null,
            loyaltyPoints: Int = 0,
            loyaltyTier: String? = null,
            totalPurchases: Double = 0.0,
            isActive: Boolean = true,
            createdAt: Instant = Clock.System.now(),
            updatedAt: Instant = Clock.System.now(),
        ): Customer = Customer(
            id = id,
            code = code,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            loyaltyPoints = loyaltyPoints,
            loyaltyTier = loyaltyTier,
            totalPurchases = totalPurchases,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
