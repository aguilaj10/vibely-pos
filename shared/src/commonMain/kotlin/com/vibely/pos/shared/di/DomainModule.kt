package com.vibely.pos.shared.di

import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.auth.usecase.LoginUseCase
import com.vibely.pos.shared.domain.auth.usecase.LogoutUseCase
import com.vibely.pos.shared.domain.auth.usecase.RefreshTokenUseCase
import com.vibely.pos.shared.domain.customer.usecase.AddLoyaltyPointsUseCase
import com.vibely.pos.shared.domain.customer.usecase.CreateCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.DeleteCustomerUseCase
import com.vibely.pos.shared.domain.customer.usecase.GetAllCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.GetCustomerPurchaseHistoryUseCase
import com.vibely.pos.shared.domain.customer.usecase.SearchCustomersUseCase
import com.vibely.pos.shared.domain.customer.usecase.UpdateCustomerUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetDashboardSummaryUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetLowStockProductsUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetRecentTransactionsUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetAllProductsUseCase
import com.vibely.pos.shared.domain.inventory.usecase.GetCategoriesUseCase
import com.vibely.pos.shared.domain.sales.usecase.AddToCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.CompleteSaleUseCase
import com.vibely.pos.shared.domain.sales.usecase.GetSalesUseCase
import com.vibely.pos.shared.domain.sales.usecase.RemoveFromCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
import com.vibely.pos.shared.domain.supplier.usecase.CreateSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.DeleteSupplierUseCase
import com.vibely.pos.shared.domain.supplier.usecase.GetAllSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.SearchSuppliersUseCase
import com.vibely.pos.shared.domain.supplier.usecase.UpdateSupplierUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for the Domain layer.
 *
 * Registers use cases and repository interfaces.
 * Repository interfaces are defined here but their implementations
 * are provided by [dataModule].
 *
 * As use cases and repository interfaces are created, register them here:
 * ```
 * val domainModule = module {
 *     singleOf(::GetProductsUseCase)
 *     singleOf(::CreateOrderUseCase)
 * }
 * ```
 */
val domainModule =
    module {
        // Auth use cases
        singleOf(::LoginUseCase)
        singleOf(::LogoutUseCase)
        singleOf(::GetCurrentUserUseCase)
        singleOf(::RefreshTokenUseCase)

        // Dashboard use cases
        singleOf(::GetDashboardSummaryUseCase)
        singleOf(::GetRecentTransactionsUseCase)
        singleOf(::GetLowStockProductsUseCase)

        // Sales use cases
        singleOf(::SearchProductsUseCase)
        singleOf(::AddToCartUseCase)
        singleOf(::RemoveFromCartUseCase)
        singleOf(::CompleteSaleUseCase)
        singleOf(::GetSalesUseCase)

        // Inventory use cases
        singleOf(::GetAllProductsUseCase)
        singleOf(::GetCategoriesUseCase)

        // Customer use cases
        singleOf(::GetAllCustomersUseCase)
        singleOf(::CreateCustomerUseCase)
        singleOf(::UpdateCustomerUseCase)
        singleOf(::DeleteCustomerUseCase)
        singleOf(::SearchCustomersUseCase)
        singleOf(::GetCustomerPurchaseHistoryUseCase)
        singleOf(::AddLoyaltyPointsUseCase)

        // Supplier use cases
        singleOf(::GetAllSuppliersUseCase)
        singleOf(::CreateSupplierUseCase)
        singleOf(::UpdateSupplierUseCase)
        singleOf(::DeleteSupplierUseCase)
        singleOf(::SearchSuppliersUseCase)
    }
