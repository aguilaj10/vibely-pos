package com.vibely.pos.shared.domain.purchaseorder.usecase

import com.vibely.pos.shared.domain.purchaseorder.entity.PurchaseOrder
import com.vibely.pos.shared.domain.purchaseorder.repository.PurchaseOrderRepository
import com.vibely.pos.shared.domain.purchaseorder.valueobject.PurchaseOrderStatus
import com.vibely.pos.shared.domain.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class GetAllPurchaseOrdersUseCaseTest {

    private class MockPurchaseOrderRepository : PurchaseOrderRepository {
        var getAllResult: Result<List<PurchaseOrder>>? = null
        var lastSupplierId: String? = null
        var lastStatus: PurchaseOrderStatus? = null
        var lastPage: Int? = null
        var lastPageSize: Int? = null

        override suspend fun getAll(supplierId: String?, status: PurchaseOrderStatus?, page: Int, pageSize: Int): Result<List<PurchaseOrder>> {
            lastSupplierId = supplierId
            lastStatus = status
            lastPage = page
            lastPageSize = pageSize
            return getAllResult ?: Result.Error("Not configured")
        }

        override suspend fun getById(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun create(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun update(purchaseOrder: PurchaseOrder): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun updateStatus(id: String, status: PurchaseOrderStatus): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun delete(id: String): Result<Unit> = Result.Error("Not implemented")

        override suspend fun receive(id: String): Result<PurchaseOrder> = Result.Error("Not implemented")

        override suspend fun generatePoNumber(): Result<String> = Result.Error("Not implemented")
    }

    private fun createTestPurchaseOrder(
        id: String,
        poNumber: String,
        supplierId: String = "sup-001",
        status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
    ): PurchaseOrder {
        val now = Clock.System.now()
        return PurchaseOrder.create(
            id = id,
            poNumber = poNumber,
            supplierId = supplierId,
            createdById = "user-001",
            totalAmount = 1000.0,
            supplierName = "Test Supplier",
            status = status,
            orderDate = now,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `invoke should return all purchase orders`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val purchaseOrders = listOf(
            createTestPurchaseOrder("po-001", "PO-2024-001"),
            createTestPurchaseOrder("po-002", "PO-2024-002"),
        )
        mockRepo.getAllResult = Result.Success(purchaseOrders)

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<PurchaseOrder>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("PO-2024-001", result.data[0].poNumber)
        assertEquals("PO-2024-002", result.data[1].poNumber)
    }

    @Test
    fun `invoke should return empty list when no purchase orders exist`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<PurchaseOrder>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when fetch fails`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Error(
            message = "Database connection failed",
            code = "DB_ERROR",
        )

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Database connection failed", result.message)
        assertEquals("DB_ERROR", result.code)
    }

    @Test
    fun `invoke should pass supplier filter to repository`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        useCase(supplierId = "sup-123")

        // Then
        assertEquals("sup-123", mockRepo.lastSupplierId)
    }

    @Test
    fun `invoke should pass status filter to repository`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        useCase(status = PurchaseOrderStatus.APPROVED)

        // Then
        assertEquals(PurchaseOrderStatus.APPROVED, mockRepo.lastStatus)
    }

    @Test
    fun `invoke should pass default pagination parameters`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        useCase()

        // Then
        assertEquals(null, mockRepo.lastSupplierId)
        assertEquals(null, mockRepo.lastStatus)
        assertEquals(1, mockRepo.lastPage)
        assertEquals(50, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should pass custom pagination parameters`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        mockRepo.getAllResult = Result.Success(emptyList())

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        useCase(supplierId = "sup-456", status = PurchaseOrderStatus.PENDING, page = 3, pageSize = 25)

        // Then
        assertEquals("sup-456", mockRepo.lastSupplierId)
        assertEquals(PurchaseOrderStatus.PENDING, mockRepo.lastStatus)
        assertEquals(3, mockRepo.lastPage)
        assertEquals(25, mockRepo.lastPageSize)
    }

    @Test
    fun `invoke should filter by status and return matching orders`() = runTest {
        // Given
        val mockRepo = MockPurchaseOrderRepository()
        val approvedOrders = listOf(
            createTestPurchaseOrder("po-001", "PO-2024-001", status = PurchaseOrderStatus.APPROVED),
            createTestPurchaseOrder("po-002", "PO-2024-002", status = PurchaseOrderStatus.APPROVED),
        )
        mockRepo.getAllResult = Result.Success(approvedOrders)

        val useCase = GetAllPurchaseOrdersUseCase(mockRepo)

        // When
        val result = useCase(status = PurchaseOrderStatus.APPROVED)

        // Then
        assertIs<Result.Success<List<PurchaseOrder>>>(result)
        assertEquals(2, result.data.size)
        assertTrue(result.data.all { it.status == PurchaseOrderStatus.APPROVED })
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
