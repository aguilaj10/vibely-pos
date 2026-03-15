package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class UpdateCustomerUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(customer: Customer): Result<Customer> = customerRepository.update(customer)
}
