package com.vibely.pos.backend.dto.request

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
    val name: String,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val supplierId: String? = null,
    val unitPrice: Double,
    val costPrice: Double? = null,
    val currentStock: Int,
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val unitOfMeasure: String? = null,
    val isActive: Boolean = true,
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
 * @property minStockLevel Optional new minimum stock level
 * @property maxStockLevel Optional new maximum stock level
 * @property reorderPoint Optional new reorder point
 * @property unitOfMeasure Optional new unit of measure
 * @property isActive Optional new active status
 * @property taxRate Optional new tax rate
 */
@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val supplierId: String? = null,
    val unitPrice: Double? = null,
    val costPrice: Double? = null,
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val unitOfMeasure: String? = null,
    val isActive: Boolean? = null,
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
    val quantity: Int,
    val transactionType: String,
    val referenceType: String? = null,
    val referenceId: String? = null,
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
    val page: Int = 1,
    val pageSize: Int = 50,
    val categoryId: String? = null,
    val supplierId: String? = null,
    val isActive: Boolean? = null,
    val lowStock: Boolean? = null
)
