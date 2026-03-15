package com.vibely.pos.shared.domain.customer.usecase

import com.vibely.pos.shared.domain.customer.repository.CustomerRepository
import com.vibely.pos.shared.domain.result.Result

class GetCustomerPurchaseHistoryUseCase(private val customerRepository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, page: Int = 1, pageSize: Int = 50): Result<List<Map<String, Any>>> =
        customerRepository.getPurchaseHistory(customerId, page, pageSize)
}
