package com.vibely.pos.shared.di

import com.vibely.pos.shared.domain.auth.usecase.GetCurrentUserUseCase
import com.vibely.pos.shared.domain.auth.usecase.LoginUseCase
import com.vibely.pos.shared.domain.auth.usecase.LogoutUseCase
import com.vibely.pos.shared.domain.auth.usecase.RefreshTokenUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetDashboardSummaryUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetLowStockProductsUseCase
import com.vibely.pos.shared.domain.dashboard.usecase.GetRecentTransactionsUseCase
import com.vibely.pos.shared.domain.sales.usecase.AddToCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.CompleteSaleUseCase
import com.vibely.pos.shared.domain.sales.usecase.GetSalesUseCase
import com.vibely.pos.shared.domain.sales.usecase.RemoveFromCartUseCase
import com.vibely.pos.shared.domain.sales.usecase.SearchProductsUseCase
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
    }
