package com.vibely.pos.shared.domain.currency.usecase

import com.vibely.pos.shared.data.currency.dto.CurrencyDTO
import com.vibely.pos.shared.domain.currency.repository.CurrencyRepository
import com.vibely.pos.shared.domain.result.Result

class GetActiveCurrenciesUseCase(private val currencyRepository: CurrencyRepository) {
    suspend operator fun invoke(): Result<List<CurrencyDTO>> = currencyRepository.getActiveCurrencies()
}
