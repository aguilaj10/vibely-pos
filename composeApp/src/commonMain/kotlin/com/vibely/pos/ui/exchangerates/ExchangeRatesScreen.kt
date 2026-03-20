package com.vibely.pos.ui.exchangerates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.currency.entity.CurrencyExchangeRate
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.dialogs.ConfirmationDialog
import com.vibely.pos.ui.dialogs.ExchangeRateFormData
import com.vibely.pos.ui.dialogs.ExchangeRateFormDialog
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatDecimal
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.MoneyBillWave
import compose.icons.fontawesomeicons.solid.Plus
import compose.icons.fontawesomeicons.solid.Trash
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_delete
import vibely_pos.composeapp.generated.resources.common_edit
import vibely_pos.composeapp.generated.resources.exchange_rates_add
import vibely_pos.composeapp.generated.resources.exchange_rates_no_rates_found
import vibely_pos.composeapp.generated.resources.exchange_rates_title

@Composable
fun ExchangeRatesScreen(modifier: Modifier = Modifier, viewModel: ExchangeRatesViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSuccessMessageDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ExchangeRatesHeader(
                onAddExchangeRate = viewModel::onAddExchangeRate,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading && state.exchangeRates.isEmpty()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ExchangeRatesTable(
                    exchangeRates = state.exchangeRates,
                    onEditExchangeRate = viewModel::onEditExchangeRate,
                    onDeleteExchangeRate = viewModel::onDeleteExchangeRate,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (state.showExchangeRateDialog) {
            val editingExchangeRate = state.editingExchangeRate
            ExchangeRateFormDialog(
                isEdit = editingExchangeRate != null,
                currencies = state.currencies,
                initialData =
                editingExchangeRate?.let {
                    ExchangeRateFormData(
                        id = it.id,
                        currencyFrom = it.currencyCodeFrom,
                        currencyTo = it.currencyCodeTo,
                        rate = it.rate,
                        effectiveDate = it.effectiveDate,
                    )
                },
                onSave = viewModel::onSaveExchangeRate,
                onDismiss = viewModel::onDismissExchangeRateDialog,
            )
        }

        if (state.showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete Exchange Rate",
                message = "Are you sure you want to delete this exchange rate? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::onConfirmDelete,
                onDismiss = viewModel::onDismissDeleteDialog,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun ExchangeRatesHeader(onAddExchangeRate: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.exchange_rates_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        AppButton(
            text = stringResource(Res.string.exchange_rates_add),
            onClick = onAddExchangeRate,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun ExchangeRatesTable(
    exchangeRates: List<CurrencyExchangeRate>,
    onEditExchangeRate: (String) -> Unit,
    onDeleteExchangeRate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.padding(horizontal = 16.dp),
        style = AppCardStyle.Elevated,
        elevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TableHeader()

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            if (exchangeRates.isEmpty()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.MoneyBillWave,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.exchange_rates_no_rates_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(exchangeRates) { exchangeRate ->
                        TableRow(
                            exchangeRate = exchangeRate,
                            onEdit = { onEditExchangeRate(exchangeRate.id) },
                            onDelete = { onDeleteExchangeRate(exchangeRate.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell("From Currency", modifier = Modifier.weight(1f))
        TableHeaderCell("To Currency", modifier = Modifier.weight(1f))
        TableHeaderCell("Rate", modifier = Modifier.weight(1f))
        TableHeaderCell("Effective Date", modifier = Modifier.weight(1f))
        TableHeaderCell("Actions", modifier = Modifier.width(100.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun TableRow(exchangeRate: CurrencyExchangeRate, onEdit: () -> Unit, onDelete: () -> Unit) {
    val formattedRate =
        remember(exchangeRate.rate) {
            // Format with 4 decimal places for exchange rates
            exchangeRate.rate.formatDecimal(4)
        }

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(exchangeRate.currencyCodeFrom, modifier = Modifier.weight(1f))
        TableCell(exchangeRate.currencyCodeTo, modifier = Modifier.weight(1f))
        TableCell(formattedRate, modifier = Modifier.weight(1f))
        TableCell(exchangeRate.effectiveDate, modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.width(100.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Edit,
                    contentDescription = stringResource(Res.string.common_edit),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Trash,
                    contentDescription = stringResource(Res.string.common_delete),
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.ErrorDark,
                )
            }
        }
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}
