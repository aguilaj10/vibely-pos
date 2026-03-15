package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import com.vibely.pos.shared.domain.sales.valueobject.Cart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AddToCartUseCaseTest {

    private class MockProductRepository : ProductRepository {
        val products = mutableMapOf<String, Product>()

        override suspend fun search(query: String): Result<List<Product>> = Result.Success(emptyList())

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
    fun `invoke should add item to empty cart when product exists and has stock`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, product.id, quantity = 2)

        assertIs<Result.Success<Cart>>(result)
        assertEquals(1, result.data.items.size)
        assertEquals(2, result.data.items[0].quantity)
        assertEquals(product.id, result.data.items[0].productId)
        assertEquals(200.0, result.data.totalAmount)
    }

    @Test
    fun `invoke should update quantity when adding existing item to cart`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val useCase = AddToCartUseCase(mockRepo)
        val initialCart = Cart()
        val addFirstResult = useCase(initialCart, product.id, quantity = 2).let { it as Result.Success }.data

        val result = useCase(addFirstResult, product.id, quantity = 3)

        assertIs<Result.Success<Cart>>(result)
        assertEquals(1, result.data.items.size)
        assertEquals(5, result.data.items[0].quantity)
        assertEquals(500.0, result.data.totalAmount)
    }

    @Test
    fun `invoke should return error when product not found`() = runTest {
        val mockRepo = MockProductRepository()
        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, "non-existent-id", quantity = 1)

        assertIs<Result.Error>(result)
        assertEquals("Product not found", result.message)
    }

    @Test
    fun `invoke should return error when insufficient stock`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 2,
            minStockLevel = 1,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, product.id, quantity = 5)

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("not available for sale") || result.message.contains("Insufficient stock"))
    }

    @Test
    fun `invoke should return error when trying to add zero quantity`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, product.id, quantity = 0)

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("positive"))
    }

    @Test
    fun `invoke should return error when trying to add negative quantity`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, product.id, quantity = -1)

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("positive"))
    }

    @Test
    fun `invoke should return error when product is inactive`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Inactive Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
            categoryId = "cat-1",
            isActive = false,
        )
        mockRepo.products[product.id] = product

        val cart = Cart()
        val useCase = AddToCartUseCase(mockRepo)

        val result = useCase(cart, product.id, quantity = 1)

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("not available for sale"))
    }

    @Test
    fun `invoke should return error when adding to existing item exceeds stock`() = runTest {
        val mockRepo = MockProductRepository()
        val product = Product.create(
            id = "prod-1",
            name = "Test Product",
            sku = "SKU-001",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 5,
            minStockLevel = 1,
            categoryId = "cat-1",
            isActive = true,
        )
        mockRepo.products[product.id] = product

        val useCase = AddToCartUseCase(mockRepo)
        val initialCart = Cart()
        val cartWith3Items = useCase(initialCart, product.id, quantity = 3).let { it as Result.Success }.data

        val result = useCase(cartWith3Items, product.id, quantity = 3)

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Insufficient stock"))
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
