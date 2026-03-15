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

class GetPurchaseOrderByIdUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var getByIdResult: Result<PurchaseOrder>? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> =
            Result.Success(emptyList())

        override suspend fun getById(id: String): Result<PurchaseOrder> = getByIdResult ?: Result.Error("Not configured")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Success("PO-0001")
    }

    @Test
    fun `invoke should return purchase order when found`() = runTest {
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
        mockRepo.getByIdResult = Result.Success(purchaseOrder)

        val useCase = GetPurchaseOrderByIdUseCase(mockRepo)

        val result = useCase("po-001")

        assertIs<Result.Success<PurchaseOrder>>(result)
        assertEquals("po-001", result.data.id)
        assertEquals("PO-0001", result.data.poNumber)
    }

    @Test
    fun `invoke should return error when purchase order not found`() = runTest {
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getByIdResult = Result.Error("Purchase order not found", "NOT_FOUND")

        val useCase = GetPurchaseOrderByIdUseCase(mockRepo)

        val result = useCase("po-999")

        assertIs<Result.Error>(result)
        assertEquals("NOT_FOUND", result.code)
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
