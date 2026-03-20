# Authentication UI Implementation

## Overview

This document describes the UI layer implementation for the authentication system in the Vibely POS application.

## Components

### LoginViewModel

- **Location**: `composeApp/src/commonMain/kotlin/com/vibely/pos/ui/auth/LoginViewModel.kt`
- **Purpose**: Manages the state and business logic for the login screen
- **Key Features**:
  - StateFlow-based reactive state management
  - Form validation (email and password)
  - Error handling
  - Loading state management
  - Integration with `LoginUseCase` from the domain layer

### LoginScreen

- **Location**: `composeApp/src/commonMain/kotlin/com/vibely/pos/ui/auth/LoginScreen.kt`
- **Purpose**: Composable UI for user authentication
- **Key Features**:
  - Email input field with validation
  - Password input field with visibility toggle
  - "Remember Me" checkbox
  - Loading indicator during authentication
  - Error message display
  - Keyboard actions (Next/Done)
  - Navigation to dashboard on success

## State Management

The `LoginState` data class contains:

| Field | Description |
|-------|-------------|
| `email` | Current email input |
| `password` | Current password input |
| `isPasswordVisible` | Toggle for password visibility |
| `rememberMe` | Remember me checkbox state |
| `isLoading` | Loading state during authentication |
| `isLoginSuccessful` | Success flag for navigation |
| `emailError` | Email validation error message |
| `passwordError` | Password validation error message |
| `errorMessage` | General error message |

## Usage

```kotlin
LoginScreen(
    onLoginSuccess = { /* Navigate to dashboard */ }
)
```

## Dependency Injection

`LoginViewModel` is registered in the DI container via `UiModule.kt` and injected using Koin:

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()
) {
    // ...
}
```

## Testing

To manually test the login screen:

1. Run the desktop application: `./gradlew :composeApp:desktopRun`
2. Enter valid credentials:
   - Email: must be a valid email format
   - Password: must meet security requirements (8+ characters, uppercase, lowercase, digit, special character)
3. Click "Sign In"

For automated backend authentication tests, see [backend/README.md](../backend/README.md#testing).

## Future Enhancements

- [ ] Forgot password flow
- [ ] Social login integration
- [ ] Biometric authentication
- [ ] Auto-fill support
- [ ] Better error toast notifications
- [ ] Accessibility improvements
- [ ] Animated transitions
