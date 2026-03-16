package com.vibely.pos.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.EmptyState
import com.vibely.pos.ui.components.EmptyStateSize
import com.vibely.pos.ui.settings.components.ReceiptConfigTab
import com.vibely.pos.ui.settings.components.StoreInfoTab
import com.vibely.pos.ui.settings.components.TaxCurrencyTab
import com.vibely.pos.ui.settings.components.UserPreferencesTab
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Cog
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.Receipt
import compose.icons.fontawesomeicons.solid.Store
import compose.icons.fontawesomeicons.solid.UserCog
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("Store Info", FontAwesomeIcons.Solid.Store),
        TabItem("Receipt", FontAwesomeIcons.Solid.Receipt),
        TabItem("Tax & Currency", FontAwesomeIcons.Solid.Cog),
        TabItem("Preferences", FontAwesomeIcons.Solid.UserCog),
    )

    LaunchedEffect(state) {
        if (state is SettingsUiState.Success && (state as SettingsUiState.Success).saveSuccess) {
            viewModel.clearSaveStatus()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    val successState = state as? SettingsUiState.Success
                    if (successState?.isSaving == true) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        AppButton(
                            text = "Save",
                            onClick = {
                                when (selectedTabIndex) {
                                    0 -> {
                                        successState?.storeSettings?.let { store ->
                                            viewModel.updateStoreInfo(
                                                storeName = store.storeName,
                                                address = store.address,
                                                phone = store.phone,
                                                email = store.email,
                                            )
                                        }
                                    }
                                    1 -> {
                                        successState?.receiptSettings?.let { receipt ->
                                            viewModel.updateReceiptSettings(
                                                header = receipt.header,
                                                footer = receipt.footer,
                                                logoUrl = receipt.logoUrl,
                                                showTax = receipt.showTax,
                                            )
                                        }
                                    }
                                    2 -> {
                                        successState?.taxSettings?.let { tax ->
                                            viewModel.updateTaxSettings(
                                                taxRate = tax.taxRate,
                                                currency = tax.currency,
                                            )
                                        }
                                    }
                                    3 -> {
                                        successState?.userPreferences?.let { prefs ->
                                            viewModel.updateUserPreferences(
                                                language = prefs.language,
                                                theme = prefs.theme,
                                                enableNotifications = prefs.enableNotifications,
                                                autoLogoutTimeout = prefs.autoLogoutTimeout,
                                            )
                                        }
                                    }
                                }
                            },
                            style = AppButtonStyle.Primary,
                            enabled = successState?.isSaving != true && successState?.storeSettings != null,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when (val currentState = state) {
                    is SettingsUiState.Loading -> {
                        LoadingState()
                    }
                    is SettingsUiState.Error -> {
                        ErrorState(
                            message = currentState.message,
                            onRetry = { viewModel.loadSettings() },
                        )
                    }
                    is SettingsUiState.Success -> {
                        if (currentState.isLoading) {
                            LoadingState()
                        } else if (currentState.storeSettings == null) {
                            EmptyState(
                                icon = FontAwesomeIcons.Solid.Cog,
                                title = "No settings found",
                                description = "Configure your store settings to get started",
                                size = EmptyStateSize.Large,
                                action = {
                                    AppButton(
                                        text = "Retry",
                                        onClick = { viewModel.loadSettings() },
                                        style = AppButtonStyle.Primary,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            when (selectedTabIndex) {
                                0 -> StoreInfoTab(
                                    storeSettings = currentState.storeSettings,
                                    onStoreInfoChange = { name, address, phone, email ->
                                        viewModel.updateStoreInfo(name, address, phone, email)
                                    },
                                    isSaving = currentState.isSaving,
                                )
                                1 -> ReceiptConfigTab(
                                    receiptSettings = currentState.receiptSettings,
                                    onReceiptSettingsChange = { header, footer, logoUrl, showTax ->
                                        viewModel.updateReceiptSettings(header, footer, logoUrl, showTax)
                                    },
                                    isSaving = currentState.isSaving,
                                )
                                2 -> TaxCurrencyTab(
                                    taxSettings = currentState.taxSettings,
                                    onTaxSettingsChange = { taxRate, currency ->
                                        viewModel.updateTaxSettings(taxRate, currency)
                                    },
                                    isSaving = currentState.isSaving,
                                )
                                3 -> UserPreferencesTab(
                                    userPreferences = currentState.userPreferences,
                                    onPreferencesChange = { language, theme, notifications, timeout ->
                                        viewModel.updateUserPreferences(language, theme, notifications, timeout)
                                    },
                                    isSaving = currentState.isSaving,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading settings...",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondaryLight,
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    EmptyState(
        icon = FontAwesomeIcons.Solid.ExclamationCircle,
        title = "Failed to load settings",
        description = message,
        size = EmptyStateSize.Large,
        action = {
            AppButton(
                text = "Retry",
                onClick = onRetry,
                style = AppButtonStyle.Primary,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

private data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
