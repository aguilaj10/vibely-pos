package com.vibely.pos.shared.data.currency.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyExchangeRateDTO(
    @SerialName("id") val id: String,
    @SerialName("currency_code_from") val currencyCodeFrom: String,
    @SerialName("currency_code_to") val currencyCodeTo: String,
    @SerialName("rate") val rate: Double,
    @SerialName("effective_date") val effectiveDate: String,
    @SerialName("created_at") val createdAt: String? = null,
)
