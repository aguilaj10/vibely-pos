# Authentication UI Implementation

## Overview

This directory contains the UI layer implementation for the authentication system in the Vibely POS application.

## Components

### LoginViewModel
- **Location**: `LoginViewModel.kt`
- **Purpose**: Manages the state and business logic for the login screen
- **Key Features**:
  - StateFlow-based reactive state management
  - Form validation (email and password)
  - Error handling
  - Loading state management
  - Integration with LoginUseCase from domain layer

### LoginScreen
- **Location**: `LoginScreen.kt`
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
- `email`: Current email input
- `password`: Current password input
- `isPasswordVisible`: Toggle for password visibility
- `rememberMe`: Remember me checkbox state
- `isLoading`: Loading state during authentication
- `isLoginSuccessful`: Success flag for navigation
- `emailError`: Email validation error message
- `passwordError`: Password validation error message
- `errorMessage`: General error message

## Usage

```kotlin
LoginScreen(
    onLoginSuccess = { /* Navigate to dashboard */ }
)
```

## Integration

The LoginViewModel is registered in the DI container via `UiModule.kt` and can be injected using Koin:

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()
) {
    // ...
}
```

## Testing

To test the login screen:

1. Run the desktop application: `./gradlew :composeApp:desktopRun`
2. Enter valid credentials:
   - Email: Must be a valid email format
   - Password: Must meet security requirements (8+ chars, uppercase, lowercase, digit, special char)
3. Click "Sign In"

## Future Enhancements

- [ ] Forgot password flow
- [ ] Social login integration
- [ ] Biometric authentication
- [ ] Auto-fill support
- [ ] Better error toast notifications
- [ ] Accessibility improvements
- [ ] Animated transitions
