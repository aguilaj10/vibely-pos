package com.vibely.pos.backend.data.room.mapper

import com.vibely.pos.backend.data.room.entity.PaymentEntity
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import java.util.UUID
import kotlin.time.Clock

/**
 * Maps a [PaymentEntity] to its shared [PaymentDTO] representation.
 *
 * @return Populated [PaymentDTO]
 */
fun PaymentEntity.toDto(): PaymentDTO =
    PaymentDTO(
        id = id,
        saleId = saleId,
        amount = amount,
        paymentType = paymentMethod,
        status = status,
        referenceNumber = referenceNumber,
        notes = null,
        paymentDate = processedAt,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

/**
 * Maps a [CreatePaymentRequest] to a new [PaymentEntity] ready for Room insertion.
 *
 * Generates a new random UUID and sets the [PaymentEntity.status] to "completed".
 *
 * @param completedStatus The status string that represents a completed payment
 * @return New [PaymentEntity] with a generated ID and current timestamps
 */
fun CreatePaymentRequest.toEntity(completedStatus: String): PaymentEntity {
    val now = Clock.System.now().toString()
    return PaymentEntity(
        id = UUID.randomUUID().toString(),
        saleId = saleId,
        paymentMethod = paymentType,
        amount = amount,
        referenceNumber = referenceNumber,
        status = completedStatus,
        processedAt = now,
        createdAt = now,
    )
}
