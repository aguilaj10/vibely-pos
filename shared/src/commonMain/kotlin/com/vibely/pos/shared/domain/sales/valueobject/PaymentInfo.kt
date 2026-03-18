package com.vibely.pos.shared.domain.sales.valueobject

data class PaymentInfo(val type: PaymentType, val amount: Double, val reference: String = "") {
    init {
        require(amount > 0) { "Payment amount must be positive" }
    }
}
