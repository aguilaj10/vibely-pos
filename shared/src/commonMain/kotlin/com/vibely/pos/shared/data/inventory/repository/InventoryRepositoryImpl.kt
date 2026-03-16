package com.vibely.pos.shared.data.inventory.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.inventory.datasource.RemoteInventoryDataSource
import com.vibely.pos.shared.data.inventory.mapper.InventoryTransactionMapper
import com.vibely.pos.shared.domain.inventory.entity.InventoryTransaction
import com.vibely.pos.shared.domain.inventory.entity.TransactionType
import com.vibely.pos.shared.domain.inventory.repository.InventoryRepository
import com.vibely.pos.shared.domain.result.Result
import kotlin.time.Instant

class InventoryRepositoryImpl(private val remoteDataSource: RemoteInventoryDataSource) :
    BaseRepository(),
    InventoryRepository {

    override suspend fun getAll(
        productId: String?,
        transactionType: TransactionType?,
        startDate: Instant?,
        endDate: Instant?,
        page: Int,
        pageSize: Int,
    ): Result<List<InventoryTransaction>> = mapList(
        remoteDataSource.getTransactions(
            productId = productId,
            type = transactionType?.name?.lowercase(),
            startDate = startDate?.toString(),
            endDate = endDate?.toString(),
            page = page,
            pageSize = pageSize,
        ),
        InventoryTransactionMapper::toDomain,
    )

    override suspend fun getById(id: String): Result<InventoryTransaction> = mapSingle(
        remoteDataSource.getTransactionById(id),
        InventoryTransactionMapper::toDomain,
    )

    override suspend fun create(transaction: InventoryTransaction): Result<InventoryTransaction> = mapSingle(
        remoteDataSource.createTransaction(InventoryTransactionMapper.toDTO(transaction)),
        InventoryTransactionMapper::toDomain,
    )

    override suspend fun getByProduct(productId: String, page: Int, pageSize: Int): Result<List<InventoryTransaction>> = mapList(
        remoteDataSource.getTransactions(
            productId = productId,
            page = page,
            pageSize = pageSize,
        ),
        InventoryTransactionMapper::toDomain,
    )
}
