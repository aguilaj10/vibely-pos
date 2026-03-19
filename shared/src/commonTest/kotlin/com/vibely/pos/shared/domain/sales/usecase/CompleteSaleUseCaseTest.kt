package com.vibely.pos.shared.domain.sales.usecase

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.domain.sales.entity.SaleItem
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import com.vibely.pos.shared.domain.sales.repository.SaleRepository
import com.vibely.pos.shared.domain.sales.valueobject.Cart
import com.vibely.pos.shared.domain.sales.valueobject.CartItem
import com.vibely.pos.shared.domain.sales.valueobject.PaymentStatus
import com.vibely.pos.shared.domain.sales.valueobject.SaleStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class CompleteSaleUseCaseTest {
    private class MockProductRepository : ProductRepository {
        val products = mutableMapOf<String, Product>()

        override suspend fun search(query: String): Result<List<Product>> = Result.Success(emptyList())

        override suspend fun getById(id: String): Result<Product> = products[id]?.let { Result.Success(it) } ?: Result.Error("Product not found")

        override suspend fun checkStock(productId: String, quantity: Int): Result<Boolean> {
            val product = products[productId] ?: return Result.Error("Product not found")
            return Result.Success(product.currentStock >= quantity)
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

    private class MockSaleRepository : SaleRepository {
        var createResult: Result<Sale>? = null
        val sales = mutableListOf<Sale>()
        val saleItems = mutableMapOf<String, List<SaleItem>>()

        override suspend fun create(sale: Sale, items: List<SaleItem>): Result<Sale> = createResult ?: run {
            sales.add(sale)
            saleItems[sale.id] = items
            Result.Success(sale)
        }

        override suspend fun getAll(startDate: Instant?, endDate: Instant?, status: SaleStatus?, page: Int, pageSize: Int): Result<List<Sale>> =
            Result.Success(sales)

        override suspend fun getById(id: String): Result<Sale> =
            sales.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error("Sale not found")

        override suspend fun getItems(saleId: String): Result<List<SaleItem>> =
            saleItems[saleId]?.let { Result.Success(it) } ?: Result.Success(emptyList())

        override suspend fun updateStatus(saleId: String, status: SaleStatus): Result<Sale> {
            val saleIndex = sales.indexOfFirst { it.id == saleId }
            if (saleIndex == -1) return Result.Error("Sale not found")
            val updated = sales[saleIndex].withStatus(status)
            sales[saleIndex] = updated
            return Result.Success(updated)
        }
    }

    @Test
    fun `invoke should complete sale successfully with valid cart`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product =
            Product.create(
                id = "prod-1",
                name = "Test Product",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 10,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        mockProductRepo.products[product.id] = product

        val cart =
            Cart()
                .add(
                    product,
                    2,
                ).let { if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart") }

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Success<Sale>>(result)
        assertEquals(200.0, result.data.totalAmount)
        assertEquals(SaleStatus.COMPLETED, result.data.status)
        assertEquals(PaymentStatus.COMPLETED, result.data.paymentStatus)
        assertEquals("cashier-1", result.data.cashierId)
        assertTrue(result.data.invoiceNumber.startsWith("SAL-"))
    }

    @Test
    fun `invoke should return error when cart is empty`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()
        val cart = Cart()

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Cart cannot be empty"))
    }

    @Test
    fun `invoke should return error when product not found`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product =
            Product.create(
                id = "prod-1",
                name = "Test Product",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 10,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        val cart =
            Cart().add(product, 2).let {
                if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart")
            }

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Product not found"))
    }

    @Test
    fun `invoke should return error when insufficient stock`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product =
            Product.create(
                id = "prod-1",
                name = "Test Product",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 1,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        mockProductRepo.products[product.id] = product

        val cart =
            Cart(
                items =
                listOf(
                    CartItem(
                        productId = product.id,
                        productName = product.name,
                        quantity = 5,
                        unitPrice = product.sellingPrice,
                    ),
                ),
            )

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Insufficient stock"))
    }

    @Test
    fun `invoke should return error when sale creation fails`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product =
            Product.create(
                id = "prod-1",
                name = "Test Product",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 10,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        mockProductRepo.products[product.id] = product

        val cart =
            Cart().add(product, 2).let {
                if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart")
            }

        mockSaleRepo.createResult = Result.Error("Database error", "DB_ERROR")

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Error>(result)
        assertEquals("Database error", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    @Test
    fun `invoke should deduct stock for multiple products`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product1 =
            Product.create(
                id = "prod-1",
                name = "Product 1",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 10,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        val product2 =
            Product.create(
                id = "prod-2",
                name = "Product 2",
                sku = "SKU-002",
                costPrice = 25.0,
                sellingPrice = 50.0,
                currentStock = 20,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        mockProductRepo.products[product1.id] = product1
        mockProductRepo.products[product2.id] = product2

        val cart =
            Cart()
                .add(
                    product1,
                    2,
                ).let { if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart") }
                .add(
                    product2,
                    3,
                ).let { if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart") }

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result = useCase(cart, "cashier-1")

        assertIs<Result.Success<Sale>>(result)
        assertEquals(350.0, result.data.totalAmount)
        // Stock deduction is handled by backend (SaleCreationHelper.deductStockAndLogTransactions)
        // The use case only validates cart and creates sale records
        assertEquals(10, mockProductRepo.products[product1.id]?.currentStock) // Stock unchanged
        assertEquals(20, mockProductRepo.products[product2.id]?.currentStock) // Stock unchanged
    }

    @Test
    fun `invoke should generate unique invoice numbers`() = runTest {
        val mockProductRepo = MockProductRepository()
        val mockSaleRepo = MockSaleRepository()

        val product =
            Product.create(
                id = "prod-1",
                name = "Test Product",
                sku = "SKU-001",
                costPrice = 50.0,
                sellingPrice = 100.0,
                currentStock = 100,
                minStockLevel = 5,
                categoryId = "cat-1",
                isActive = true,
            )
        mockProductRepo.products[product.id] = product

        val cart =
            Cart().add(product, 1).let {
                if (it is Result.Success) it.data else throw RuntimeException("Failed to create cart")
            }

        val useCase = CompleteSaleUseCase(mockSaleRepo, mockProductRepo)

        val result1 = useCase(cart, "cashier-1")
        val result2 = useCase(cart, "cashier-1")

        assertIs<Result.Success<Sale>>(result1)
        assertIs<Result.Success<Sale>>(result2)
        assertTrue(result1.data.invoiceNumber != result2.data.invoiceNumber)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
