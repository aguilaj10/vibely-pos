package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class AddLoyaltyPointsUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, points: Int): Result<Customer> = customerRepository.addLoyaltyPoints(customerId, points)
}
