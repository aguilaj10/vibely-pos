package com.vibely.pos.shared.domain.currency.usecase

import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate
import com.vibely.pos.shared.domain.currency.repository.CurrencyRepository
import com.vibely.pos.shared.domain.result.Result

class GetAllExchangeRatesUseCase(private val currencyRepository: CurrencyRepository) {
    suspend operator fun invoke(): Result<List<CurrencyExchangeRate>> = currencyRepository.getAllExchangeRates()
}
