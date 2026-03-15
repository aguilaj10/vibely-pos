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

class UpdatePurchaseOrderUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var updateResult: Result<PurchaseOrder>? = null
        var lastUpdatedPurchaseOrder: PurchaseOrder? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> =
            Result.Success(emptyList())

        override suspend fun getById(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> {
            lastUpdatedPurchaseOrder = purchaseOrder
            return updateResult ?: Result.Error("Not configured")
        }

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Success("PO-0001")
    }

    @Test
    fun `invoke should return success when purchase order is updated`() = runTest {
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
            status = PurchaseOrderStatus.DRAFT,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )
        mockRepo.updateResult = Result.Success(purchaseOrder)

        val useCase = UpdatePurchaseOrderUseCase(mockRepo)

        val result = useCase(purchaseOrder)

        assertIs<Result.Success<PurchaseOrder>>(result)
        assertEquals(purchaseOrder.id, result.data.id)
    }

    @Test
    fun `invoke should return error when purchase order cannot be modified`() = runTest {
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
            status = PurchaseOrderStatus.RECEIVED,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )

        val useCase = UpdatePurchaseOrderUseCase(mockRepo)

        val result = useCase(purchaseOrder)

        assertIs<Result.Error>(result)
        assertEquals("CANNOT_MODIFY", result.code)
    }

    @Test
    fun `invoke should return error when purchase order has no items`() = runTest {
        val mockRepo = MockPurchaseOrderRepository()
        val now = Clock.System.now()
        val purchaseOrder = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 500.0,
            status = PurchaseOrderStatus.DRAFT,
            items = emptyList(),
            createdAt = now,
            updatedAt = now,
        )

        val useCase = UpdatePurchaseOrderUseCase(mockRepo)

        val result = useCase(purchaseOrder)

        assertIs<Result.Error>(result)
        assertEquals("EMPTY_ITEMS", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
