package com.vibely.pos.shared.data.currency.mapper

import com.vibely.pos.shared.data.currency.dto.CurrencyExchangeRateDTO
import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate

object CurrencyMapper {
    fun toDomain(dto: CurrencyExchangeRateDTO): CurrencyExchangeRate = CurrencyExchangeRate.create(
        id = dto.id,
        currencyCodeFrom = dto.currencyCodeFrom,
        currencyCodeTo = dto.currencyCodeTo,
        rate = dto.rate,
        effectiveDate = dto.effectiveDate,
        createdAt = dto.createdAt,
    )

    fun toDTO(exchangeRate: CurrencyExchangeRate): CurrencyExchangeRateDTO = CurrencyExchangeRateDTO(
        id = exchangeRate.id,
        currencyCodeFrom = exchangeRate.currencyCodeFrom,
        currencyCodeTo = exchangeRate.currencyCodeTo,
        rate = exchangeRate.rate,
        effectiveDate = exchangeRate.effectiveDate,
        createdAt = exchangeRate.createdAt,
    )
}
