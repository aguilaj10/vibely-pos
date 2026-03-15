package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

/**
 * Request data classes for Inventory operations.
 */

/**
 * Request parameters for querying inventory transactions.
 *
 * @property page Page number (default: 1)
 * @property pageSize Items per page (default: 50)
 * @property productId Filter by product ID
 * @property type Filter by transaction type
 * @property startDate Filter by start date (ISO-8601)
 * @property endDate Filter by end date (ISO-8601)
 */
@Serializable
data class GetTransactionsRequest(
    val page: Int = 1,
    val pageSize: Int = 50,
    val productId: String? = null,
    val type: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

/**
 * Request parameters for querying low stock products.
 *
 * @property page Page number (default: 1)
 * @property pageSize Items per page (default: 50)
 * @property categoryId Filter by category ID
 */
@Serializable
data class GetLowStockProductsRequest(
    val page: Int = 1,
    val pageSize: Int = 50,
    val categoryId: String? = null
)
