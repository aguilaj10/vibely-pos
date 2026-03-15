package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class DeleteCustomerUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(customerId: String): Result<Unit> = customerRepository.delete(customerId)
}
