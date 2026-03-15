package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrderItem
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

/**
 * Unit tests for [CreatePurchaseOrderUseCase].
 */
class CreatePurchaseOrderUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var createResult: Result<PurchaseOrder>? = null
        var lastCreatedPurchaseOrder: PurchaseOrder? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> =
            Result.Success(emptyList())

        override suspend fun getById(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
            lastCreatedPurchaseOrder = purchaseOrder
            return createResult ?: Result.Error("Not configured")
        }

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Success("PO-0001")
    }

    @Test
    fun `invoke should return success when purchase order is created`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val now = Clock.System.now()
        val item = PurchaseOrderItem.create(
            id = "item-001",
            purchaseOrderId = "po-001",
            productId = "prod-001",
            productName = "Product A",
            quantity = 10,
            unitCost = 50.0,
        )
        val purchaseOrder = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 500.0,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Success(purchaseOrder)

        val useCase = CreatePurchaseOrderUseCase(mockRepo)

        // When
        val result = useCase(purchaseOrder)

        // Then
        assertIs<Result.Success<PurchaseOrder>>(result)
        assertEquals(purchaseOrder.id, result.data.id)
        assertEquals(purchaseOrder.poNumber, result.data.poNumber)
        assertEquals(purchaseOrder.supplierId, result.data.supplierId)
        assertEquals(purchaseOrder, mockRepo.lastCreatedPurchaseOrder)
    }

    @Test
    fun `invoke should return error when purchase order has no items`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val now = Clock.System.now()
        val purchaseOrder = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 500.0,
            items = emptyList(),
            createdAt = now,
            updatedAt = now,
        )

        val useCase = CreatePurchaseOrderUseCase(mockRepo)

        // When
        val result = useCase(purchaseOrder)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Purchase order must have at least one item", result.message)
        assertEquals("EMPTY_ITEMS", result.code)
    }

    @Test
    fun `invoke should return error when purchase order has zero total amount`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val now = Clock.System.now()
        val item = PurchaseOrderItem.create(
            id = "item-001",
            purchaseOrderId = "po-001",
            productId = "prod-001",
            productName = "Product A",
            quantity = 10,
            unitCost = 50.0,
        )
        val purchaseOrder = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 0.0,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )

        val useCase = CreatePurchaseOrderUseCase(mockRepo)

        // When
        val result = useCase(purchaseOrder)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Purchase order total amount must be positive", result.message)
        assertEquals("INVALID_TOTAL", result.code)
    }

    @Test
    fun `invoke should return error when creation fails`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val now = Clock.System.now()
        val item = PurchaseOrderItem.create(
            id = "item-001",
            purchaseOrderId = "po-001",
            productId = "prod-001",
            productName = "Product A",
            quantity = 10,
            unitCost = 50.0,
        )
        val purchaseOrder = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 500.0,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.createResult = Result.Error(
            message = "Supplier not found",
            code = "SUPPLIER_NOT_FOUND",
        )

        val useCase = CreatePurchaseOrderUseCase(mockRepo)

        // When
        val result = useCase(purchaseOrder)

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Supplier not found", result.message)
        assertEquals("SUPPLIER_NOT_FOUND", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
