package com.vibely.pos.shared.data.sales.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.sales.datasource.RemoteSaleDataSource
import com.vibely.pos.shared.data.sales.dto.CreateSaleItemRequest
import com.vibely.pos.shared.data.sales.dto.CreateSaleRequest
import com.vibely.pos.shared.data.sales.mapper.SaleItemMapper
import com.vibely.pos.shared.data.sales.mapper.SaleMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.time.Instant

class SaleRepositoryImpl(private val remoteDataSource: RemoteSaleDataSource) :
    BaseRepository(),
    SaleRepository {

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

        return mapSingle(remoteDataSource.createSale(request), SaleMapper::toDomain)
    }

    override suspend fun getAll(startDate: Instant?, endDate: Instant?, status: SaleStatus?, page: Int, pageSize: Int): Result<List<Sale>> = mapList(
        remoteDataSource.getAllSales(
            startDate = startDate?.toString(),
            endDate = endDate?.toString(),
            status = status?.name?.lowercase(),
            page = page,
            pageSize = pageSize,
        ),
        SaleMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<Sale> = mapSingle(remoteDataSource.getSaleById(id), SaleMapper::toDomain)

    override suspend fun getItems(saleId: String): Result<List<SaleItem>> = mapList(remoteDataSource.getSaleItems(saleId), SaleItemMapper::toDomain)

    override suspend fun updateStatus(saleId: String, status: SaleStatus): Result<Sale> =
        mapSingle(remoteDataSource.updateSaleStatus(saleId, status.name.lowercase()), SaleMapper::toDomain)
}
