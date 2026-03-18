package com.vibely.pos.shared.data.sales.mapper

import com.vibely.pos.shared.data.sales.dto.PaymentDTO
import com.vibely.pos.shared.domain.sales.entity.Payment
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType
import kotlin.time.Instant

object PaymentMapper {
    fun toDomain(dto: PaymentDTO): Payment = Payment.create(
        id = dto.id,
        saleId = dto.saleId,
        amount = dto.amount,
        paymentType = PaymentType.fromDbValue(dto.paymentType),
        status = PaymentStatus.valueOf(dto.status.uppercase()),
        referenceNumber = dto.referenceNumber,
        notes = dto.notes,
        paymentDate = dto.paymentDate?.let { Instant.parse(it) } ?: Instant.parse(dto.createdAt),
        createdAt = Instant.parse(dto.createdAt),
        updatedAt = Instant.parse(dto.updatedAt),
    )
}
