package com.vibely.pos.di

import androidx.compose.runtime.Composable
import com.vibely.pos.shared.di.sharedModules
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

/**
 * Wraps the Compose application with a Koin dependency injection context.
 *
 * Uses `KoinApplication` from koin-compose to provide a Koin instance
 * scoped to the Compose tree, enabling `koinInject()` and `koinViewModel()`
 * throughout the UI layer.
 *
 * @param platformModules Additional platform-specific Koin modules (e.g., Android context).
 * @param content The composable content of the application.
 */
@Composable
fun AppKoinContext(platformModules: List<Module> = emptyList(), content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(sharedModules() + uiModule + platformModules)
    }) {
        content()
    }
}
