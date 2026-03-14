package com.vibely.pos.shared.domain.dashboard.usecase

import com.vibely.pos.shared.domain.dashboard.entity.AlertSeverity
import com.vibely.pos.shared.domain.dashboard.entity.DashboardSummary
import com.vibely.pos.shared.domain.dashboard.entity.LowStockProduct
import com.vibely.pos.shared.domain.dashboard.entity.RecentTransaction
import com.vibely.pos.shared.domain.dashboard.repository.DashboardRepository
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.valueobject.Money
import com.vibely.pos.shared.domain.valueobject.SKU
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetLowStockProductsUseCaseTest {

    // Mock repository for testing
    private class MockDashboardRepository : DashboardRepository {
        var lowStockResult: Result<List<LowStockProduct>>? = null

        override suspend fun getDashboardSummary(): Result<DashboardSummary> = Result.Error("Not implemented")

        override suspend fun getRecentTransactions(limit: Int): Result<List<RecentTransaction>> = Result.Success(emptyList())

        override suspend fun getLowStockProducts(): Result<List<LowStockProduct>> = lowStockResult ?: Result.Error("Not configured")

        override suspend fun refreshDashboard(): Result<Unit> = Result.Success(Unit)
    }

    @Test
    fun `invoke should return success with products when repository succeeds`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val expectedProducts = listOf(
            LowStockProduct.create(
                id = "prod-1",
                sku = SKU.create("SKU-001"),
                name = "Product A",
                currentStock = 5,
                minStockLevel = 10,
                sellingPrice = Money.fromAmount(100.00, "PHP"),
                categoryName = "Category 1",
            ),
            LowStockProduct.create(
                id = "prod-2",
                sku = SKU.create("SKU-002"),
                name = "Product B",
                currentStock = 2,
                minStockLevel = 20,
                sellingPrice = Money.fromAmount(200.00, "PHP"),
                categoryName = "Category 2",
            ),
        )
        mockRepo.lowStockResult = Result.Success(expectedProducts)

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<LowStockProduct>>>(result)
        assertEquals(2, result.data.size)
        // Products are sorted by severity (both HIGH in this case, but by ordinal descending)
        // Both have HIGH severity so order might vary, just check both are present
        assertTrue(result.data.any { it.id == "prod-1" })
        assertTrue(result.data.any { it.id == "prod-2" })
    }

    @Test
    fun `invoke should sort products by severity - CRITICAL first`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val products = listOf(
            // MEDIUM severity (5 out of 10)
            LowStockProduct.create(
                id = "prod-medium",
                sku = SKU.create("SKU-001"),
                name = "Medium Stock",
                currentStock = 5,
                minStockLevel = 10,
                sellingPrice = Money.fromAmount(100.00, "PHP"),
                categoryName = "Category",
            ),
            // CRITICAL severity (out of stock)
            LowStockProduct.create(
                id = "prod-critical",
                sku = SKU.create("SKU-002"),
                name = "Out of Stock",
                currentStock = 0,
                minStockLevel = 10,
                sellingPrice = Money.fromAmount(100.00, "PHP"),
                categoryName = "Category",
            ),
            // HIGH severity (less than 50% of min)
            LowStockProduct.create(
                id = "prod-high",
                sku = SKU.create("SKU-003"),
                name = "Low Stock",
                currentStock = 4,
                minStockLevel = 10,
                sellingPrice = Money.fromAmount(100.00, "PHP"),
                categoryName = "Category",
            ),
        )
        mockRepo.lowStockResult = Result.Success(products)

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<LowStockProduct>>>(result)
        assertEquals(3, result.data.size)
        // Verify sorting: CRITICAL > HIGH > MEDIUM
        assertEquals(AlertSeverity.CRITICAL, result.data[0].alertSeverity())
        assertEquals("prod-critical", result.data[0].id)
        assertEquals(AlertSeverity.HIGH, result.data[1].alertSeverity())
        assertEquals("prod-high", result.data[1].id)
        assertEquals(AlertSeverity.MEDIUM, result.data[2].alertSeverity())
        assertEquals("prod-medium", result.data[2].id)
    }

    @Test
    fun `invoke should return empty list when no low stock products`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.lowStockResult = Result.Success(emptyList())

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<LowStockProduct>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.lowStockResult = Result.Error(
            message = "Network error",
            code = "NETWORK_ERROR",
        )

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Network error", result.message)
        assertEquals("NETWORK_ERROR", result.code)
    }

    @Test
    fun `invoke should return error when unauthorized`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.lowStockResult = Result.Error(
            message = "User is not authenticated",
            code = "UNAUTHORIZED",
        )

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("User is not authenticated", result.message)
        assertEquals("UNAUTHORIZED", result.code)
    }

    @Test
    fun `invoke should return error when server error occurs`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        mockRepo.lowStockResult = Result.Error(
            message = "Backend service error",
            code = "SERVER_ERROR",
        )

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Error>(result)
        assertEquals("Backend service error", result.message)
        assertEquals("SERVER_ERROR", result.code)
    }

    @Test
    fun `invoke should handle multiple products with same severity`() = runTest {
        // Given
        val mockRepo = MockDashboardRepository()
        val products = listOf(
            LowStockProduct.create(
                id = "prod-1",
                sku = SKU.create("SKU-001"),
                name = "Product 1",
                currentStock = 0,
                minStockLevel = 10,
                sellingPrice = Money.fromAmount(100.00, "PHP"),
                categoryName = "Category",
            ),
            LowStockProduct.create(
                id = "prod-2",
                sku = SKU.create("SKU-002"),
                name = "Product 2",
                currentStock = 0,
                minStockLevel = 15,
                sellingPrice = Money.fromAmount(150.00, "PHP"),
                categoryName = "Category",
            ),
        )
        mockRepo.lowStockResult = Result.Success(products)

        val useCase = GetLowStockProductsUseCase(mockRepo)

        // When
        val result = useCase()

        // Then
        assertIs<Result.Success<List<LowStockProduct>>>(result)
        assertEquals(2, result.data.size)
        // Both should be CRITICAL
        assertEquals(AlertSeverity.CRITICAL, result.data[0].alertSeverity())
        assertEquals(AlertSeverity.CRITICAL, result.data[1].alertSeverity())
    }

    // Helper function for running suspending tests
    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest { block() }
    }
}
