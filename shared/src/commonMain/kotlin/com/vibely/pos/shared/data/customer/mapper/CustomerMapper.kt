package com.vibely.pos.shared.data.customer.mapper

import com.vibely.pos.shared.data.customer.dto.CustomerDTO
import com.vibely.pos.shared.domain.customer.entity.Customer
import kotlin.time.Instant

object CustomerMapper {
    fun toDomain(dto: CustomerDTO): Customer = Customer.create(
        id = dto.id,
        code = dto.code,
        firstName = dto.firstName,
        lastName = dto.lastName,
        email = dto.email,
        phone = dto.phone,
        loyaltyPoints = dto.loyaltyPoints,
        loyaltyTier = dto.loyaltyTier,
        totalPurchases = dto.totalPurchases,
        isActive = dto.isActive,
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )

    fun toDTO(customer: Customer): CustomerDTO = CustomerDTO(
        id = customer.id,
        code = customer.code,
        firstName = customer.firstName,
        lastName = customer.lastName,
        email = customer.email,
        phone = customer.phone,
        loyaltyPoints = customer.loyaltyPoints,
        loyaltyTier = customer.loyaltyTier,
        totalPurchases = customer.totalPurchases,
        isActive = customer.isActive,
        createdAt = customer.createdAt.toString(),
        updatedAt = customer.updatedAt.toString(),
    )
}
