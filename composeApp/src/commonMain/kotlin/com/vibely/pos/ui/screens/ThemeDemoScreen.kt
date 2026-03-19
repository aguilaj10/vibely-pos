package com.vibely.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonSize
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppSearchField
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppToastHost
import com.vibely.pos.ui.components.ProductCard
import com.vibely.pos.ui.components.SummaryCard
import com.vibely.pos.ui.components.ToastType
import com.vibely.pos.ui.components.ValidationState
import com.vibely.pos.ui.components.rememberToastState
import com.vibely.pos.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.theme_demo_description
import vibely_pos.composeapp.generated.resources.theme_demo_destructive_button
import vibely_pos.composeapp.generated.resources.theme_demo_elevated_card_desc
import vibely_pos.composeapp.generated.resources.theme_demo_elevated_card_title
import vibely_pos.composeapp.generated.resources.theme_demo_error
import vibely_pos.composeapp.generated.resources.theme_demo_filled_card_desc
import vibely_pos.composeapp.generated.resources.theme_demo_filled_card_title
import vibely_pos.composeapp.generated.resources.theme_demo_info
import vibely_pos.composeapp.generated.resources.theme_demo_outlined_button
import vibely_pos.composeapp.generated.resources.theme_demo_outlined_card_desc
import vibely_pos.composeapp.generated.resources.theme_demo_outlined_card_title
import vibely_pos.composeapp.generated.resources.theme_demo_primary_button
import vibely_pos.composeapp.generated.resources.theme_demo_search_placeholder
import vibely_pos.composeapp.generated.resources.theme_demo_secondary_button
import vibely_pos.composeapp.generated.resources.theme_demo_size_large
import vibely_pos.composeapp.generated.resources.theme_demo_size_medium
import vibely_pos.composeapp.generated.resources.theme_demo_size_small
import vibely_pos.composeapp.generated.resources.theme_demo_success
import vibely_pos.composeapp.generated.resources.theme_demo_tertiary_button
import vibely_pos.composeapp.generated.resources.theme_demo_text_field_label
import vibely_pos.composeapp.generated.resources.theme_demo_text_field_placeholder
import vibely_pos.composeapp.generated.resources.theme_demo_text_field_validation
import vibely_pos.composeapp.generated.resources.theme_demo_text_field_validation_label
import vibely_pos.composeapp.generated.resources.theme_demo_title
import vibely_pos.composeapp.generated.resources.theme_demo_warning

/**
 * Theme demo screen showcasing all custom components and styles
 * This screen demonstrates the Vibely POS theme system
 */
