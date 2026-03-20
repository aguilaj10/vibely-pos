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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.settings_configure_store
import vibely_pos.composeapp.generated.resources.settings_error
import vibely_pos.composeapp.generated.resources.settings_loading
import vibely_pos.composeapp.generated.resources.settings_no_settings
import vibely_pos.composeapp.generated.resources.settings_preferences
import vibely_pos.composeapp.generated.resources.settings_receipt
import vibely_pos.composeapp.generated.resources.settings_retry
import vibely_pos.composeapp.generated.resources.settings_save
import vibely_pos.composeapp.generated.resources.settings_store_info
import vibely_pos.composeapp.generated.resources.settings_tax_currency
import vibely_pos.composeapp.generated.resources.settings_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is SettingsUiState.Success && (state as SettingsUiState.Success).saveSuccess) {
            viewModel.clearSaveStatus()
        }
    }

    SettingsScreenContent(
        state = state,
        onUpdateStoreInfo = viewModel::updateStoreInfo,
        onUpdateReceiptSettings = viewModel::updateReceiptSettings,
        onUpdateTaxSettings = viewModel::updateTaxSettings,
        onUpdateUserPreferences = viewModel::updateUserPreferences,
        onLoadSettings = viewModel::loadSettings,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onUpdateStoreInfo: (String, String, String, String) -> Unit,
    onUpdateReceiptSettings: (String, String, String?, Boolean) -> Unit,
    onUpdateTaxSettings: (Double, String) -> Unit,
    onUpdateUserPreferences: (String, String, Boolean, kotlin.time.Duration) -> Unit,
    onLoadSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs =
        listOf(
            TabItem(stringResource(Res.string.settings_store_info), FontAwesomeIcons.Solid.Store),
            TabItem(stringResource(Res.string.settings_receipt), FontAwesomeIcons.Solid.Receipt),
            TabItem(stringResource(Res.string.settings_tax_currency), FontAwesomeIcons.Solid.Cog),
            TabItem(stringResource(Res.string.settings_preferences), FontAwesomeIcons.Solid.UserCog),
        )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.settings_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    val successState = state as? SettingsUiState.Success
                    if (successState?.isSaving == true) {
                        CircularProgressIndicator(
                            modifier =
                            Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        AppButton(
                            text = stringResource(Res.string.settings_save),
                            onClick = {
                                when (selectedTabIndex) {
                                    0 -> {
                                        successState?.storeSettings?.let { store ->
                                            onUpdateStoreInfo(
                                                store.storeName,
                                                store.address,
                                                store.phone,
                                                store.email,
                                            )
                                        }
                                    }

                                    1 -> {
                                        successState?.receiptSettings?.let { receipt ->
                                            onUpdateReceiptSettings(
                                                receipt.header,
                                                receipt.footer,
                                                receipt.logoUrl,
                                                receipt.showTax,
                                            )
                                        }
                                    }

                                    2 -> {
                                        successState?.taxSettings?.let { tax ->
                                            onUpdateTaxSettings(
                                                tax.taxRate,
                                                tax.currency,
                                            )
                                        }
                                    }

                                    3 -> {
                                        successState?.userPreferences?.let { prefs ->
                                            onUpdateUserPreferences(
                                                prefs.language,
                                                prefs.theme,
                                                prefs.enableNotifications,
                                                prefs.autoLogoutTimeout,
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
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PrimaryTabRow(
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
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                when (state) {
                    is SettingsUiState.Loading -> {
                        LoadingState()
                    }

                    is SettingsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = onLoadSettings,
                        )
                    }

                    is SettingsUiState.Success -> {
                        if (state.isLoading) {
                            LoadingState()
                        } else if (state.storeSettings == null) {
                            EmptyState(
                                icon = FontAwesomeIcons.Solid.Cog,
                                title = stringResource(Res.string.settings_no_settings),
                                description = stringResource(Res.string.settings_configure_store),
                                size = EmptyStateSize.Large,
                                action = {
                                    AppButton(
                                        text = stringResource(Res.string.settings_retry),
                                        onClick = onLoadSettings,
                                        style = AppButtonStyle.Primary,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            when (selectedTabIndex) {
                                0 -> {
                                    StoreInfoTab(
                                        storeSettings = state.storeSettings,
                                        onStoreInfoChange = onUpdateStoreInfo,
                                        isSaving = state.isSaving,
                                    )
                                }

                                1 -> {
                                    ReceiptConfigTab(
                                        receiptSettings = state.receiptSettings,
                                        onReceiptSettingsChange = onUpdateReceiptSettings,
                                        isSaving = state.isSaving,
                                    )
                                }

                                2 -> {
                                    TaxCurrencyTab(
                                        taxSettings = state.taxSettings,
                                        onTaxSettingsChange = onUpdateTaxSettings,
                                        isSaving = state.isSaving,
                                    )
                                }

                                3 -> {
                                    UserPreferencesTab(
                                        userPreferences = state.userPreferences,
                                        onPreferencesChange = onUpdateUserPreferences,
                                        isSaving = state.isSaving,
                                    )
                                }
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
                text = stringResource(Res.string.settings_loading),
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
        title = stringResource(Res.string.settings_error),
        description = message,
        size = EmptyStateSize.Large,
        action = {
            AppButton(
                text = stringResource(Res.string.settings_retry),
                onClick = onRetry,
                style = AppButtonStyle.Primary,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

private data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
