package com.vibely.pos.backend.routes

import com.vibely.pos.backend.services.DashboardService
import com.vibely.pos.shared.data.dashboard.dto.ActiveShiftInfoDTO
import com.vibely.pos.shared.data.dashboard.dto.DashboardSummaryDTO
import com.vibely.pos.shared.data.dashboard.dto.LowStockProductDTO
import com.vibely.pos.shared.data.dashboard.dto.RecentTransactionDTO
import com.vibely.pos.shared.domain.result.Result
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock
import com.vibely.pos.backend.config.configureTestAuthentication

class DashboardRoutesTest {

    private val mockDashboardService = mockk<DashboardService>(relaxed = true)

    private val testSummary = DashboardSummaryDTO(
        todaySalesCents = 250000,
        todayTransactionCount = 15,
        lowStockCount = 3,
        activeShift = ActiveShiftInfoDTO(
            shiftId = "shift-123",
            cashierId = "cashier-456",
            cashierName = "John Doe",
            openedAt = Clock.System.now().toString(),
            openingBalanceCents = 100000
        ),
        generatedAt = Clock.System.now().toString()
    )

    private val testTransactions = listOf(
        RecentTransactionDTO(
            id = "txn-1",
            invoiceNumber = "INV-001",
            totalCents = 50000,
            status = "completed",
            saleDate = Clock.System.now().toString(),
            customerName = "Customer 1"
        ),
        RecentTransactionDTO(
            id = "txn-2",
            invoiceNumber = "INV-002",
            totalCents = 75000,
            status = "completed",
            saleDate = Clock.System.now().toString(),
            customerName = null
        )
    )

    private val testLowStockProducts = listOf(
        LowStockProductDTO(
            id = "prod-1",
            sku = "SKU-001",
            name = "Product 1",
            currentStock = 0,
            minStockLevel = 10,
            sellingPriceCents = 10000,
            categoryName = "Category A"
        ),
        LowStockProductDTO(
            id = "prod-2",
            sku = "SKU-002",
            name = "Product 2",
            currentStock = 3,
            minStockLevel = 15,
            sellingPriceCents = 20000,
            categoryName = "Category B"
        )
    )

    private fun Application.configureTestDashboardRoutes(dashboardService: DashboardService) {
        configureTestAuthentication()
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        routing {
            dashboardRoutes(dashboardService)
        }
    }

    @Test
    fun `GET dashboard summary - successful request returns 200 with summary data`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        // Mock successful response
        coEvery { mockDashboardService.getDashboardSummary() } returns Result.Success(testSummary)

        val response = client.get("/api/dashboard/summary") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val summary = response.body<DashboardSummaryDTO>()
        assertNotNull(summary)
        assertEquals(250000, summary.todaySalesCents)
        assertEquals(15, summary.todayTransactionCount)
        assertEquals(3, summary.lowStockCount)
        assertNotNull(summary.activeShift)

        coVerify { mockDashboardService.getDashboardSummary() }
    }

    @Test
    fun `GET dashboard summary - unauthorized request returns 401`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/summary")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET dashboard summary - returns summary with no active shift`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val summaryWithoutShift = DashboardSummaryDTO(
            todaySalesCents = 0,
            todayTransactionCount = 0,
            lowStockCount = 0,
            activeShift = null,
            generatedAt = Clock.System.now().toString()
        )

        coEvery { mockDashboardService.getDashboardSummary() } returns Result.Success(summaryWithoutShift)

        val response = client.get("/api/dashboard/summary") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val summary = response.body<DashboardSummaryDTO>()
        assertNotNull(summary)
        assertEquals(null, summary.activeShift)
    }

    @Test
    fun `GET recent transactions - successful request returns 200 with transactions`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getRecentTransactions(10) } returns Result.Success(testTransactions)

        val response = client.get("/api/dashboard/recent-transactions?limit=10") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val transactions = response.body<List<RecentTransactionDTO>>()
        assertNotNull(transactions)
        assertEquals(2, transactions.size)
        assertEquals("txn-1", transactions[0].id)
        assertEquals("INV-001", transactions[0].invoiceNumber)

        coVerify { mockDashboardService.getRecentTransactions(10) }
    }

    @Test
    fun `GET recent transactions - uses default limit when not provided`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getRecentTransactions(10) } returns Result.Success(testTransactions)

        val response = client.get("/api/dashboard/recent-transactions") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockDashboardService.getRecentTransactions(10) }
    }

    @Test
    fun `GET recent transactions - limit of 1 returns 200`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getRecentTransactions(1) } returns Result.Success(listOf(testTransactions[0]))

        val response = client.get("/api/dashboard/recent-transactions?limit=1") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockDashboardService.getRecentTransactions(1) }
    }

    @Test
    fun `GET recent transactions - limit of 100 returns 200`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getRecentTransactions(100) } returns Result.Success(testTransactions)

        val response = client.get("/api/dashboard/recent-transactions?limit=100") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockDashboardService.getRecentTransactions(100) }
    }

    @Test
    fun `GET recent transactions - limit of 0 returns 400`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/recent-transactions?limit=0") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET recent transactions - negative limit returns 400`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/recent-transactions?limit=-1") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET recent transactions - limit over 100 returns 400`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/recent-transactions?limit=101") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET recent transactions - invalid limit format uses default limit`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getRecentTransactions(10) } returns Result.Success(testTransactions)

        val response = client.get("/api/dashboard/recent-transactions?limit=invalid") {
            bearerAuth("test-user-123")
        }

        // Invalid format falls back to default limit (10)
        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockDashboardService.getRecentTransactions(10) }
    }

    @Test
    fun `GET recent transactions - unauthorized request returns 401`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/recent-transactions")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET low stock products - successful request returns 200 with products`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getLowStockProducts() } returns Result.Success(testLowStockProducts)

        val response = client.get("/api/dashboard/low-stock") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val products = response.body<List<LowStockProductDTO>>()
        assertNotNull(products)
        assertEquals(2, products.size)
        assertEquals("prod-1", products[0].id)
        assertEquals("Product 1", products[0].name)
        assertEquals(0, products[0].currentStock)

        coVerify { mockDashboardService.getLowStockProducts() }
    }

    @Test
    fun `GET low stock products - returns empty list when no low stock`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        coEvery { mockDashboardService.getLowStockProducts() } returns Result.Success(emptyList())

        val response = client.get("/api/dashboard/low-stock") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val products = response.body<List<LowStockProductDTO>>()
        assertNotNull(products)
        assertEquals(0, products.size)
    }

    @Test
    fun `GET low stock products - unauthorized request returns 401`() = testApplication {
        application { configureTestDashboardRoutes(mockDashboardService) }
        val client = createTestClient()

        val response = client.get("/api/dashboard/low-stock")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
