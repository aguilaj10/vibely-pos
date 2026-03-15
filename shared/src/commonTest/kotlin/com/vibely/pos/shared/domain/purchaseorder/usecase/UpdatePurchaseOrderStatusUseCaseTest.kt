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

class UpdatePurchaseOrderStatusUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var getByIdResult: Result<PurchaseOrder>? = null
        var updateStatusResult: Result<PurchaseOrder>? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> =
            Result.Success(emptyList())

        override suspend fun getById(id: String): Result<PurchaseOrder> = getByIdResult ?: Result.Error("Not configured")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> =
            updateStatusResult ?: Result.Error("Not configured")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Success("PO-0001")
    }

    @Test
    fun `invoke should return success when status is updated to APPROVED`() = runTest {
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
        val pendingPO = PurchaseOrder.create(
            id = "po-001",
            poNumber = "PO-0001",
            supplierId = "sup-001",
            createdById = "user-001",
            totalAmount = 500.0,
            status = PurchaseOrderStatus.PENDING,
            items = listOf(item),
            createdAt = now,
            updatedAt = now,
        )
        val approvedPO = pendingPO.withStatus(PurchaseOrderStatus.APPROVED)
        mockRepo.getByIdResult = Result.Success(pendingPO)
        mockRepo.updateStatusResult = Result.Success(approvedPO)

        val useCase = UpdatePurchaseOrderStatusUseCase(mockRepo)

        val result = useCase("po-001", PurchaseOrderStatus.APPROVED)

        assertIs<Result.Success<PurchaseOrder>>(result)
        assertEquals(PurchaseOrderStatus.APPROVED, result.data.status)
    }

    @Test
    fun `invoke should return error when purchase order not found`() = runTest {
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getByIdResult = Result.Error("Purchase order not found", "NOT_FOUND")

        val useCase = UpdatePurchaseOrderStatusUseCase(mockRepo)

        val result = useCase("po-999", PurchaseOrderStatus.APPROVED)

        assertIs<Result.Error>(result)
        assertEquals("NOT_FOUND", result.code)
    }

    @Test
    fun `invoke should return error for invalid status transition`() = runTest {
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
        // DRAFT status cannot be approved (only PENDING can be approved)
        val draftPO = PurchaseOrder.create(
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
        mockRepo.getByIdResult = Result.Success(draftPO)

        val useCase = UpdatePurchaseOrderStatusUseCase(mockRepo)

        val result = useCase("po-001", PurchaseOrderStatus.APPROVED)

        assertIs<Result.Error>(result)
        assertEquals("INVALID_STATUS_TRANSITION", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
