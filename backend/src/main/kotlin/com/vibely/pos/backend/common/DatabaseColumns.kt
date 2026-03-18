package com.vibely.pos.backend.common

@Suppress("UndocumentedPublicProperty", "UndocumentedPublicClass")
object DatabaseColumns {
    // Common columns (used across multiple tables)
    const val CREATED_AT = "created_at"
    const val ID = "id"
    const val IS_ACTIVE = "is_active"
    const val UPDATED_AT = "updated_at"
    const val USER_ID = "user_id"

    // Category columns
    const val CATEGORIES_NAME = "categories(name)"
    const val CATEGORY_ID = "category_id"
    const val COLOR = "color_hex"
    const val DESCRIPTION = "description"
    const val ICON = "icon_name"
    const val NAME = "name"

    // Customer columns
    const val CUSTOMER_CODE = "customer_code"
    const val CUSTOMER_ID = "customer_id"
    const val EMAIL = "email"
    const val FULL_NAME = "full_name"
    const val LOYALTY_POINTS = "loyalty_points"
    const val LOYALTY_TIER = "loyalty_tier"
    const val PHONE = "phone"
    const val TOTAL_PURCHASES = "total_purchases"

    // Currency Exchange Rate columns
    const val COST_CURRENCY_CODE = "cost_currency_code"
    const val CURRENCY_CODE_FROM = "currency_code_from"
    const val CURRENCY_CODE_TO = "currency_code_to"
    const val EFFECTIVE_DATE = "effective_date"
    const val RATE = "rate"

    // Inventory Transaction columns
    const val NOTES = "notes"
    const val PRODUCT_ID = "product_id"
    const val QUANTITY = "quantity"
    const val QUANTITY_AFTER = "quantity_after"
    const val REFERENCE_ID = "reference_id"
    const val REFERENCE_TYPE = "reference_type"
    const val TRANSACTION_TYPE = "transaction_type"

    // Product columns
    const val BARCODE = "barcode"
    const val COST_PRICE = "cost_price"
    const val CURRENT_STOCK = "current_stock"
    const val IMAGE_URL = "image_url"
    const val MAX_STOCK_LEVEL = "max_stock_level"
    const val MIN_STOCK_LEVEL = "min_stock_level"
    const val REORDER_POINT = "reorder_point"
    const val SELLING_PRICE = "selling_price"
    const val SKU = "sku"
    const val SUPPLIER_ID = "supplier_id"
    const val TAX_RATE = "tax_rate"
    const val UNIT = "unit"

    // Purchase Order columns
    const val CREATED_BY = "created_by"
    const val EXPECTED_DELIVERY_DATE = "expected_delivery_date"
    const val ORDER_DATE = "order_date"
    const val PO_NUMBER = "po_number"
    const val PURCHASE_ORDER_ID = "purchase_order_id"
    const val RECEIVED_DATE = "received_date"
    const val RECEIVED_QUANTITY = "received_quantity"
    const val SUBTOTAL = "subtotal"
    const val TOTAL_AMOUNT = "total_amount"
    const val UNIT_COST = "unit_cost"

    // Sale columns
    const val SALE_DATE = "sale_date"
    const val SALE_ID = "sale_id"
    const val STATUS = "status"

    // Payment columns
    const val AMOUNT = "amount"
    const val PAYMENT_DATE = "payment_date"
    const val PAYMENT_STATUS = "payment_status"
    const val PAYMENT_TYPE = "payment_type"
    const val REFERENCE_NUMBER = "reference_number"

    // Shift columns
    const val CASHIER_ID = "cashier_id"
    const val CLOSED_AT = "closed_at"
    const val CLOSING_BALANCE = "closing_balance"
    const val DISCREPANCY = "discrepancy"
    const val EXPECTED_BALANCE = "expected_balance"
    const val OPENED_AT = "opened_at"
    const val OPENING_BALANCE = "opening_balance"
    const val SHIFT_NUMBER = "shift_number"
    const val TOTAL_CARD = "total_card"
    const val TOTAL_CASH = "total_cash"
    const val TOTAL_OTHER = "total_other"
    const val TOTAL_SALES = "total_sales"

    // Supplier columns
    const val ADDRESS = "address"
    const val CODE = "code"
    const val CONTACT_PERSON = "contact_person"
    const val SUPPLIER_CODE = "supplier_code"

    // Token columns
    const val BLACKLISTED_AT = "blacklisted_at"
    const val EXPIRES_AT = "expires_at"
    const val TOKEN = "token"

    // User columns
    const val PASSWORD_HASH = "password_hash"
    const val ROLE = "role"

    // Loyalty tier thresholds
    const val GOLD_THRESHOLD = 2000
    const val PLATINUM_THRESHOLD = 5000
    const val SILVER_THRESHOLD = 500
}
