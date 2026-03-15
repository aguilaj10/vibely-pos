package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class CreateCustomerUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(customer: Customer): Result<Customer> = customerRepository.create(customer)
}
