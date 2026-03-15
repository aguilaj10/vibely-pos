package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrderItem
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.time.Clock

class DeletePurchaseOrderUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var getByIdResult: Result<PurchaseOrder>? = null
        var deleteResult: Result<Unit>? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> =
            Result.Success(emptyList())

        override suspend fun getById(id: String): Result<PurchaseOrder> = getByIdResult ?: Result.Error("Not configured")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = deleteResult ?: Result.Error("Not configured")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Success("PO-0001")
    }

    @Test
    fun `invoke should return success when purchase order is deleted`() = runTest {
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
        mockRepo.deleteResult = Result.Success(Unit)

        val useCase = DeletePurchaseOrderUseCase(mockRepo)

        val result = useCase("po-001")

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `invoke should return error when purchase order not found`() = runTest {
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getByIdResult = Result.Error("Purchase order not found", "NOT_FOUND")

        val useCase = DeletePurchaseOrderUseCase(mockRepo)

        val result = useCase("po-999")

        assertIs<Result.Error>(result)
    }

    @Test
    fun `invoke should return error when purchase order cannot be deleted`() = runTest {
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
        val receivedPO = PurchaseOrder.create(
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
        mockRepo.getByIdResult = Result.Success(receivedPO)

        val useCase = DeletePurchaseOrderUseCase(mockRepo)

        val result = useCase("po-001")

        assertIs<Result.Error>(result)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
