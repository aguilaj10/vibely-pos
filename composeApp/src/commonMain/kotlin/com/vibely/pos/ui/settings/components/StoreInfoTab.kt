package com.vibely.pos.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.ValidationState
import com.vibely.pos.ui.theme.AppColors
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

                AppTextField(
                    value = storeName,
                    onValueChange = { newValue ->
                        storeName = newValue
                        storeNameError = if (newValue.isBlank()) errorStoreNameRequired else null
                    },
                    label = stringResource(Res.string.store_info_store_name),
                    placeholder = stringResource(Res.string.store_info_store_name_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 20.dp).size(20.dp),
                        )
                    },
                    validationState =
                    if (storeNameError !=
                        null
                    ) {
                        ValidationState.Error(storeNameError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !isSaving,
                    singleLine = true,
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextField(
                    value = address,
                    onValueChange = { newValue ->
                        address = newValue
                        addressError = if (newValue.isBlank()) errorAddressRequired else null
                    },
                    label = stringResource(Res.string.store_info_address),
                    placeholder = stringResource(Res.string.store_info_address_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.MapMarkerAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 20.dp).size(20.dp),
                        )
                    },
                    validationState =
                    if (addressError !=
                        null
                    ) {
                        ValidationState.Error(addressError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !isSaving,
                    singleLine = false,
                    maxLines = 3,
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextField(
                    value = phone,
                    onValueChange = { newValue ->
                        phone = newValue
                        phoneError = if (newValue.isBlank()) errorPhoneRequired else null
                    },
                    label = stringResource(Res.string.store_info_phone),
                    placeholder = stringResource(Res.string.store_info_phone_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 20.dp).size(20.dp),
                        )
                    },
                    validationState =
                    if (phoneError !=
                        null
                    ) {
                        ValidationState.Error(phoneError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                    variant = AppTextFieldVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppTextField(
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
                    label = stringResource(Res.string.store_info_email),
                    placeholder = stringResource(Res.string.store_info_email_placeholder),
                    leadingIcon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Envelope,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 20.dp).size(20.dp),
                        )
                    },
                    validationState =
                    if (emailError !=
                        null
                    ) {
                        ValidationState.Error(emailError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions =
                    androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    variant = AppTextFieldVariant.Outlined,
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
