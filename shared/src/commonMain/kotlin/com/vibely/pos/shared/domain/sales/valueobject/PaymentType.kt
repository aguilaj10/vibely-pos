package com.vibely.pos.shared.domain.sales.valueobject

enum class PaymentType(val dbValue: String) {
    CASH("cash"),
    CREDIT_CARD("credit_card"),
    DEBIT_CARD("debit_card"),
    BANK_TRANSFER("bank_transfer"),
    ;

    companion object {
        fun fromDbValue(value: String): PaymentType = entries.find { it.dbValue == value.lowercase() } ?: CASH
    }
}
