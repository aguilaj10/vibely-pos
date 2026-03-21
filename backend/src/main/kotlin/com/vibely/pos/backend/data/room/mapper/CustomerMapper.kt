package com.vibely.pos.backend.data.room.mapper

import com.vibely.pos.backend.data.room.entity.CustomerEntity
import com.vibely.pos.backend.dto.request.CreateCustomerRequest
import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import java.util.UUID
import kotlin.time.Clock

/**
 * Maps a [CustomerEntity] to its shared [CustomerDTO] representation.
 *
 * @return Populated [CustomerDTO]
 */
fun CustomerEntity.toDto(): CustomerDTO =
    CustomerDTO(
        id = id,
        code = code,
        fullName = fullName,
        email = email,
        phone = phone,
        loyaltyPoints = loyaltyPoints,
        loyaltyTier = loyaltyTier,
        totalPurchases = totalPurchases,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Maps a [CreateCustomerRequest] to a new [CustomerEntity] ready for Room insertion.
 *
 * Generates a new random UUID for [CustomerEntity.id] and sets both timestamps to the
 * current instant.
 *
 * @return New [CustomerEntity] with a generated ID and current timestamps
 */
fun CreateCustomerRequest.toEntity(): CustomerEntity {
    val now = Clock.System.now().toString()
    return CustomerEntity(
        id = UUID.randomUUID().toString(),
        code = code,
        fullName = fullName,
        email = email,
        phone = phone,
        loyaltyPoints = loyaltyPoints,
        loyaltyTier = loyaltyTier,
        totalPurchases = totalPurchases,
        isActive = isActive,
        createdAt = now,
        updatedAt = now,
    )
}
