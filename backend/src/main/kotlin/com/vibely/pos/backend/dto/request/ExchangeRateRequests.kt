package com.vibely.pos.backend.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request to create a new currency exchange rate.
 *
 * @property currencyFrom Source currency code (e.g., "USD")
 * @property currencyTo Target currency code (e.g., "MXN")
 * @property rate Exchange rate from source to target currency
 * @property effectiveDate Date when this rate becomes effective (ISO 8601 format)
 */
@Serializable
data class CreateExchangeRateRequest(
    @SerialName("currency_code_from") val currencyFrom: String,
    @SerialName("currency_code_to") val currencyTo: String,
    @SerialName("rate") val rate: Double,
    @SerialName("effective_date") val effectiveDate: String
)

/**
 * Request to update an existing currency exchange rate.
 *
 * @property rate New exchange rate value
 * @property effectiveDate New effective date (ISO 8601 format)
 */
@Serializable
data class UpdateExchangeRateRequest(
    @SerialName("rate") val rate: Double,
    @SerialName("effective_date") val effectiveDate: String
)

/**
 * Response containing currency conversion result.
 *
 * @property originalAmount Amount before conversion
 * @property fromCurrency Source currency code
 * @property toCurrency Target currency code
 * @property convertedAmount Amount after conversion
 * @property date Date of the exchange rate used for conversion
 */
@Serializable
data class ConvertAmountResponse(
    @SerialName("original_amount") val originalAmount: Double,
    @SerialName("from_currency") val fromCurrency: String,
    @SerialName("to_currency") val toCurrency: String,
    @SerialName("converted_amount") val convertedAmount: Double,
    @SerialName("date") val date: String
)
