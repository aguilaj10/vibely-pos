package com.vibely.pos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.auth.usecase.LoginUseCase
import com.vibely.pos.shared.domain.auth.valueobject.Credentials
import com.vibely.pos.shared.domain.exception.ValidationException
import com.vibely.pos.shared.domain.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen.
 *
 * Manages login form state, validation, and authentication flow.
 * Uses StateFlow for reactive UI updates.
 */
class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /**
     * Handles email input changes.
     */
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    /**
     * Handles password input changes.
     */
    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    /**
     * Toggles password visibility.
     */
    fun onPasswordVisibilityToggle() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Handles "Remember Me" checkbox changes.
     */
    fun onRememberMeChange(rememberMe: Boolean) {
        _state.update { it.copy(rememberMe = rememberMe) }
    }

    /**
     * Dismisses the error toast.
     */
    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Attempts to log in with the current credentials.
     */
    fun onLoginClick() {
        // Clear previous errors
        _state.update {
            it.copy(
                emailError = null,
                passwordError = null,
                errorMessage = null,
                isLoading = true,
            )
        }

        viewModelScope.launch {
            try {
                // Validate and create credentials
                val credentials = Credentials.create(
                    email = _state.value.email.trim(),
                    password = _state.value.password,
                )

                // Attempt login
                when (val result = loginUseCase(credentials)) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                            )
                        }
                    }
                    is Result.Error -> {
                        handleLoginError(result.message)
                    }
                }
            } catch (e: ValidationException) {
                handleValidationError(e)
            } catch (e: Exception) {
                handleLoginError(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private fun handleValidationError(exception: ValidationException) {
        _state.update {
            when (exception.field) {
                "email" -> it.copy(
                    isLoading = false,
                    emailError = exception.message,
                )
                "password" -> it.copy(
                    isLoading = false,
                    passwordError = exception.message,
                )
                else -> it.copy(
                    isLoading = false,
                    errorMessage = exception.message,
                )
            }
        }
    }

    private fun handleLoginError(message: String) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
            )
        }
    }
}

/**
 * State for the Login screen.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
) {
    /**
     * Returns true if the login button should be enabled.
     */
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
