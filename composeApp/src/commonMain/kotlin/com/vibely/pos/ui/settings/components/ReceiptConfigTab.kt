package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Image
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.receipt_config_title
import vibely_pos.composeapp.generated.resources.receipt_configure_description
import vibely_pos.composeapp.generated.resources.receipt_footer_label
import vibely_pos.composeapp.generated.resources.receipt_footer_placeholder
import vibely_pos.composeapp.generated.resources.receipt_header_label
import vibely_pos.composeapp.generated.resources.receipt_header_placeholder
import vibely_pos.composeapp.generated.resources.receipt_logo_url_label
import vibely_pos.composeapp.generated.resources.receipt_preview_item_1
import vibely_pos.composeapp.generated.resources.receipt_preview_item_2
import vibely_pos.composeapp.generated.resources.receipt_preview_store_name
import vibely_pos.composeapp.generated.resources.receipt_preview_subtotal
import vibely_pos.composeapp.generated.resources.receipt_preview_tax_label
import vibely_pos.composeapp.generated.resources.receipt_preview_title
import vibely_pos.composeapp.generated.resources.receipt_preview_total_label
import vibely_pos.composeapp.generated.resources.receipt_sample_logo_placeholder
import vibely_pos.composeapp.generated.resources.receipt_sample_please_come_again
import vibely_pos.composeapp.generated.resources.receipt_sample_thank_you
import vibely_pos.composeapp.generated.resources.receipt_show_tax_description
import vibely_pos.composeapp.generated.resources.receipt_show_tax_label

@Composable
fun ReceiptConfigTab(
    receiptSettings: com.vibely.pos.shared.domain.settings.entity.ReceiptSettings?,
    onReceiptSettingsChange: (String, String, String?, Boolean) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    var header by remember(receiptSettings) { mutableStateOf(receiptSettings?.header ?: "") }
    var footer by remember(receiptSettings) { mutableStateOf(receiptSettings?.footer ?: "") }
    var logoUrl by remember(receiptSettings) { mutableStateOf(receiptSettings?.logoUrl ?: "") }
    var showTax by remember(receiptSettings) { mutableStateOf(receiptSettings?.showTax ?: true) }

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        AppCard(
            style = AppCardStyle.Outlined,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.receipt_config_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                AppTextField(
                    value = header,
                    onValueChange = { header = it },
                    label = stringResource(Res.string.receipt_header_label),
                    placeholder = stringResource(Res.string.receipt_header_placeholder),
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    enabled = !isSaving,
                    singleLine = false,
                    maxLines = 3,
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = stringResource(Res.string.receipt_footer_label),
                    placeholder = stringResource(Res.string.receipt_footer_placeholder),
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    enabled = !isSaving,
                    singleLine = false,
                    maxLines = 3,
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = stringResource(Res.string.receipt_logo_url_label),
                    placeholder = "https://example.com/logo.png",
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    enabled = !isSaving,
                    singleLine = true,
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.receipt_show_tax_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.receipt_show_tax_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondaryLight,
                        )
                    }
                    Switch(
                        checked = showTax,
                        onCheckedChange = { showTax = it },
                        enabled = !isSaving,
                        colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = AppColors.Primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.receipt_configure_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.receipt_preview_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ReceiptPreviewCard(
            header = header.ifBlank { stringResource(Res.string.receipt_sample_thank_you) },
            footer = footer.ifBlank { stringResource(Res.string.receipt_sample_please_come_again) },
            showTax = showTax,
            logoUrl = logoUrl.ifBlank { null },
        )
    }
}

@Composable
private fun ReceiptPreviewCard(header: String, footer: String, showTax: Boolean, logoUrl: String?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = PosShapes.ProductCard,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (logoUrl != null) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.receipt_sample_logo_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondaryLight,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            Text(
                text = stringResource(Res.string.receipt_preview_store_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = header,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.receipt_preview_item_1),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "$10.00",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.receipt_preview_item_2),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "$15.00",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.receipt_preview_subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "$25.00",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (showTax) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.receipt_preview_tax_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$4.00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.receipt_preview_total_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (showTax) "$29.00" else "$25.00",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.receipt_preview_title),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondaryLight,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
