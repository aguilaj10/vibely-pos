package com.vibely.pos.backend.common

@Suppress("UndocumentedPublicProperty", "UndocumentedPublicClass")
object DatabaseColumns {
    const val ID = "id"
    const val USER_ID = "user_id"
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"
    const val IS_ACTIVE = "is_active"

    const val NAME = "name"
    const val FULL_NAME = "full_name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val CUSTOMER_CODE = "customer_code"
    const val LOYALTY_POINTS = "loyalty_points"
    const val LOYALTY_TIER = "loyalty_tier"
    const val TOTAL_PURCHASES = "total_purchases"
    const val SKU = "sku"
    const val BARCODE = "barcode"
    const val DESCRIPTION = "description"
    const val CATEGORY_ID = "category_id"
    const val SUPPLIER_ID = "supplier_id"
    const val CURRENT_STOCK = "current_stock"
    const val MIN_STOCK_LEVEL = "min_stock_level"
    const val MAX_STOCK_LEVEL = "max_stock_level"
    const val REORDER_POINT = "reorder_point"
    const val UNIT_OF_MEASURE = "unit_of_measure"
    const val UNIT_PRICE = "unit_price"
    const val COST_PRICE = "cost_price"
    const val TAX_RATE = "tax_rate"

    const val SALE_ID = "sale_id"
    const val SALE_DATE = "sale_date"
    const val STATUS = "status"

    const val PRODUCT_ID = "product_id"
    const val TRANSACTION_TYPE = "transaction_type"
    const val REFERENCE_TYPE = "reference_type"
    const val REFERENCE_ID = "reference_id"
    const val QUANTITY = "quantity"
    const val NOTES = "notes"

    const val COLOR = "color"
    const val ICON = "icon"

    const val TOKEN = "token"
    const val EXPIRES_AT = "expires_at"
    const val BLACKLISTED_AT = "blacklisted_at"

    const val CLOSED_AT = "closed_at"

    // Loyalty tier thresholds
    const val PLATINUM_THRESHOLD = 5000
    const val GOLD_THRESHOLD = 2000
    const val SILVER_THRESHOLD = 500
}
