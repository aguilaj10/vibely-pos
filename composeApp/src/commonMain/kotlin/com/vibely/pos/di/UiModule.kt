package com.vibely.pos.di

import com.vibely.pos.ui.auth.LoginViewModel
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
}