@Composable
fun ThemeDemoScreen() {
    val toastState = rememberToastState()
    val scope = rememberCoroutineScope()
    var textFieldValue by remember { mutableStateOf("") }
    var validationState by remember { mutableStateOf<ValidationState>(ValidationState.None) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = stringResource(Res.string.theme_demo_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.theme_demo_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Color Palette
            item {
                SectionHeader("Color Palette")
            }
            item {
                ColorPaletteDemo()
            }

            // Typography section removed - TypographyDemo() function not implemented

            // Buttons
            item {
                SectionHeader("Buttons")
            }
            item {
                ButtonsDemo(onShowToast = { message, type ->
                    scope.launch { toastState.show(message, type) }
                })
            }

            // Text Fields
            item {
                SectionHeader("Text Fields")
            }
            item {
                TextFieldsDemo(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        validationState =
                            when {
                                it.isEmpty() -> ValidationState.None
                                it.length < 3 -> ValidationState.Error("Minimum 3 characters required")
                                it.length < 5 -> ValidationState.Warning("Consider adding more characters")
                                else -> ValidationState.Valid
                            }
                    },
                    validationState = validationState,
                )
            }

            // Cards
            item {
                SectionHeader("Cards")
            }
            item {
                CardsDemo()
            }

            // Product Cards
            item {
                SectionHeader("Product Cards")
            }
            item {
                ProductCardsDemo()
            }

            // Summary Cards
            item {
                SectionHeader("Summary Cards")
            }
            item {
                SummaryCardsDemo()
            }

            // Toasts
            item {
                SectionHeader("Toasts & Notifications")
            }
            item {
                ToastsDemo(onShowToast = { message, type ->
                    scope.launch { toastState.show(message, type) }
                })
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Toast host
        AppToastHost(
            state = toastState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun ColorPaletteDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorRow("Primary", AppColors.Primary, AppColors.OnPrimary)
        ColorRow("Secondary", AppColors.Secondary, AppColors.OnSecondary)
        ColorRow("Tertiary", AppColors.Tertiary, AppColors.OnTertiary)
        ColorRow("Success", AppColors.Success, AppColors.OnSuccess)
        ColorRow("Warning", AppColors.Warning, AppColors.OnWarning)
        ColorRow("Error", AppColors.Error, AppColors.OnError)
        ColorRow("Info", AppColors.Info, AppColors.OnInfo)
    }
}

@Composable
private fun ColorRow(name: String, color: androidx.compose.ui.graphics.Color, onColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            color = onColor,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun CardsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppCard(style = AppCardStyle.Elevated) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.theme_demo_elevated_card_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.theme_demo_elevated_card_desc),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        AppCard(style = AppCardStyle.Outlined) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.theme_demo_outlined_card_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.theme_demo_outlined_card_desc),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        AppCard(
            style = AppCardStyle.Filled,
            containerColor = AppColors.PrimaryLight.copy(alpha = 0.1f),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.theme_demo_filled_card_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.theme_demo_filled_card_desc),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ButtonsDemo(onShowToast: (String, ToastType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppButton(
            text = stringResource(Res.string.theme_demo_primary_button),
            onClick = { onShowToast("Primary button clicked", ToastType.Info) },
            style = AppButtonStyle.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_secondary_button),
            onClick = { onShowToast("Secondary button clicked", ToastType.Info) },
            style = AppButtonStyle.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_tertiary_button),
            onClick = { onShowToast("Tertiary button clicked", ToastType.Info) },
            style = AppButtonStyle.Tertiary,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_destructive_button),
            onClick = { onShowToast("Destructive action", ToastType.Warning) },
            style = AppButtonStyle.Destructive,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_outlined_button),
            onClick = { onShowToast("Outlined button clicked", ToastType.Info) },
            style = AppButtonStyle.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                text = stringResource(Res.string.theme_demo_size_small),
                onClick = { },
                size = AppButtonSize.Small,
            )
            AppButton(
                text = stringResource(Res.string.theme_demo_size_medium),
                onClick = { },
                size = AppButtonSize.Medium,
            )
            AppButton(
                text = stringResource(Res.string.theme_demo_size_large),
                onClick = { },
                size = AppButtonSize.Large,
            )
        }
    }
}

@Composable
private fun TextFieldsDemo(value: String, onValueChange: (String) -> Unit, validationState: ValidationState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            label = stringResource(Res.string.theme_demo_text_field_label),
            placeholder = stringResource(Res.string.theme_demo_text_field_placeholder),
        )
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            label = stringResource(Res.string.theme_demo_text_field_validation_label),
            placeholder = stringResource(Res.string.theme_demo_text_field_validation),
            validationState = validationState,
        )
        AppSearchField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(Res.string.theme_demo_search_placeholder),
        )
    }
}

@Composable
private fun ProductCardsDemo() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(300.dp),
    ) {
        items(
            listOf(
                "Espresso" to "$3.50",
                "Cappuccino" to "$4.50",
                "Latte" to "$5.00",
                "Mocha" to "$5.50",
            ),
        ) { (name, price) ->
            ProductCard(
                name = name,
                price = price,
                onClick = { },
            )
        }
    }
}

@Composable
private fun SummaryCardsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SummaryCard(
                title = "Today's Sales",
                value = "$1,234",
                subtitle = "+12% from yesterday",
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                title = "Orders",
                value = "45",
                subtitle = "23 pending",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ToastsDemo(onShowToast: (String, ToastType) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        AppButton(
            text = stringResource(Res.string.theme_demo_success),
            onClick = { onShowToast("Operation successful!", ToastType.Success) },
            style = AppButtonStyle.Secondary,
            size = AppButtonSize.Small,
            modifier = Modifier.weight(1f),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_error),
            onClick = { onShowToast("Something went wrong!", ToastType.Error) },
            style = AppButtonStyle.Destructive,
            size = AppButtonSize.Small,
            modifier = Modifier.weight(1f),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_warning),
            onClick = { onShowToast("Please check your input", ToastType.Warning) },
            style = AppButtonStyle.Tertiary,
            size = AppButtonSize.Small,
            modifier = Modifier.weight(1f),
        )
        AppButton(
            text = stringResource(Res.string.theme_demo_info),
            onClick = { onShowToast("New update available", ToastType.Info) },
            style = AppButtonStyle.Primary,
            size = AppButtonSize.Small,
            modifier = Modifier.weight(1f),
        )
    }
}
