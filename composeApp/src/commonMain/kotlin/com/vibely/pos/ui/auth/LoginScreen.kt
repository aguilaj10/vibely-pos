package com.vibely.pos.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonSize
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.ValidationState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.auth_email_label
import vibely_pos.composeapp.generated.resources.auth_email_placeholder
import vibely_pos.composeapp.generated.resources.auth_hide_password
import vibely_pos.composeapp.generated.resources.auth_password_label
import vibely_pos.composeapp.generated.resources.auth_password_placeholder
import vibely_pos.composeapp.generated.resources.auth_remember_me
import vibely_pos.composeapp.generated.resources.auth_show_password
import vibely_pos.composeapp.generated.resources.auth_sign_in
import vibely_pos.composeapp.generated.resources.auth_sign_in_to_your_account
import vibely_pos.composeapp.generated.resources.auth_welcome_back

/**
 * Login screen for user authentication.
 *
 * Features:
 * - Email and password input fields
 * - Password visibility toggle
 * - "Remember Me" checkbox
 * - Loading state during authentication
 * - Error handling with toast notifications
 * - Navigation to dashboard on success
 *
 * @param onLoginSuccess Callback invoked when login succeeds.
 * @param modifier Optional modifier for customization.
 * @param viewModel The LoginViewModel (injected via Koin).
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, modifier: Modifier = Modifier, viewModel: LoginViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // Navigate to dashboard when login succeeds
    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Show error toast if there's an error message
    LaunchedEffect(state.errorMessage) {
        // Toast notification would be shown here
        // For now, the error is displayed in the UI
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AppCard(
            modifier =
            Modifier
                .width(400.dp)
                .padding(16.dp),
            style = AppCardStyle.Elevated,
            elevation = 4.dp,
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.auth_welcome_back),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = stringResource(Res.string.auth_sign_in_to_your_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to your account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email field
                AppTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = stringResource(Res.string.auth_email_label),
                    placeholder = stringResource(Res.string.auth_email_placeholder),
                    validationState =
                    if (state.emailError != null) {
                        ValidationState.Error(state.emailError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !state.isLoading,
                    singleLine = true,
                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions =
                    KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                AppTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = stringResource(Res.string.auth_password_label),
                    placeholder = stringResource(Res.string.auth_password_placeholder),
                    validationState =
                    if (state.passwordError != null) {
                        ValidationState.Error(state.passwordError!!)
                    } else {
                        ValidationState.None
                    },
                    enabled = !state.isLoading,
                    singleLine = true,
                    visualTransformation =
                    if (state.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val hidePasswordDesc = stringResource(Res.string.auth_hide_password)
                        val showPasswordDesc = stringResource(Res.string.auth_show_password)

                        IconButton(
                            onClick = viewModel::onPasswordVisibilityToggle,
                            enabled = !state.isLoading,
                            modifier =
                            Modifier.semantics {
                                contentDescription =
                                    if (state.isPasswordVisible) hidePasswordDesc else showPasswordDesc
                            },
                        ) {
                            Text(
                                text = if (state.isPasswordVisible) "🙈" else "👁",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions =
                    KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (state.isLoginEnabled) {
                                viewModel.onLoginClick()
                            }
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Remember Me checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.rememberMe,
                        onCheckedChange = viewModel::onRememberMeChange,
                        enabled = !state.isLoading,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.auth_remember_me),
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                        if (state.isLoading) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message display
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Login button
                AppButton(
                    text = stringResource(Res.string.auth_sign_in),
                    onClick = viewModel::onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    style = AppButtonStyle.Primary,
                    size = AppButtonSize.Large,
                    enabled = state.isLoginEnabled,
                    loading = state.isLoading,
                )
            }
        }
    }
}
