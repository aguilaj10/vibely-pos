package com.vibely.pos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.pos.di.AppKoinContext
import com.vibely.pos.shared.util.NetworkMonitor
import com.vibely.pos.ui.navigation.AppNavigation
import com.vibely.pos.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.core.module.Module

private const val OFFLINE_BANNER_DESCRIPTION = "Offline status banner"

@Composable
fun App(platformModules: List<Module> = emptyList()) {
    AppKoinContext(platformModules = platformModules) {
        AppTheme {
            val networkMonitor: NetworkMonitor = koinInject()
            val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

            DisposableEffect(Unit) {
                networkMonitor.startMonitoring()
                onDispose {
                    networkMonitor.stopMonitoring()
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()

                    if (!isOnline) {
                        OfflineBanner(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics {
            contentDescription = OFFLINE_BANNER_DESCRIPTION
        },
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Text(
            text = "You're offline",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
