package com.vibely.pos.shared.domain.currency.repository

import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate
import com.vibely.pos.shared.domain.result.Result

interface CurrencyRepository {
    suspend fun getAllExchangeRates(): Result<List<CurrencyExchangeRate>>
    suspend fun getExchangeRateById(id: String): Result<CurrencyExchangeRate>
    suspend fun createExchangeRate(exchangeRate: CurrencyExchangeRate): Result<CurrencyExchangeRate>
    suspend fun updateExchangeRate(exchangeRate: CurrencyExchangeRate): Result<CurrencyExchangeRate>
    suspend fun deleteExchangeRate(id: String): Result<Unit>
    suspend fun getActiveCurrencies(): Result<List<CurrencyDTO>>
}
