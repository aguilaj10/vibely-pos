package com.vibely.pos.shared.di

import org.koin.dsl.module

/**
 * Koin module for the Presentation layer.
 *
 * Registers ViewModels used by the Compose UI layer.
 * ViewModels are scoped as factories so each screen gets a fresh instance.
 *
 * As ViewModels are implemented, register them here:
 * ```
 * val presentationModule = module {
 *     viewModelOf(::ProductListViewModel)
 *     viewModelOf(::OrderViewModel)
 *     viewModelOf(::LoginViewModel)
 * }
 * ```
 */
val presentationModule =
    module {
        // ViewModels will be registered here as they are implemented
        // Example:
        // viewModelOf(::ProductListViewModel)
        // viewModelOf(::OrderViewModel)
        // viewModelOf(::LoginViewModel)
        // viewModelOf(::SettingsViewModel)
    }
