package com.vibely.pos.shared.domain.currency.usecase

import com.vibely.pos.shared.domain.currency.repository.CurrencyRepository
import com.vibely.pos.shared.domain.result.Result

class DeleteExchangeRateUseCase(private val currencyRepository: CurrencyRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = currencyRepository.deleteExchangeRate(id)
}
