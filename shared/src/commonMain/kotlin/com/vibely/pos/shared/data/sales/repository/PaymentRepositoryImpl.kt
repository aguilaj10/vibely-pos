package com.vibely.pos.shared.data.sales.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.sales.datasource.RemotePaymentDataSource
import com.vibely.pos.shared.data.sales.dto.CreatePaymentRequest
import com.vibely.pos.shared.data.sales.mapper.PaymentMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Payment
import com.vibely.pos.shared.domain.sales.repository.PaymentRepository
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType

class PaymentRepositoryImpl(private val remoteDataSource: RemotePaymentDataSource) :
    BaseRepository(),
    PaymentRepository {
    override suspend fun recordPayment(
        saleId: String,
        amount: Double,
        paymentType: PaymentType,
        referenceNumber: String?,
        notes: String?,
    ): Result<Payment> {
        val request =
            CreatePaymentRequest(
                saleId = saleId,
                amount = amount,
                paymentType = paymentType.dbValue,
                referenceNumber = referenceNumber,
                notes = notes,
            )
        return mapSingle(remoteDataSource.recordPayment(request), PaymentMapper::toDomain)
    }

    override suspend fun getPaymentsBySale(saleId: String): Result<List<Payment>> =
        mapList(remoteDataSource.getPaymentsBySale(saleId), PaymentMapper::toDomain)
}
