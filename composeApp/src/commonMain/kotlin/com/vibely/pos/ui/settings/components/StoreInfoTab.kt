package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.theme.AppColors
import com.vibely.pos.ui.theme.PosShapes
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Envelope
import compose.icons.fontawesomeicons.solid.MapMarkerAlt
import compose.icons.fontawesomeicons.solid.Phone
import compose.icons.fontawesomeicons.solid.Store
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.store_info_address
import vibely_pos.composeapp.generated.resources.store_info_address_error
import vibely_pos.composeapp.generated.resources.store_info_address_placeholder
import vibely_pos.composeapp.generated.resources.store_info_description
import vibely_pos.composeapp.generated.resources.store_info_email
import vibely_pos.composeapp.generated.resources.store_info_email_error
import vibely_pos.composeapp.generated.resources.store_info_email_error_invalid
import vibely_pos.composeapp.generated.resources.store_info_email_placeholder
import vibely_pos.composeapp.generated.resources.store_info_phone
import vibely_pos.composeapp.generated.resources.store_info_phone_error
import vibely_pos.composeapp.generated.resources.store_info_phone_placeholder
import vibely_pos.composeapp.generated.resources.store_info_store_name
import vibely_pos.composeapp.generated.resources.store_info_store_name_error
import vibely_pos.composeapp.generated.resources.store_info_store_name_placeholder
import vibely_pos.composeapp.generated.resources.store_info_title

@Composable
fun StoreInfoTab(
    storeSettings: com.vibely.pos.shared.domain.settings.entity.StoreSettings?,
    onStoreInfoChange: (String, String, String, String) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    var storeName by remember(storeSettings) { mutableStateOf(storeSettings?.storeName ?: "") }
    var address by remember(storeSettings) { mutableStateOf(storeSettings?.address ?: "") }
    var phone by remember(storeSettings) { mutableStateOf(storeSettings?.phone ?: "") }
    var email by remember(storeSettings) { mutableStateOf(storeSettings?.email ?: "") }

    var storeNameError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val errorStoreNameRequired = stringResource(Res.string.store_info_store_name_error)
    val errorAddressRequired = stringResource(Res.string.store_info_address_error)
    val errorPhoneRequired = stringResource(Res.string.store_info_phone_error)
    val errorEmailRequired = stringResource(Res.string.store_info_email_error)
    val errorEmailInvalid = stringResource(Res.string.store_info_email_error_invalid)

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
                    text = stringResource(Res.string.store_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OutlinedTextField(
                    value = storeName,
                    onValueChange = { newValue ->
                        storeName = newValue
                        storeNameError = if (newValue.isBlank()) errorStoreNameRequired else null
                    },
                    label = { Text(stringResource(Res.string.store_info_store_name)) },
                    placeholder = { Text(stringResource(Res.string.store_info_store_name_placeholder)) },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = storeNameError != null,
                    supportingText = storeNameError?.let { { Text(it) } },
                    enabled = !isSaving,
                    singleLine = true,
                    shape = PosShapes.InputField,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { newValue ->
                        address = newValue
                        addressError = if (newValue.isBlank()) errorAddressRequired else null
                    },
                    label = { Text(stringResource(Res.string.store_info_address)) },
                    placeholder = { Text(stringResource(Res.string.store_info_address_placeholder)) },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.MapMarkerAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = addressError != null,
                    supportingText = addressError?.let { { Text(it) } },
                    enabled = !isSaving,
                    singleLine = false,
                    maxLines = 3,
                    shape = PosShapes.InputField,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { newValue ->
                        phone = newValue
                        phoneError = if (newValue.isBlank()) errorPhoneRequired else null
                    },
                    label = { Text(stringResource(Res.string.store_info_phone)) },
                    placeholder = { Text(stringResource(Res.string.store_info_phone_placeholder)) },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it) } },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                    shape = PosShapes.InputField,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { newValue ->
                        email = newValue
                        emailError =
                            if (newValue.isBlank()) {
                                errorEmailRequired
                            } else if (!newValue.contains("@")) {
                                errorEmailInvalid
                            } else {
                                null
                            }
                    },
                    label = { Text(stringResource(Res.string.store_info_email)) },
                    placeholder = { Text(stringResource(Res.string.store_info_email_placeholder)) },
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = FontAwesomeIcons.Solid.Envelope,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    shape = PosShapes.InputField,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.store_info_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondaryLight,
                )
            }
        }
    }
}
