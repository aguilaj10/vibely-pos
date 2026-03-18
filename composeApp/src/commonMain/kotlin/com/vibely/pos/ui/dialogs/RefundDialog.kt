package com.vibely.pos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.vibely.pos.shared.domain.sales.entity.Sale
import com.vibely.pos.shared.util.FormatUtils.formatCurrency
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.TimesCircle

/**
 * Refund confirmation dialog with reason input.
 * Displays sale information and warns about restocking products.
 *
 * @param sale The sale to refund
 * @param itemsCount Number of items in the sale
 * @param onConfirm Callback with reason when refund is confirmed
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun RefundDialog(sale: Sale, itemsCount: Int, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.TimesCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AppColors.Warning,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Process Refund",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                SaleInfoCard(sale, itemsCount)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "This will restore product stock to inventory.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Warning,
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Reason (optional)",
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    AppButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = AppButtonStyle.Text,
                        enabled = !isProcessing,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    AppButton(
                        text = "Process Refund",
                        onClick = {
                            isProcessing = true
                            onConfirm(reason)
                        },
                        style = AppButtonStyle.Destructive,
                        enabled = !isProcessing,
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleInfoCard(sale: Sale, itemsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Invoice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = sale.invoiceNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatCurrency(sale.totalAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = itemsCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
