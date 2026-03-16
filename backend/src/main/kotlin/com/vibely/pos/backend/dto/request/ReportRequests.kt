package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

/**
 * Request parameters for time period-based reports.
 *
 * @property startTime Start of the time period in ISO 8601 format
 * @property endTime End of the time period in ISO 8601 format
 */
@Serializable
data class TimePeriodRequest(
    val startTime: String? = null,
    val endTime: String? = null,
)

/**
 * Request parameters for top products report.
 *
 * @property startTime Start of the time period in ISO 8601 format
 * @property endTime End of the time period in ISO 8601 format
 * @property limit Maximum number of products to return
 */
@Serializable
data class TopProductsRequest(
    val startTime: String? = null,
    val endTime: String? = null,
    val limit: Int = 10,
)

/**
 * Request parameters for category breakdown report.
 *
 * @property startTime Start of the time period in ISO 8601 format
 * @property endTime End of the time period in ISO 8601 format
 */
@Serializable
data class CategoryBreakdownRequest(
    val startTime: String? = null,
    val endTime: String? = null,
)

/**
 * Request parameters for customer analytics report.
 *
 * @property startTime Start of the time period in ISO 8601 format
 * @property endTime End of the time period in ISO 8601 format
 * @property limit Maximum number of customers to return
 */
@Serializable
data class CustomerAnalyticsRequest(
    val startTime: String? = null,
    val endTime: String? = null,
    val limit: Int = 10,
)

/**
 * Request parameters for sales trend report.
 *
 * @property startTime Start of the time period in ISO 8601 format
 * @property endTime End of the time period in ISO 8601 format
 * @property granularity Time bucket size: "daily", "weekly", "monthly", or "yearly"
 */
@Serializable
data class SalesTrendRequest(
    val startTime: String? = null,
    val endTime: String? = null,
    val granularity: String? = null,
)
