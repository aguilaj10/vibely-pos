package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request data classes for Product operations.
 */

/**
 * Request body for creating a product.
 *
 * @property name Product name (required)
 * @property sku Optional SKU
 * @property barcode Optional barcode
 * @property description Optional description
 * @property categoryId Optional category ID
 * @property supplierId Optional supplier ID
 * @property unitPrice Unit price (required)
 * @property costPrice Optional cost price
 * @property costCurrencyCode Currency code for cost price (default: USD)
 * @property currentStock Initial stock quantity (required)
 * @property minStockLevel Optional minimum stock level
 * @property maxStockLevel Optional maximum stock level
 * @property reorderPoint Optional reorder point
 * @property unitOfMeasure Optional unit of measure
 * @property isActive Whether product is active (default: true)
 * @property taxRate Optional tax rate
 */
@Serializable
data class CreateProductRequest(
    @SerialName("name")
    val name: String,
    @SerialName("sku")
    val sku: String? = null,
    @SerialName("barcode")
    val barcode: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("supplier_id")
    val supplierId: String? = null,
    @SerialName("selling_price")
    val unitPrice: Double,
    @SerialName("cost_price")
    val costPrice: Double? = null,
    @SerialName("cost_currency_code")
    val costCurrencyCode: String? = null,
    @SerialName("current_stock")
    val currentStock: Int,
    @SerialName("min_stock_level")
    val minStockLevel: Int? = null,
    @SerialName("max_stock_level")
    val maxStockLevel: Int? = null,
    @SerialName("reorder_point")
    val reorderPoint: Int? = null,
    @SerialName("unit")
    val unitOfMeasure: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("tax_rate")
    val taxRate: Double? = null
)

/**
 * Request body for updating a product.
 *
 * @property name Optional new name
 * @property sku Optional new SKU
 * @property barcode Optional new barcode
 * @property description Optional new description
 * @property categoryId Optional new category ID
 * @property supplierId Optional new supplier ID
 * @property unitPrice Optional new unit price
 * @property costPrice Optional new cost price
 * @property costCurrencyCode Optional new cost currency code
 * @property minStockLevel Optional new minimum stock level
 * @property maxStockLevel Optional new maximum stock level
 * @property reorderPoint Optional new reorder point
 * @property unitOfMeasure Optional new unit of measure
 * @property isActive Optional new active status
 * @property taxRate Optional new tax rate
 */
@Serializable
data class UpdateProductRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("sku")
    val sku: String? = null,
    @SerialName("barcode")
    val barcode: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("supplier_id")
    val supplierId: String? = null,
    @SerialName("selling_price")
    val unitPrice: Double? = null,
    @SerialName("cost_price")
    val costPrice: Double? = null,
    @SerialName("cost_currency_code")
    val costCurrencyCode: String? = null,
    @SerialName("min_stock_level")
    val minStockLevel: Int? = null,
    @SerialName("max_stock_level")
    val maxStockLevel: Int? = null,
    @SerialName("reorder_point")
    val reorderPoint: Int? = null,
    @SerialName("unit")
    val unitOfMeasure: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null,
    @SerialName("tax_rate")
    val taxRate: Double? = null
)

/**
 * Request body for adjusting product stock.
 *
 * @property quantity Quantity to add (positive) or remove (negative)
 * @property transactionType Type of transaction (required)
 * @property referenceType Optional reference type
 * @property referenceId Optional reference ID
 * @property notes Optional notes
 */
@Serializable
data class AdjustStockRequest(
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("transaction_type")
    val transactionType: String,
    @SerialName("reference_type")
    val referenceType: String? = null,
    @SerialName("reference_id")
    val referenceId: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request parameters for querying products.
 *
 * @property page Page number (default: 1)
 * @property pageSize Items per page (default: 50)
 * @property categoryId Filter by category ID
 * @property supplierId Filter by supplier ID
 * @property isActive Filter by active status
 * @property lowStock Filter products below reorder point
 */
@Serializable
data class GetAllProductsRequest(
    @SerialName("page")
    val page: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 50,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("supplier_id")
    val supplierId: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null,
    @SerialName("low_stock")
    val lowStock: Boolean? = null
)
