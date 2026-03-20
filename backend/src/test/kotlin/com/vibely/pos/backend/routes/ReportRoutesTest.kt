package com.vibely.pos.backend.routes

import com.vibely.pos.backend.auth.ProdAuthProvider
import com.vibely.pos.backend.config.configureTestAuthentication
import com.vibely.pos.backend.services.ReportService
import com.vibely.pos.backend.dto.request.TimePeriodRequest
import com.vibely.pos.backend.dto.request.SalesTrendRequest
import com.vibely.pos.shared.data.reports.dto.CategoryBreakdownDTO
import com.vibely.pos.shared.data.reports.dto.CustomerAnalyticsDTO
import com.vibely.pos.shared.data.reports.dto.ProductPerformanceDTO
import com.vibely.pos.shared.data.reports.dto.SalesReportDTO
import com.vibely.pos.shared.data.reports.dto.SalesTrendDTO
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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

class ReportRoutesTest {

    private val mockReportService = mockk<ReportService>(relaxed = true)

    private val testSalesReport = SalesReportDTO(
        totalRevenue = 500000,
        totalCost = 300000,
        totalProfit = 200000,
        transactionCount = 25,
        averageTransactionValue = 20000
    )

    private val testTopProducts = listOf(
        ProductPerformanceDTO(
            productId = "prod-1",
            productName = "Product 1",
            quantitySold = 100,
            revenue = 100000,
            cost = 60000,
            profit = 40000
        ),
        ProductPerformanceDTO(
            productId = "prod-2",
            productName = "Product 2",
            quantitySold = 50,
            revenue = 50000,
            cost = 30000,
            profit = 20000
        )
    )

    private val testCategoryBreakdown = listOf(
        CategoryBreakdownDTO(
            categoryId = "cat-1",
            categoryName = "Electronics",
            revenue = 300000,
            transactionCount = 15
        ),
        CategoryBreakdownDTO(
            categoryId = "cat-2",
            categoryName = "Clothing",
            revenue = 200000,
            transactionCount = 10
        )
    )

    private val testCustomerAnalytics = listOf(
        CustomerAnalyticsDTO(
            customerId = "cust-1",
            customerName = "John Doe",
            totalSpent = 150000,
            visitCount = 10,
            lastVisit = 1700000000000
        ),
        CustomerAnalyticsDTO(
            customerId = "cust-2",
            customerName = "Jane Smith",
            totalSpent = 100000,
            visitCount = 5,
            lastVisit = 1690000000000
        )
    )

    private val testSalesTrend = listOf(
        SalesTrendDTO(
            timestamp = 1700000000000,
            revenue = 100000,
            transactionCount = 5
        ),
        SalesTrendDTO(
            timestamp = 1700100000000,
            revenue = 150000,
            transactionCount = 8
        )
    )

    private fun Application.configureTestReportRoutes(reportService: ReportService) {
        configureTestAuthentication()
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        routing {
            reportRoutes(reportService, ProdAuthProvider())
        }
    }

    @Test
    fun `POST sales report - successful request returns 200 with report data`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        coEvery { mockReportService.getSalesReport(any(), any()) } returns testSalesReport

        val response = client.post("/api/reports/sales") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val report = response.body<SalesReportDTO>()
        assertNotNull(report)
        assertEquals(500000, report.totalRevenue)
        assertEquals(25, report.transactionCount)

