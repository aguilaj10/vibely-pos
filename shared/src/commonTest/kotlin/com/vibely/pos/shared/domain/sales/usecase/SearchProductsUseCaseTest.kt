package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SearchProductsUseCaseTest {

    private class MockProductRepository : ProductRepository {
        val products = mutableMapOf<String, Product>()
        var searchResult: Result<List<Product>>? = null

        override suspend fun search(query: String): Result<List<Product>> = searchResult ?: Result.Success(
            products.values.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.sku.contains(query, ignoreCase = true) ||
                    it.barcode?.contains(query, ignoreCase = true) == true
            },
        )

        override suspend fun getById(id: String): Result<Product> = products[id]?.let { Result.Success(it) } ?: Result.Error("Product not found")

        override suspend fun checkStock(productId: String, quantity: Int): Result<Boolean> {
            val product = products[productId] ?: return Result.Error("Product not found")
            return Result.Success(product.canSell(quantity))
        }

        override suspend fun getAll(
            categoryId: String?,
            isActive: Boolean?,
            lowStockOnly: Boolean,
            page: Int,
            pageSize: Int,
        ): Result<List<Product>> = Result.Success(products.values.toList())

        override suspend fun create(product: Product): Result<Product> {
            products[product.id] = product
            return Result.Success(product)
        }

        override suspend fun update(product: Product): Result<Product> {
            products[product.id] = product
            return Result.Success(product)
        }

        override suspend fun delete(id: String): Result<Unit> {
            products.remove(id)
            return Result.Success(Unit)
        }
    }

    @Test
    fun `invoke should return products matching name`() = runTest {
        val mockRepo = MockProductRepository()
        val product1 = Product.create(
            id = "prod-1",
            name = "Laptop Computer",
            sku = "LAP-001",
            costPrice = 500.0,
            sellingPrice = 1000.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        val product2 = Product.create(
            id = "prod-2",
            name = "Desktop Computer",
            sku = "DES-001",
            costPrice = 600.0,
            sellingPrice = 1200.0,
            currentStock = 5,
            minStockLevel = 1,
        )
        val product3 = Product.create(
            id = "prod-3",
            name = "Mouse",
            sku = "MOU-001",
            costPrice = 10.0,
            sellingPrice = 20.0,
            currentStock = 50,
            minStockLevel = 10,
        )
        mockRepo.products[product1.id] = product1
        mockRepo.products[product2.id] = product2
        mockRepo.products[product3.id] = product3

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("computer")

        assertIs<Result.Success<List<Product>>>(result)
        assertEquals(2, result.data.size)
        assertTrue(result.data.any { it.id == product1.id })
        assertTrue(result.data.any { it.id == product2.id })
    }

    @Test
    fun `invoke should return products matching SKU`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-12345",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        mockRepo.products[product.id] = product

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("SKU-12345")

        assertIs<Result.Success<List<Product>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(product.id, result.data[0].id)
    }

    @Test
    fun `invoke should return products matching barcode`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            barcode = "1234567890123",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        mockRepo.products[product.id] = product

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("1234567890123")

        assertIs<Result.Success<List<Product>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(product.id, result.data[0].id)
    }

    @Test
    fun `invoke should be case insensitive`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Laptop Computer",
            sku = "LAP-001",
            costPrice = 500.0,
            sellingPrice = 1000.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        mockRepo.products[product.id] = product

        val useCase = SearchProductsUseCase(mockRepo)
        val result1 = useCase("LAPTOP")
        val result2 = useCase("laptop")
        val result3 = useCase("LaPtOp")

        assertIs<Result.Success<List<Product>>>(result1)
        assertIs<Result.Success<List<Product>>>(result2)
        assertIs<Result.Success<List<Product>>>(result3)
        assertEquals(1, result1.data.size)
        assertEquals(1, result2.data.size)
        assertEquals(1, result3.data.size)
    }

    @Test
    fun `invoke should return empty list when no matches found`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Laptop",
            sku = "LAP-001",
            costPrice = 500.0,
            sellingPrice = 1000.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        mockRepo.products[product.id] = product

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("nonexistent")

        assertIs<Result.Success<List<Product>>>(result)
        assertEquals(0, result.data.size)
    }

    @Test
    fun `invoke should return error when query is empty`() = runTest {
        val mockRepo = MockProductRepository()
        val useCase = SearchProductsUseCase(mockRepo)

        val result = useCase("")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("cannot be empty"))
    }

    @Test
    fun `invoke should return error when query is blank`() = runTest {
        val mockRepo = MockProductRepository()
        val useCase = SearchProductsUseCase(mockRepo)

        val result = useCase("   ")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("cannot be empty"))
    }

    @Test
    fun `invoke should return error when query is too short`() = runTest {
        val mockRepo = MockProductRepository()
        val useCase = SearchProductsUseCase(mockRepo)

        val result = useCase("a")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("at least 2 characters"))
    }

    @Test
    fun `invoke should trim whitespace from query`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Laptop",
            sku = "LAP-001",
            costPrice = 500.0,
            sellingPrice = 1000.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        mockRepo.products[product.id] = product

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("  Laptop  ")

        assertIs<Result.Success<List<Product>>>(result)
        assertEquals(1, result.data.size)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        val mockRepo = MockProductRepository()
        mockRepo.searchResult = Result.Error("Database error", "DB_ERROR")

        val useCase = SearchProductsUseCase(mockRepo)
        val result = useCase("laptop")

        assertIs<Result.Error>(result)
        assertEquals("Database error", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
