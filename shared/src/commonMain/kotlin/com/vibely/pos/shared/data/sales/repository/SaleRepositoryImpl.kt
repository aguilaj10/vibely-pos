package com.vibely.pos.shared.data.sales.repository

import com.vibely.pos.shared.data.sales.datasource.RemoteSaleDataSource
import com.vibely.pos.shared.data.sales.dto.CreateSaleItemRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.mapper.SaleItemMapper
import com.vibely.pos.shared.data.sales.mapper.SaleMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Instant

class SaleRepositoryImpl(private val remoteDataSource: RemoteSaleDataSource) : SaleRepository {

    override suspend fun create(sale: Sale, items: List<SaleItem>): Result<Sale> {
        val request = CreateSaleRequest(
            cashierId = sale.cashierId,
            customerId = sale.customerId,
            subtotal = sale.subtotal,
            taxAmount = sale.taxAmount,
            discountAmount = sale.discountAmount,
            totalAmount = sale.totalAmount,
            notes = sale.notes,
            items = items.map { item ->
                CreateSaleItemRequest(
                    productId = item.productId,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    discountAmount = item.discountAmount,
                )
            },
        )

        return remoteDataSource.createSale(request)
            .map { SaleMapper.toDomain(it) }
    }

    override suspend fun getAll(startDate: Instant?, endDate: Instant?, status: SaleStatus?, page: Int, pageSize: Int): Result<List<Sale>> =
        remoteDataSource.getAllSales(
            startDate = startDate?.toString(),
            endDate = endDate?.toString(),
            status = status?.name?.lowercase(),
            page = page,
            pageSize = pageSize,
        ).map { dtoList -> dtoList.map { SaleMapper.toDomain(it) } }

    override suspend fun getById(id: String): Result<Sale> = remoteDataSource.getSaleById(id)
        .map { SaleMapper.toDomain(it) }

    override suspend fun getItems(saleId: String): Result<List<SaleItem>> = remoteDataSource.getSaleItems(saleId)
        .map { dtoList -> dtoList.map { SaleItemMapper.toDomain(it) } }

    override suspend fun updateStatus(saleId: String, status: SaleStatus): Result<Sale> =
        remoteDataSource.updateSaleStatus(saleId, status.name.lowercase())
            .map { SaleMapper.toDomain(it) }
}
