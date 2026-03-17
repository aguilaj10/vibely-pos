package com.vibely.pos.shared.domain.currency.entity

data class CurrencyExchangeRate(
    val id: String,
    val currencyCodeFrom: String,
    val currencyCodeTo: String,
    val rate: Double,
    val effectiveDate: String,
    val createdAt: String? = null,
) {
    val displayName: String
        get() = "$currencyCodeFrom → $currencyCodeTo"

    fun withRate(newRate: Double): CurrencyExchangeRate = copy(
        rate = newRate,
    )

    fun withEffectiveDate(newDate: String): CurrencyExchangeRate = copy(
        effectiveDate = newDate,
    )

    companion object {
        fun create(
            id: String,
            currencyCodeFrom: String,
            currencyCodeTo: String,
            rate: Double,
            effectiveDate: String,
            createdAt: String? = null,
        ): CurrencyExchangeRate = CurrencyExchangeRate(
            id = id,
            currencyCodeFrom = currencyCodeFrom,
            currencyCodeTo = currencyCodeTo,
            rate = rate,
            effectiveDate = effectiveDate,
            createdAt = createdAt,
        )
    }
}
