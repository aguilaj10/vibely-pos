package com.vibely.pos.backend.common

@Suppress("UndocumentedPublicProperty", "UndocumentedPublicClass")
object ErrorKeys {
    const val ERROR = "error"
}

@Suppress("UndocumentedPublicProperty", "UndocumentedPublicClass")
object ErrorMessages {
    const val UNAUTHORIZED = "User not authenticated"
    const val MISSING_ID = "Missing ID"
    const val INVALID_CREDENTIALS = "Invalid email or password"
    const val INVALID_TOKEN = "Invalid token"
    const val INVALID_REFRESH = "Invalid or expired refresh token"
    const val USER_NOT_FOUND = "User not found"
    const val EMAIL_EXISTS = "Email already exists"
    const val INVALID_PASSWORD = "Invalid current password"
    const val CUSTOMER_NOT_FOUND = "Customer not found"
    const val PRODUCT_NOT_FOUND = "Product not found"
    const val CATEGORY_NOT_FOUND = "Category not found"
    const val SUPPLIER_NOT_FOUND = "Supplier not found"
    const val SHIFT_NOT_FOUND = "Shift not found"
    const val PURCHASE_ORDER_NOT_FOUND = "Purchase order not found"
    const val SALE_NOT_FOUND = "Sale not found"
}
