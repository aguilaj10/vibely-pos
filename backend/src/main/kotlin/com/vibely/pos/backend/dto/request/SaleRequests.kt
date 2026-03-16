package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
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
    @SerialName("page")
    val page: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 50,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("status")
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
    @SerialName("items")
    val items: List<SaleItemRequest>,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("customer_id")
    val customerId: String? = null,
    @SerialName("notes")
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
    @SerialName("product_id")
    val productId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double
)
