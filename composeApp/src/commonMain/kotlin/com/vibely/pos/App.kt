package com.vibely.pos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vibely.pos.di.AppKoinContext
import com.vibely.pos.ui.navigation.AppNavigation
import com.vibely.pos.ui.theme.AppTheme
import org.koin.core.module.Module

/**
 * Root composable for the Vibely POS application.
 *
 * Wraps the app content with Koin DI and the AppTheme.
 *
 * @param platformModules Optional platform-specific Koin modules.
 */
@Composable
fun App(platformModules: List<Module> = emptyList()) {
    AppKoinContext(platformModules = platformModules) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavigation()
            }
        }
    }
}
