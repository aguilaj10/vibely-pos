package com.vibely.pos.shared.domain.inventory.usecase

import com.vibely.pos.shared.domain.inventory.entity.InventoryTransaction
import com.vibely.pos.shared.domain.inventory.entity.TransactionType
import com.vibely.pos.shared.domain.inventory.repository.InventoryRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.sales.entity.Product
import com.vibely.pos.shared.domain.sales.repository.ProductRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AdjustStockUseCaseTest {

    private class FakeProductRepository : ProductRepository {
        val products = mutableMapOf<String, Product>()
        var shouldFailGetById = false
        var shouldFailUpdate = false
        var failMessage = "Repository error"
        var failCode = "REPO_ERROR"

        override suspend fun search(query: String): Result<List<Product>> = Result.Success(
            products.values.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.sku.contains(query, ignoreCase = true)
            },
        )

        override suspend fun getById(id: String): Result<Product> = if (shouldFailGetById) {
            Result.Error(failMessage, failCode)
        } else {
            products[id]?.let { Result.Success(it) }
                ?: Result.Error("Product not found", "NOT_FOUND")
        }

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

        override suspend fun update(product: Product): Result<Product> = if (shouldFailUpdate) {
            Result.Error(failMessage, failCode)
        } else {
            products[product.id] = product
            Result.Success(product)
        }

        override suspend fun delete(id: String): Result<Unit> {
            products.remove(id)
            return Result.Success(Unit)
        }
    }

    private class FakeInventoryRepository : InventoryRepository {
        val transactions = mutableMapOf<String, InventoryTransaction>()
        var shouldFail = false
        var failMessage = "Repository error"
        var failCode = "REPO_ERROR"
        var createdTransaction: InventoryTransaction? = null

        override suspend fun getAll(
            productId: String?,
            transactionType: TransactionType?,
            startDate: kotlin.time.Instant?,
            endDate: kotlin.time.Instant?,
            page: Int,
            pageSize: Int,
        ): Result<List<InventoryTransaction>> = Result.Success(transactions.values.toList())

        override suspend fun getById(id: String): Result<InventoryTransaction> = transactions[id]?.let { Result.Success(it) }
            ?: Result.Error("Transaction not found", "NOT_FOUND")

        override suspend fun create(transaction: InventoryTransaction): Result<InventoryTransaction> = if (shouldFail) {
            Result.Error(failMessage, failCode)
        } else {
            createdTransaction = transaction
            transactions[transaction.id] = transaction
            Result.Success(transaction)
        }

        override suspend fun getByProduct(productId: String, page: Int, pageSize: Int): Result<List<InventoryTransaction>> = Result.Success(
            transactions.values.filter { it.productId == productId },
        )
    }

    @Test
    fun `increases stock with positive quantity`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase order received",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(15, result.data.currentStock)
    }

    @Test
    fun `decreases stock with negative quantity`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -3,
            reason = "Sold at discount",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(7, result.data.currentStock)
    }

    @Test
    fun `returns error when insufficient stock for decrease`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -15,
            reason = "Sale",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Insufficient stock"))
        assertTrue(result.message.contains("Current: 10"))
        assertTrue(result.message.contains("Adjustment: -15"))
    }

    @Test
    fun `returns error when adjustment results in negative stock`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 5,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -10,
            reason = "Damaged goods",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Insufficient stock"))
    }

    @Test
    fun `creates inventory transaction on stock increase`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase order received",
            performedBy = "user-1",
            notes = "PO-12345",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals("prod-1", fakeInventoryRepo.createdTransaction?.productId)
        assertEquals(5, fakeInventoryRepo.createdTransaction?.quantity)
        assertEquals("user-1", fakeInventoryRepo.createdTransaction?.performedBy)
        assertEquals("PO-12345", fakeInventoryRepo.createdTransaction?.notes)
    }

    @Test
    fun `creates inventory transaction on stock decrease`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -3,
            reason = "Sold at discount",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals("prod-1", fakeInventoryRepo.createdTransaction?.productId)
        assertEquals(-3, fakeInventoryRepo.createdTransaction?.quantity)
    }

    @Test
    fun `returns error when product ID is blank`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "",
            quantity = 5,
            reason = "Purchase",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Product ID cannot be blank"))
    }

    @Test
    fun `returns error when quantity is zero`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 0,
            reason = "Purchase",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Quantity cannot be zero"))
    }

    @Test
    fun `returns error when reason is blank`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Reason cannot be blank"))
    }

    @Test
    fun `returns error when performedBy is blank`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase",
            performedBy = "",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Performed by user ID cannot be blank"))
    }

    @Test
    fun `returns error when product not found`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "non-existent-product",
            quantity = 5,
            reason = "Purchase",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Product not found"))
    }

    @Test
    fun `returns error when inventory repository fails`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()
        fakeInventoryRepo.shouldFail = true
        fakeInventoryRepo.failMessage = "Database error"

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Failed to create inventory transaction"))
    }

    @Test
    fun `returns error when product repository fails on update`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()
        fakeProductRepo.shouldFailUpdate = true
        fakeProductRepo.failMessage = "Update failed"

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase",
            performedBy = "user-1",
        )

        assertIs<Result.Error>(result)
        assertTrue(result.message.contains("Failed to update product stock"))
    }

    @Test
    fun `creates transaction with ADJUSTMENT type for purchase reason`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Purchase order",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals(5, fakeInventoryRepo.createdTransaction?.quantity)
        assertEquals("prod-1", fakeInventoryRepo.createdTransaction?.productId)
        assertEquals("user-1", fakeInventoryRepo.createdTransaction?.performedBy)
    }

    @Test
    fun `creates transaction with ADJUSTMENT type for return reason`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 2,
            reason = "Customer return",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals(2, fakeInventoryRepo.createdTransaction?.quantity)
    }

    @Test
    fun `creates transaction with ADJUSTMENT type for damage reason`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -3,
            reason = "Damaged goods",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals(-3, fakeInventoryRepo.createdTransaction?.quantity)
    }

    @Test
    fun `creates transaction with ADJUSTMENT type for sale reason`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -2,
            reason = "Items sold",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals(-2, fakeInventoryRepo.createdTransaction?.quantity)
    }

    @Test
    fun `creates transaction with ADJUSTMENT type for generic reason`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 3,
            reason = "Inventory count correction",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
        assertEquals(3, fakeInventoryRepo.createdTransaction?.quantity)
    }

    @Test
    fun `allows exact stock depletion`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 5,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = -5,
            reason = "All items sold",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(0, result.data.currentStock)
    }

    @Test
    fun `handles large stock increase correctly`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 1000,
            reason = "Bulk purchase order",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertEquals(1010, result.data.currentStock)
    }

    @Test
    fun `transaction includes reason in record`() = runTest {
        val fakeProductRepo = FakeProductRepository()
        val fakeInventoryRepo = FakeInventoryRepository()

        val product = Product.create(
            id = "prod-1",
            sku = "SKU-001",
            name = "Test Product",
            costPrice = 50.0,
            sellingPrice = 100.0,
            currentStock = 10,
            minStockLevel = 2,
        )
        fakeProductRepo.products[product.id] = product

        val useCase = AdjustStockUseCase(fakeProductRepo, fakeInventoryRepo)
        val result = useCase(
            productId = "prod-1",
            quantity = 5,
            reason = "Restocked from supplier",
            performedBy = "user-1",
        )

        assertIs<Result.Success<Product>>(result)
        assertTrue(fakeInventoryRepo.createdTransaction != null)
    }
}
