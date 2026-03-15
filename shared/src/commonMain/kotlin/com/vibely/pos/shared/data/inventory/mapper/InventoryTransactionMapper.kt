package com.vibely.pos.shared.data.inventory.mapper

import com.vibely.pos.shared.data.inventory.dto.InventoryTransactionDTO
import com.vibely.pos.shared.domain.inventory.entity.InventoryTransaction
import com.vibely.pos.shared.domain.inventory.entity.TransactionType
import kotlin.time.Instant

/**
 * Mapper for converting between [InventoryTransactionDTO] and [InventoryTransaction] domain entity.
 */
object InventoryTransactionMapper {

    /**
     * Maps a [InventoryTransactionDTO] from the backend to a [InventoryTransaction] domain entity.
     *
     * @param dto The DTO from the backend.
     * @return The domain entity.
     * @throws IllegalArgumentException if any field cannot be parsed.
     */
    fun toDomain(dto: InventoryTransactionDTO): InventoryTransaction = InventoryTransaction.create(
        id = dto.id,
        productId = dto.productId,
        transactionType = TransactionType.valueOf(dto.transactionType.uppercase()),
        quantity = dto.quantity,
        performedBy = dto.performedBy,
        referenceId = dto.referenceId,
        referenceType = dto.referenceType,
        reason = dto.reason,
        notes = dto.notes,
        createdAt = Instant.parse(dto.createdAt),
    )

    /**
     * Maps a [InventoryTransaction] domain entity to a [InventoryTransactionDTO] for the backend.
     *
     * @param transaction The domain entity.
     * @return The DTO for the backend.
     */
    fun toDTO(transaction: InventoryTransaction): InventoryTransactionDTO = InventoryTransactionDTO(
        id = transaction.id,
        productId = transaction.productId,
        transactionType = transaction.transactionType.name.lowercase(),
        quantity = transaction.quantity,
        referenceId = transaction.referenceId,
        referenceType = transaction.referenceType,
        reason = transaction.reason,
        performedBy = transaction.performedBy,
        notes = transaction.notes,
        createdAt = transaction.createdAt.toString(),
    )
}
