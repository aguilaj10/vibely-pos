package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class GetAllCustomersUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(isActive: Boolean? = null, page: Int = 1, pageSize: Int = 50): Result<List<Customer>> =
        customerRepository.getAll(isActive, page, pageSize)
}
