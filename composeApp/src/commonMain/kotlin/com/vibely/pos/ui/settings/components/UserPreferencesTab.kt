package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bell
import compose.icons.fontawesomeicons.solid.Clock
import compose.icons.fontawesomeicons.solid.Language
import compose.icons.fontawesomeicons.solid.Palette
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.preferences_120_min
import vibely_pos.composeapp.generated.resources.preferences_5_min
import vibely_pos.composeapp.generated.resources.preferences_auto_logout_timeout
import vibely_pos.composeapp.generated.resources.preferences_automatically_logout
import vibely_pos.composeapp.generated.resources.preferences_dark
import vibely_pos.composeapp.generated.resources.preferences_enable_notifications
import vibely_pos.composeapp.generated.resources.preferences_english
import vibely_pos.composeapp.generated.resources.preferences_french
import vibely_pos.composeapp.generated.resources.preferences_language
import vibely_pos.composeapp.generated.resources.preferences_light
import vibely_pos.composeapp.generated.resources.preferences_receive_alerts
import vibely_pos.composeapp.generated.resources.preferences_settings_apply
import vibely_pos.composeapp.generated.resources.preferences_spanish
import vibely_pos.composeapp.generated.resources.preferences_system
import vibely_pos.composeapp.generated.resources.preferences_theme
import vibely_pos.composeapp.generated.resources.preferences_timeout_format
import vibely_pos.composeapp.generated.resources.preferences_user_preferences
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
fun UserPreferencesTab(
    userPreferences: com.vibely.pos.shared.domain.settings.entity.UserPreferences?,
    onPreferencesChange: (String, String, Boolean, Duration) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    var language by remember(userPreferences) { mutableStateOf(userPreferences?.language ?: "en") }
    var theme by remember(userPreferences) { mutableStateOf(userPreferences?.theme ?: "system") }
    var enableNotifications by remember(
        userPreferences,
    ) { mutableStateOf(userPreferences?.enableNotifications ?: true) }
    var autoLogoutTimeout by remember(userPreferences) {
        mutableFloatStateOf(
            (userPreferences?.autoLogoutTimeout?.inWholeMinutes?.toFloat() ?: 30f),
        )
    }

    var languageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    val languages = listOf("en", "es", "fr")
    val languageLabels =
        mapOf(
            "en" to stringResource(Res.string.preferences_english),
            "es" to stringResource(Res.string.preferences_spanish),
            "fr" to stringResource(Res.string.preferences_french),
        )

    val themes = listOf("light", "dark", "system")
    val themeLabels =
        mapOf(
            "light" to stringResource(Res.string.preferences_light),
            "dark" to stringResource(Res.string.preferences_dark),
            "system" to stringResource(Res.string.preferences_system),
        )

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
                    text = stringResource(Res.string.preferences_user_preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AppTextField(
                        value = languageLabels[language] ?: language,
                        onValueChange = {},
                        readOnly = true,
                        label = stringResource(Res.string.preferences_language),
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = FontAwesomeIcons.Solid.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded)
                        },
                        enabled = !isSaving,
                        singleLine = true,
                        variant = AppTextFieldVariant.Outlined,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false },
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(languageLabels[lang] ?: lang) },
                                onClick = {
                                    language = lang
                                    languageExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = themeExpanded,
                    onExpandedChange = { themeExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AppTextField(
                        value = themeLabels[theme] ?: theme,
                        onValueChange = {},
                        readOnly = true,
                        label = stringResource(Res.string.preferences_theme),
                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = FontAwesomeIcons.Solid.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                        },
                        enabled = !isSaving,
                        singleLine = true,
                        variant = AppTextFieldVariant.Outlined,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false },
                    ) {
                        themes.forEach { th ->
                            DropdownMenuItem(
                                text = { Text(themeLabels[th] ?: th) },
                                onClick = {
                                    theme = th
                                    themeExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = FontAwesomeIcons.Solid.Bell,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(Res.string.preferences_enable_notifications),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = stringResource(Res.string.preferences_receive_alerts),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondaryLight,
                            modifier = Modifier.padding(start = 32.dp),
                        )
                    }
                    Switch(
                        checked = enableNotifications,
                        onCheckedChange = { enableNotifications = it },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = FontAwesomeIcons.Solid.Clock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.preferences_auto_logout_timeout),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(Res.string.preferences_automatically_logout),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                    modifier = Modifier.padding(start = 32.dp, bottom = 8.dp),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Slider(
                        value = autoLogoutTimeout,
                        onValueChange = { autoLogoutTimeout = it },
                        onValueChangeFinished = {
                            onPreferencesChange(
                                language,
                                theme,
                                enableNotifications,
                                autoLogoutTimeout.toInt().minutes,
                            )
                        },
                        valueRange = 5f..120f,
                        steps = 22,
                        enabled = !isSaving,
                        colors =
                        SliderDefaults.colors(
                            thumbColor = AppColors.Primary,
                            activeTrackColor = AppColors.Primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.preferences_5_min),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondaryLight,
                        )
                        Text(
                            text = stringResource(Res.string.preferences_timeout_format, autoLogoutTimeout.toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(Res.string.preferences_120_min),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextSecondaryLight,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.preferences_settings_apply),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                )
            }
        }
    }
}