        coVerify { mockReportService.getSalesReport(any(), any()) }
    }

    @Test
    fun `POST sales report - missing start time returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                endTime = "2024-01-31"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sales report - missing end time returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                startTime = "2024-01-01"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sales report - unauthorized request returns 401`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales") {
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET top products - successful request returns 200 with products`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        coEvery { mockReportService.getTopProducts(any(), any(), any()) } returns testTopProducts

        val response = client.get("/api/reports/top-products?start_time=2024-01-01&end_time=2024-01-31&limit=10") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val products = response.body<List<ProductPerformanceDTO>>()
        assertNotNull(products)
        assertEquals(2, products.size)
        assertEquals("Product 1", products[0].productName)

        coVerify { mockReportService.getTopProducts(any(), any(), any()) }
    }

    @Test
    fun `GET top products - limit of 0 returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.get("/api/reports/top-products?start_time=2024-01-01&end_time=2024-01-31&limit=0") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET top products - limit over 100 returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.get("/api/reports/top-products?start_time=2024-01-01&end_time=2024-01-31&limit=101") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET top products - missing time parameters returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.get("/api/reports/top-products") {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET top products - unauthorized request returns 401`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.get("/api/reports/top-products?start_time=2024-01-01&end_time=2024-01-31")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST category breakdown - successful request returns 200 with breakdown`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        coEvery { mockReportService.getCategoryBreakdown(any(), any()) } returns testCategoryBreakdown

        val response = client.post("/api/reports/category-breakdown") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val breakdown = response.body<List<CategoryBreakdownDTO>>()
        assertNotNull(breakdown)
        assertEquals(2, breakdown.size)
        assertEquals("Electronics", breakdown[0].categoryName)

        coVerify { mockReportService.getCategoryBreakdown(any(), any()) }
    }

    @Test
    fun `POST category breakdown - missing time parameters returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/category-breakdown") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest())
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST category breakdown - unauthorized request returns 401`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/category-breakdown") {
            contentType(ContentType.Application.Json)
            setBody(TimePeriodRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET customer analytics - successful request returns 200 with analytics`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        coEvery { mockReportService.getCustomerAnalytics(any(), any(), any()) } returns testCustomerAnalytics

        val url = "/api/reports/customer-analytics?start_time=2024-01-01&end_time=2024-01-31&limit=10"
        val response = client.get(url) {
            bearerAuth("test-user-123")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val analytics = response.body<List<CustomerAnalyticsDTO>>()
        assertNotNull(analytics)
        assertEquals(2, analytics.size)
        assertEquals("John Doe", analytics[0].customerName)

        coVerify { mockReportService.getCustomerAnalytics(any(), any(), any()) }
    }

    @Test
    fun `GET customer analytics - limit validation returns 400 for invalid limits`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val urlNeg = "/api/reports/customer-analytics?start_time=2024-01-01&end_time=2024-01-31&limit=-1"
        val responseNeg = client.get(urlNeg) {
            bearerAuth("test-user-123")
        }
        assertEquals(HttpStatusCode.BadRequest, responseNeg.status)

        val urlOver = "/api/reports/customer-analytics?start_time=2024-01-01&end_time=2024-01-31&limit=101"
        val responseOver = client.get(urlOver) {
            bearerAuth("test-user-123")
        }
        assertEquals(HttpStatusCode.BadRequest, responseOver.status)
    }

    @Test
    fun `GET customer analytics - unauthorized request returns 401`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.get("/api/reports/customer-analytics?start_time=2024-01-01&end_time=2024-01-31")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST sales trend - successful request returns 200 with trend data`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        coEvery { mockReportService.getSalesTrend(any(), any(), any()) } returns testSalesTrend

        val response = client.post("/api/reports/sales-trend") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(SalesTrendRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31",
                granularity = "daily"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val trend = response.body<List<SalesTrendDTO>>()
        assertNotNull(trend)
        assertEquals(2, trend.size)

        coVerify { mockReportService.getSalesTrend(any(), any(), any()) }
    }

    @Test
    fun `POST sales trend - invalid granularity returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales-trend") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(SalesTrendRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31",
                granularity = "invalid"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sales trend - missing time parameters returns 400`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales-trend") {
            bearerAuth("test-user-123")
            contentType(ContentType.Application.Json)
            setBody(SalesTrendRequest(
                granularity = "daily"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST sales trend - unauthorized request returns 401`() = testApplication {
        application { configureTestReportRoutes(mockReportService) }
        val client = createTestClient()

        val response = client.post("/api/reports/sales-trend") {
            contentType(ContentType.Application.Json)
            setBody(SalesTrendRequest(
                startTime = "2024-01-01",
                endTime = "2024-01-31",
                granularity = "daily"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
