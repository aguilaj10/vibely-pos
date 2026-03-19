@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "LongMethod", "TooManyFunctions")

package com.vibely.pos.ui.shifts

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.shift.entity.Shift
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.PaginationControls
import com.vibely.pos.ui.dialogs.CloseShiftDialog
import com.vibely.pos.ui.dialogs.OpenShiftDialog
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.utils.formatCurrency
import com.vibely.pos.ui.utils.formatDecimal
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Clock
import compose.icons.fontawesomeicons.solid.DollarSign
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.Stop
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.shifts_card_sales
import vibely_pos.composeapp.generated.resources.shifts_cash_sales
import vibely_pos.composeapp.generated.resources.shifts_close
import vibely_pos.composeapp.generated.resources.shifts_current
import vibely_pos.composeapp.generated.resources.shifts_no_shifts_found
import vibely_pos.composeapp.generated.resources.shifts_open
import vibely_pos.composeapp.generated.resources.shifts_opening_balance
import vibely_pos.composeapp.generated.resources.shifts_title
import kotlin.math.abs

@Composable
fun ShiftsScreen(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier, viewModel: ShiftsViewModel = koinInject()) {
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
            ShiftsHeader(
                hasOpenShift = state.currentShift != null,
                onOpenShift = viewModel::onOpenShift,
                onCloseShift = { state.currentShift?.id?.let { viewModel.onCloseShift(it) } },
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.currentShift?.let { shift ->
                CurrentShiftCard(shift = shift)
                Spacer(modifier = Modifier.height(16.dp))
            }

            KpiCardsRow(
                openShiftsCount = state.openShiftsCount,
                todaysSales = state.todaysSales,
                totalDiscrepancy = state.totalDiscrepancy,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
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
                ShiftsTable(
                    shifts = state.shifts,
                    state = state,
                    viewModel = viewModel,
                    onViewShift = viewModel::onViewShiftDetails,
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

        if (state.showOpenShiftDialog) {
            OpenShiftDialog(
                onSave = viewModel::onSaveOpenShift,
                onDismiss = viewModel::onDismissOpenShiftDialog,
            )
        }

        if (state.showCloseShiftDialog) {
            state.closingShift?.let { shift ->
                CloseShiftDialog(
                    shift = shift,
                    onSave = viewModel::onSaveCloseShift,
                    onDismiss = viewModel::onDismissCloseShiftDialog,
                )
            }
        }
    }
}

@Composable
private fun ShiftsHeader(hasOpenShift: Boolean, onOpenShift: () -> Unit, onCloseShift: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.shifts_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        if (hasOpenShift) {
            AppButton(
                text = stringResource(Res.string.shifts_close),
                onClick = onCloseShift,
                style = AppButtonStyle.Secondary,
                icon = {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        } else {
            AppButton(
                text = stringResource(Res.string.shifts_open),
                onClick = onOpenShift,
                style = AppButtonStyle.Primary,
                icon = {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Play,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun CurrentShiftCard(shift: Shift) {
    AppCard(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.shifts_current, shift.shiftNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StatusChip(isOpen = true)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ShiftInfoItem(label = "Opened", value = FormatUtils.formatDateTime(shift.openedAt))
                ShiftInfoItem(
                    label = stringResource(Res.string.shifts_opening_balance),
                    value = shift.openingBalance.formatCurrency(),
                )
                ShiftInfoItem(
                    label = stringResource(Res.string.shifts_cash_sales),
                    value = shift.totalCash.formatCurrency(),
                )
                ShiftInfoItem(
                    label = stringResource(Res.string.shifts_card_sales),
                    value = shift.totalCard.formatCurrency(),
                )
                ShiftInfoItem(label = "Total Sales", value = shift.totalSales.formatCurrency())
            }
        }
    }
}

@Composable
private fun ShiftInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun KpiCardsRow(openShiftsCount: Int, todaysSales: Double, totalDiscrepancy: Double) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.Clock,
            label = "Open Shifts",
            value = openShiftsCount.toString(),
            valueColor = if (openShiftsCount > 0) AppColors.Success else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.DollarSign,
            label = "Today's Sales",
            value = todaysSales.formatCurrency(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.ExclamationTriangle,
            label = "Total Discrepancy",
            value = totalDiscrepancy.formatCurrency(),
            valueColor = getDiscrepancyColor(totalDiscrepancy),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KpiCard(icon: ImageVector, label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ShiftsTable(
    shifts: List<Shift>,
    state: ShiftsState,
    viewModel: ShiftsViewModel,
    onViewShift: (String) -> Unit,
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

            if (shifts.isEmpty()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Clock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.shifts_no_shifts_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(shifts) { shift ->
                        TableRow(
                            shift = shift,
                            onView = { onViewShift(shift.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }

                PaginationControls(
                    paginationState = state.pagination,
                    onPreviousPage = viewModel::onPreviousPage,
                    onNextPage = viewModel::onNextPage,
                    modifier = Modifier.fillMaxWidth(),
                )
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
        TableHeaderCell("Shift #", modifier = Modifier.width(100.dp))
        TableHeaderCell("Cashier", modifier = Modifier.weight(1f))
        TableHeaderCell("Opened At", modifier = Modifier.width(120.dp))
        TableHeaderCell("Closed At", modifier = Modifier.width(120.dp))
        TableHeaderCell("Opening", modifier = Modifier.width(100.dp))
        TableHeaderCell("Closing", modifier = Modifier.width(100.dp))
        TableHeaderCell("Discrepancy", modifier = Modifier.width(100.dp))
        TableHeaderCell("Status", modifier = Modifier.width(80.dp))
        TableHeaderCell("Actions", modifier = Modifier.width(60.dp))
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
private fun TableRow(shift: Shift, onView: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(shift.shiftNumber, modifier = Modifier.width(100.dp))
        TableCell(shift.cashierName ?: "-", modifier = Modifier.weight(1f))
        TableCell(FormatUtils.formatDateTime(shift.openedAt), modifier = Modifier.width(120.dp))
        TableCell(shift.closedAt?.let { FormatUtils.formatDateTime(it) } ?: "-", modifier = Modifier.width(120.dp))
        TableCell(shift.openingBalance.formatCurrency(), modifier = Modifier.width(100.dp))
        TableCell(
            shift.closingBalance?.formatCurrency() ?: "-",
            modifier = Modifier.width(100.dp),
        )

        DiscrepancyCell(discrepancy = shift.discrepancy, modifier = Modifier.width(100.dp))

        StatusChip(isOpen = shift.isOpen, modifier = Modifier.width(80.dp))

        Row(
            modifier = Modifier.width(60.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onView,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Eye,
                    contentDescription = "View",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun DiscrepancyCell(discrepancy: Double?, modifier: Modifier = Modifier) {
    val displayValue = discrepancy?.formatDecimal() ?: "-"
    val color = discrepancy?.let { getDiscrepancyColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        text = displayValue,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier,
    )
}

@Composable
private fun StatusChip(isOpen: Boolean, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) =
        if (isOpen) {
            Triple(AppColors.Success, Color.White, "Open")
        } else {
            Triple(AppColors.NeutralLight400, AppColors.TextPrimaryLight, "Closed")
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun getDiscrepancyColor(discrepancy: Double): Color = when {
    abs(discrepancy) < 0.01 -> MaterialTheme.colorScheme.onSurfaceVariant
    discrepancy > 0 -> AppColors.Success
    else -> AppColors.ErrorDark
}
