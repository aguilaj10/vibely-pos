package com.vibely.pos.backend.dto.request

import kotlinx.serialization.Serializable

/**
 * Request data classes for Sale operations.
 */

/**
 * Request parameters for querying sales.
 *
 * @property page Page number (default: 1)
 * @property pageSize Items per page (default: 50)
 * @property startDate Filter by start date
 * @property endDate Filter by end date
 * @property status Filter by status
 */
@Serializable
data class GetAllSalesRequest(
    val page: Int = 1,
    val pageSize: Int = 50,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String? = null
)

/**
 * Request parameters for creating a sale.
 *
 * @property items List of sale items (required)
 * @property paymentMethod Payment method used
 * @property customerId Optional customer ID
 * @property notes Optional notes
 */
@Serializable
data class CreateSaleRequest(
    val items: List<SaleItemRequest>,
    val paymentMethod: String,
    val customerId: String? = null,
    val notes: String? = null
)

/**
 * Request parameters for a sale item.
 *
 * @property productId Product ID (required)
 * @property quantity Quantity (required)
 * @property unitPrice Unit price at time of sale
 */
@Serializable
data class SaleItemRequest(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double
)
