package com.vibely.pos.shared.di

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
        // Use cases will be registered here as they are implemented
        // Example:
        // singleOf(::GetProductsUseCase)
        // singleOf(::CreateOrderUseCase)
        // singleOf(::ProcessPaymentUseCase)
    }
