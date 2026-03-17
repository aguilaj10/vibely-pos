package com.vibely.pos.backend.services

import com.vibely.pos.backend.common.DatabaseColumns
import com.vibely.pos.backend.common.TableNames
import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.data.currency.dto.CurrencyExchangeRateDTO
import com.vibely.pos.shared.domain.result.Result
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private const val ERROR_FETCH_CURRENCIES_FAILED = "Failed to fetch currencies"
private const val ERROR_FETCH_RATES_FAILED = "Failed to fetch exchange rates"
private const val ERROR_FETCH_RATE_FAILED = "Failed to fetch exchange rate"
private const val ERROR_CREATE_RATE_FAILED = "Failed to create exchange rate"
private const val ERROR_UPDATE_RATE_FAILED = "Failed to update exchange rate"
private const val ERROR_DELETE_RATE_FAILED = "Failed to delete exchange rate"

private fun buildExchangeRateData(
    currencyFrom: String,
    currencyTo: String,
    rate: Double,
    effectiveDate: String
): JsonObject {
    return buildJsonObject {
        put(DatabaseColumns.CURRENCY_CODE_FROM, currencyFrom)
        put(DatabaseColumns.CURRENCY_CODE_TO, currencyTo)
        put(DatabaseColumns.RATE, rate)
        put(DatabaseColumns.EFFECTIVE_DATE, effectiveDate)
    }
}

/**
 * Service for managing currencies and exchange rates.
 */
class CurrencyService(private val supabase: SupabaseClient) : BaseService() {

    /**
     * Retrieves all currencies from the database.
     */
    suspend fun getAllCurrencies(): Result<List<CurrencyDTO>> {
        return executeQuery(ERROR_FETCH_CURRENCIES_FAILED) {
            supabase.from(TableNames.CURRENCIES)
                .select()
                .decodeList<CurrencyDTO>()
        }
    }

    /**
     * Retrieves only active currencies.
     */
    suspend fun getActiveCurrencies(): Result<List<CurrencyDTO>> {
        return executeQuery(ERROR_FETCH_CURRENCIES_FAILED) {
            supabase.from(TableNames.CURRENCIES)
                .select {
                    filter {
                        eq(DatabaseColumns.IS_ACTIVE, true)
                    }
                }
                .decodeList<CurrencyDTO>()
        }
    }

    /**
     * Retrieves the exchange rate for a specific currency pair on a given date.
     * If no date is provided, uses today's date.
     */
    suspend fun getExchangeRate(
        fromCurrency: String,
        toCurrency: String,
        date: LocalDate? = null
    ): Result<CurrencyExchangeRateDTO?> {
        return executeQuery(ERROR_FETCH_RATE_FAILED) {
            val effectiveDate = date ?: LocalDate.now()

            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .select {
                    filter {
                        eq(DatabaseColumns.CURRENCY_CODE_FROM, fromCurrency)
                        eq(DatabaseColumns.CURRENCY_CODE_TO, toCurrency)
                        lte(DatabaseColumns.EFFECTIVE_DATE, effectiveDate.toString())
                    }
                    order(DatabaseColumns.EFFECTIVE_DATE, Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<CurrencyExchangeRateDTO>()
        }
    }

    /**
     * Retrieves the latest exchange rate for a specific currency pair.
     */
    suspend fun getLatestExchangeRate(
        fromCurrency: String,
        toCurrency: String
    ): Result<CurrencyExchangeRateDTO?> = getExchangeRate(fromCurrency, toCurrency, LocalDate.now())

    /**
     * Retrieves all exchange rates ordered by effective date descending.
     */
    suspend fun getAllExchangeRates(): Result<List<CurrencyExchangeRateDTO>> {
        return executeQuery(ERROR_FETCH_RATES_FAILED) {
            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .select {
                    order(DatabaseColumns.EFFECTIVE_DATE, Order.DESCENDING)
                }
                .decodeList<CurrencyExchangeRateDTO>()
        }
    }

    /**
     * Retrieves exchange rates for a specific currency pair within a date range.
     */
    suspend fun getExchangeRatesByDateRange(
        fromCurrency: String,
        toCurrency: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<CurrencyExchangeRateDTO>> {
        return executeQuery(ERROR_FETCH_RATES_FAILED) {
            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .select {
                    filter {
                        eq(DatabaseColumns.CURRENCY_CODE_FROM, fromCurrency)
                        eq(DatabaseColumns.CURRENCY_CODE_TO, toCurrency)
                        gte(DatabaseColumns.EFFECTIVE_DATE, startDate.toString())
                        lte(DatabaseColumns.EFFECTIVE_DATE, endDate.toString())
                    }
                    order(DatabaseColumns.EFFECTIVE_DATE, Order.DESCENDING)
                }
                .decodeList<CurrencyExchangeRateDTO>()
        }
    }

    /**
     * Creates a new exchange rate record.
     */
    suspend fun createExchangeRate(
        currencyFrom: String,
        currencyTo: String,
        rate: Double,
        effectiveDate: String
    ): Result<CurrencyExchangeRateDTO> {
        return executeQuery(ERROR_CREATE_RATE_FAILED) {
            val data = buildExchangeRateData(currencyFrom, currencyTo, rate, effectiveDate)

            val inserted: JsonObject = supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .insert(data) { select(Columns.list(DatabaseColumns.ID)) }
                .decodeSingle<JsonObject>()

            val id = inserted[DatabaseColumns.ID]?.jsonPrimitive?.content
                ?: error(ERROR_CREATE_RATE_FAILED)

            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .select { filter { eq(DatabaseColumns.ID, id) } }
                .decodeSingle<CurrencyExchangeRateDTO>()
        }
    }

    /**
     * Updates an existing exchange rate record.
     */
    suspend fun updateExchangeRate(
        id: String,
        rate: Double,
        effectiveDate: String
    ): Result<CurrencyExchangeRateDTO> {
        return executeQuery(ERROR_UPDATE_RATE_FAILED) {
            val data = buildJsonObject {
                put(DatabaseColumns.RATE, rate)
                put(DatabaseColumns.EFFECTIVE_DATE, effectiveDate)
            }

            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .update(data) { filter { eq(DatabaseColumns.ID, id) } }

            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .select { filter { eq(DatabaseColumns.ID, id) } }
                .decodeSingle<CurrencyExchangeRateDTO>()
        }
    }

    /**
     * Deletes an exchange rate record.
     */
    suspend fun deleteExchangeRate(id: String): Result<Unit> {
        return executeQuery(ERROR_DELETE_RATE_FAILED) {
            supabase.from(TableNames.CURRENCY_EXCHANGE_RATES)
                .delete { filter { eq(DatabaseColumns.ID, id) } }
        }
    }

    /**
     * Converts an amount from one currency to another using the exchange rate for the specified date.
     * Returns null if no exchange rate is found.
     *
     * Note: This is an internal helper that returns nullable Double for convenience in PurchaseOrderService.
     * External callers should use the Result-wrapped methods above.
     */
    suspend fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        date: LocalDate? = null
    ): Double? {
        if (fromCurrency == toCurrency) return amount

        return when (val result = getExchangeRate(fromCurrency, toCurrency, date)) {
            is Result.Success -> result.data?.let { amount * it.rate }
            is Result.Error -> null
        }
    }
}
