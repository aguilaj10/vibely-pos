package com.vibely.pos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vibely.pos.shared.domain.sales.valueobject.PaymentType
import com.vibely.pos.shared.domain.sales.valueobject.toDisplayString
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.checkout.PaymentTender
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Times
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.common_cancel
import vibely_pos.composeapp.generated.resources.payment_add
import vibely_pos.composeapp.generated.resources.payment_dialog_title
import vibely_pos.composeapp.generated.resources.payment_method_card
import vibely_pos.composeapp.generated.resources.payment_method_cash
import vibely_pos.composeapp.generated.resources.payment_method_transfer
import vibely_pos.composeapp.generated.resources.payment_processing
import vibely_pos.composeapp.generated.resources.payment_remaining
import vibely_pos.composeapp.generated.resources.payment_tenders
import vibely_pos.composeapp.generated.resources.payment_total_amount

@Composable
fun PaymentDialog(
    totalAmount: Double,
    paymentTenders: List<PaymentTender>,
    remainingAmount: Double,
    canComplete: Boolean,
    isProcessing: Boolean,
    onAddTender: (PaymentType, Double) -> Unit,
    onRemoveTender: (Int) -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<PaymentType?>(null) }
    var amountInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.payment_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.payment_total_amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(Res.string.payment_remaining),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = FormatUtils.formatCurrency(remainingAmount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingAmount > 0) AppColors.Warning else AppColors.Success,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.payment_add),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppButton(
                        text = stringResource(Res.string.payment_method_cash),
                        onClick = { selectedType = PaymentType.CASH },
                        style =
                        if (selectedType ==
                            PaymentType.CASH
                        ) {
                            AppButtonStyle.Primary
                        } else {
                            AppButtonStyle.Outlined
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                    )
                    AppButton(
                        text = stringResource(Res.string.payment_method_card),
                        onClick = { selectedType = PaymentType.CREDIT_CARD },
                        style =
                        if (selectedType ==
                            PaymentType.CREDIT_CARD
                        ) {
                            AppButtonStyle.Primary
                        } else {
                            AppButtonStyle.Outlined
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                    )
                    AppButton(
                        text = stringResource(Res.string.payment_method_transfer),
                        onClick = { selectedType = PaymentType.BANK_TRANSFER },
                        style =
                        if (selectedType ==
                            PaymentType.BANK_TRANSFER
                        ) {
                            AppButtonStyle.Primary
                        } else {
                            AppButtonStyle.Outlined
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        placeholder = "Amount",
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing,
                    )
                    AppButton(
                        text = stringResource(Res.string.payment_add),
                        onClick = {
                            val amount = amountInput.toDoubleOrNull()
                            if (amount != null && amount > 0 && selectedType != null) {
                                onAddTender(selectedType!!, amount)
                                amountInput = ""
                                selectedType = null
                            }
                        },
                        style = AppButtonStyle.Primary,
                        enabled =
                        !isProcessing &&
                            selectedType != null &&
                            amountInput.toDoubleOrNull()?.let { it > 0 } == true,
                    )
                }

                if (paymentTenders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(Res.string.payment_tenders),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    paymentTenders.forEachIndexed { index, tender ->
                        Row(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = tender.type.toDisplayString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(tender.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            IconButton(
                                onClick = { onRemoveTender(index) },
                                enabled = !isProcessing,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Times,
                                    contentDescription = "Remove",
                                    tint = AppColors.Error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.payment_processing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppButton(
                            text = stringResource(Res.string.common_cancel),
                            onClick = onDismiss,
                            style = AppButtonStyle.Text,
                            modifier = Modifier.weight(1f),
                        )
                        AppButton(
                            text = "Complete Sale",
                            onClick = onComplete,
                            style = AppButtonStyle.Primary,
                            modifier = Modifier.weight(1f),
                            enabled = canComplete,
                        )
                    }
                }
            }
        }
    }
}
