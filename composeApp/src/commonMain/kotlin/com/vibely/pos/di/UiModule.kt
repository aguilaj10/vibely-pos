package com.vibely.pos.di

import com.vibely.pos.ui.auth.LoginViewModel
import com.vibely.pos.ui.checkout.CheckoutViewModel
import com.vibely.pos.ui.dashboard.DashboardViewModel
import com.vibely.pos.ui.sales.SalesListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for UI-specific ViewModels.
 *
 * This module is specific to the composeApp and registers
 * ViewModels that depend on UI layer components.
 */
val uiModule = module {
    // Auth ViewModels
    factoryOf(::LoginViewModel)

    // Dashboard ViewModels
    factoryOf(::DashboardViewModel)

    // Checkout ViewModels
    factoryOf(::CheckoutViewModel)

    // Sales ViewModels
    factoryOf(::SalesListViewModel)
}
