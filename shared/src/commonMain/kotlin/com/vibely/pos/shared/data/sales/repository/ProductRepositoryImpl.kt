package com.vibely.pos.shared.data.sales.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.sales.datasource.RemoteProductDataSource
import com.vibely.pos.shared.data.sales.mapper.ProductMapper
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository

class ProductRepositoryImpl(private val remoteDataSource: RemoteProductDataSource) :
    BaseRepository(),
    ProductRepository {

    override suspend fun search(query: String): Result<List<Product>> = mapList(remoteDataSource.searchProducts(query), ProductMapper::toDomain)

    override suspend fun getById(id: String): Result<Product> = mapSingle(remoteDataSource.getProductById(id), ProductMapper::toDomain)

    override suspend fun checkStock(productId: String, quantity: Int): Result<Boolean> = remoteDataSource.checkStock(productId, quantity)

    override suspend fun getAll(categoryId: String?, isActive: Boolean?, lowStockOnly: Boolean, page: Int, pageSize: Int): Result<List<Product>> =
        mapList(remoteDataSource.getAllProducts(categoryId, isActive, lowStockOnly, page, pageSize), ProductMapper::toDomain)

    override suspend fun create(product: Product): Result<Product> = mapSingle(
        remoteDataSource.createProduct(ProductMapper.toDTO(product)),
        ProductMapper::toDomain,
    )

    override suspend fun update(product: Product): Result<Product> = mapSingle(
        remoteDataSource.updateProduct(product.id, ProductMapper.toDTO(product)),
        ProductMapper::toDomain,
    )

    override suspend fun delete(id: String): Result<Unit> = remoteDataSource.deleteProduct(id)
}
