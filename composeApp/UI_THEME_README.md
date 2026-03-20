# Vibely POS UI Theme System

A complete custom theme system for the Vibely POS application built with Compose Multiplatform and Material 3.

## Overview

This theme system provides a consistent, professional design language optimized for Point of Sale (POS) environments. It includes custom colors, typography, shapes, and reusable components that work across all platforms (Desktop, Android, iOS, and Web).

## Structure

```
composeApp/src/commonMain/kotlin/com/vibely/pos/ui/
├── theme/
│   ├── AppColors.kt        # Color palette and color schemes
│   ├── AppTypography.kt    # Typography system with POS-specific styles
│   ├── AppShapes.kt        # Shape system with corner radius definitions
│   └── AppTheme.kt         # Main theme composable
├── components/
│   ├── AppButton.kt        # Custom button component with variants
│   ├── AppTextField.kt     # Text input with validation states
│   ├── AppCard.kt          # Card components (Elevated, Outlined, Filled)
│   └── AppToast.kt         # Toast/Snackbar notifications
└── screens/
    └── ThemeDemoScreen.kt  # Demo screen showcasing all components
```

## Theme System

### Colors

The color palette is designed for professional retail/restaurant environments:

**Brand Colors:**
- **Primary (Indigo)** - Trust and professionalism (#6366F1)
- **Secondary (Emerald)** - Success and growth (#10B981)
- **Tertiary (Amber)** - Energy and attention (#F59E0B)

**Status Colors:**
- **Success** - Green (#10B981)
- **Warning** - Amber (#F59E0B)
- **Error** - Red (#EF4444)
- **Info** - Blue (#3B82F6)

**Neutral Colors:**
- Comprehensive grayscale palette for light and dark modes
- Optimized text colors for readability

**Usage:**
```kotlin
// Access via Material3 theme
val primary = MaterialTheme.colorScheme.primary

// Access custom colors
val colors = appColors()
val successColor = colors.Success
```

### Typography

Material 3 typography system with POS-specific additions:

**Standard Styles:**
- Display (Large, Medium, Small) - For major headlines
- Headline (Large, Medium, Small) - For section headers
- Title (Large, Medium, Small) - For card titles and dialogs
- Body (Large, Medium, Small) - For main content
- Label (Large, Medium, Small) - For buttons and labels

**POS-Specific Styles:**
- **PriceDisplay** - Large price displays (48sp, Bold)
- **PriceMedium** - Cart item prices (20sp, SemiBold)
- **PriceSmall** - Product card prices (16sp, Medium)
- **ProductCode** - SKU/barcode displays (Monospace, 12sp)
- **ReceiptText** - Receipt formatting (Monospace, 14sp)
- **NumericInput** - Calculator-style input (32sp, Medium)

**Usage:**
```kotlin
Text("$199.99", style = PosTextStyles.PriceDisplay)
Text("SKU-12345", style = PosTextStyles.ProductCode)
```

### Shapes

Consistent corner radius system:

**Standard Shapes:**
- Extra Small (4dp) - Chips, tags
- Small (8dp) - Buttons, small cards
- Medium (12dp) - Standard cards, dialogs
- Large (16dp) - Bottom sheets, large cards
- Extra Large (24dp) - Modal overlays

**POS-Specific Shapes:**
- ProductCard, ActionButton, InputField, BottomSheet, Toast, Dialog, etc.

**Usage:**
```kotlin
Card(shape = PosShapes.ProductCard) { ... }
```

## Components

### AppButton

Customizable button component with multiple styles and sizes.

**Styles:**
- `Primary` - Main actions (e.g., "Complete Sale")
- `Secondary` - Secondary actions (e.g., "Add to Cart")
- `Tertiary` - Tertiary actions (e.g., "View Details")
- `Destructive` - Destructive actions (e.g., "Delete")
- `Outlined` - Less prominent actions
- `Text` - Minimal emphasis

**Sizes:**
- `Small` - Compact buttons
- `Medium` - Standard buttons
- `Large` - Prominent buttons

**Features:**
- Loading state with spinner
- Optional leading icon
- Custom shapes
- Enable/disable state

**Usage:**
```kotlin
AppButton(
    text = "Complete Sale",
    onClick = { /* ... */ },
    style = AppButtonStyle.Primary,
    size = AppButtonSize.Large,
    loading = isProcessing,
)
```

### AppTextField

Text input component with validation states.

**Validation States:**
- `None` - No validation
- `Valid` - Input is valid
- `Error` - Validation error with message
- `Warning` - Validation warning with message

**Features:**
- Automatic border color based on validation state
- Error/warning messages below field
- Custom leading/trailing icons
- Search field variant with clear button

**Usage:**
```kotlin
AppTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    validationState = ValidationState.Error("Invalid email format"),
)

// Search variant
AppSearchField(
    value = query,
    onValueChange = { query = it },
    placeholder = "Search products...",
)
```

### AppCard

Card component with multiple style variants.

**Styles:**
- `Elevated` - Card with shadow
- `Outlined` - Card with border
- `Filled` - Card with background color

**Specialized Variants:**
- `ProductCard` - Pre-styled for product listings
- `SummaryCard` - Pre-styled for dashboard summaries

**Features:**
- Optional click handler
- Custom colors and shapes
- Configurable elevation and borders

**Usage:**
```kotlin
AppCard(style = AppCardStyle.Elevated) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Card Title")
        Text("Card content")
    }
}

// Product card
ProductCard(
    name = "Espresso",
    price = "$3.50",
    onClick = { /* ... */ },
)

// Summary card
SummaryCard(
    title = "Today's Sales",
    value = "$1,234",
    subtitle = "+12% from yesterday",
)
```

### AppToast

Toast/Snackbar notification system.

**Types:**
- `Success` - Green, checkmark icon
- `Error` - Red, X icon
- `Warning` - Amber, warning icon
- `Info` - Blue, info icon

**Durations:**
- `Short` - 2 seconds
- `Medium` - 3.5 seconds
- `Long` - 5 seconds

**Features:**
- Animated slide-in/out
- Auto-dismiss with configurable duration
- Manual dismiss button
- Extension functions for easy usage

**Usage:**
```kotlin
val toastState = rememberToastState()
val scope = rememberCoroutineScope()

// Show toast
scope.launch {
    toastState.showSuccess("Order completed!")
    // or
    toastState.showError("Failed to process payment")
}

// Place host in your screen
AppToastHost(state = toastState)
```

## Theme Demo

The `ThemeDemoScreen` showcases all theme components and provides a live preview:

- Color palette display
- Typography examples
- All button variants and sizes
- Text field validation states
- Card variants (Elevated, Outlined, Filled)
- Product cards grid
- Summary cards
- Interactive toast demonstrations

## Dark Mode Support

The theme system fully supports both light and dark modes:

```kotlin
AppTheme(darkTheme = true) {
    // Your content
}

// Auto-detect system theme
AppTheme(darkTheme = isSystemInDarkTheme()) {
    // Your content
}
```

## Integration

### Update App.kt

The theme has been integrated into the main App composable:

```kotlin
@Composable
fun App() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ThemeDemoScreen()
        }
    }
}
```

### Using in Your Screens

Replace `MaterialTheme` with `AppTheme` for custom theme support:

```kotlin
@Composable
fun MyScreen() {
    AppTheme {
        // Your screen content with access to custom theme
        val colors = appColors()
        val customShape = PosShapes.ProductCard
    }
}
```

## Design Principles

1. **Consistency** - All components follow the same design language
2. **Accessibility** - High contrast colors and readable typography
3. **POS-Optimized** - Designed for retail/restaurant environments
4. **Professional** - Clean, modern aesthetic that inspires trust
5. **Responsive** - Works across all screen sizes and platforms
6. **Themeable** - Easy to customize colors, typography, and shapes

## Platform Support

✅ Desktop (JVM)
✅ Android
✅ iOS
✅ Web (JS/Wasm)

All components are built with Compose Multiplatform and work seamlessly across all target platforms.

## Testing

Run the demo screen to test all components:

```bash
./gradlew :composeApp:run
```

The demo screen provides interactive examples of all theme components and validates the theme system implementation.

## Dependencies

- Compose Multiplatform
- Material 3
- Kotlin Coroutines (for toast animations)

## License

Part of Vibely POS - MIT License. See [LICENSE](../LICENSE) for details.
