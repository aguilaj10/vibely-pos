package com.vibely.pos.shared.data.currency.repository

import com.vibely.pos.shared.data.common.BaseRepository
import com.vibely.pos.shared.data.currency.datasource.RemoteCurrencyDataSource
import com.vibely.pos.shared.data.currency.mapper.CurrencyMapper
import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate
import com.vibely.pos.shared.domain.currency.repository.CurrencyRepository
import com.vibely.pos.shared.domain.result.Result
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CurrencyRepositoryImpl(private val remoteDataSource: RemoteCurrencyDataSource) :
    BaseRepository(),
    CurrencyRepository {

    override suspend fun getAllExchangeRates(): Result<List<CurrencyExchangeRate>> = mapList(
        remoteDataSource.getAllExchangeRates(),
        CurrencyMapper::toDomain,
    )

    override suspend fun getExchangeRateById(id: String): Result<CurrencyExchangeRate> = mapSingle(
        remoteDataSource.getExchangeRateById(id),
        CurrencyMapper::toDomain,
    )

    override suspend fun createExchangeRate(exchangeRate: CurrencyExchangeRate): Result<CurrencyExchangeRate> {
        val json = buildJsonObject {
            put("currency_code_from", exchangeRate.currencyCodeFrom)
            put("currency_code_to", exchangeRate.currencyCodeTo)
            put("rate", exchangeRate.rate)
            put("effective_date", exchangeRate.effectiveDate)
        }
        return mapSingle(
            remoteDataSource.createExchangeRate(json),
            CurrencyMapper::toDomain,
        )
    }

    override suspend fun updateExchangeRate(exchangeRate: CurrencyExchangeRate): Result<CurrencyExchangeRate> {
        val json = buildJsonObject {
            put("currency_code_from", exchangeRate.currencyCodeFrom)
            put("currency_code_to", exchangeRate.currencyCodeTo)
            put("rate", exchangeRate.rate)
            put("effective_date", exchangeRate.effectiveDate)
        }
        return mapSingle(
            remoteDataSource.updateExchangeRate(exchangeRate.id, json),
            CurrencyMapper::toDomain,
        )
    }

    override suspend fun deleteExchangeRate(id: String): Result<Unit> = remoteDataSource.deleteExchangeRate(id)

    override suspend fun getActiveCurrencies(): Result<List<com.vibely.pos.shared.data.currency.dto.CurrencyDTO>> =
        remoteDataSource.getActiveCurrencies()
}
