package com.vibely.pos.shared.data.dashboard.mapper

import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.entity.TransactionStatus
import com.vibely.pos.shared.domain.valueobject.Money
import kotlin.time.Instant

/**
 * Mapper for converting between [RecentTransactionDTO] and [RecentTransaction] domain entity.
 */
object RecentTransactionMapper {

    /**
     * Maps a [RecentTransactionDTO] from the backend to a [RecentTransaction] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if status cannot be parsed.
     */
    fun toDomain(dto: RecentTransactionDTO): RecentTransaction {
        val totalAmount = Money.fromCents(dto.totalCents, "USD")
        val status = TransactionStatus.fromDatabaseValue(dto.status)
        val saleDate = Instant.parse(dto.saleDate)

        return RecentTransaction.create(
            id = dto.id,
            invoiceNumber = dto.invoiceNumber,
            totalAmount = totalAmount,
            status = status,
            saleDate = saleDate,
            customerName = dto.customerName,
        )
    }

    /**
     * Maps a [RecentTransaction] domain entity to a [RecentTransactionDTO] for the backend.
     *
     * @param transaction The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(transaction: RecentTransaction): RecentTransactionDTO = RecentTransactionDTO(
        id = transaction.id,
        invoiceNumber = transaction.invoiceNumber,
        totalCents = transaction.totalAmount.amountInCents,
        status = transaction.status.name.lowercase(),
        saleDate = transaction.saleDate.toString(),
        customerName = transaction.customerName,
    )
}
