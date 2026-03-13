# UI Architecture - Compose Multiplatform

## Overview

This document outlines the UI implementation strategy for Vibely POS using Compose Multiplatform, ensuring consistent design across desktop, web, and mobile platforms while maintaining clean architecture principles.

## Architecture Principles

### 1. Separation of Concerns
- **NO business logic in Composables** - UI components are purely presentational
- **Unidirectional Data Flow (UDF)** - Data flows down, events flow up
- **MVVM Pattern** - ViewModels manage state, Composables render UI
- **StateFlow** for state management
- **Single Source of Truth** - State managed in ViewModels

### 2. Platform Adaptations
- Shared UI components with platform-specific behaviors
- Responsive layouts adapting to screen sizes
- Platform-specific navigation patterns
- Adaptive input methods (touch/mouse/keyboard)

---

## Theme System

### Color Palette (from Figma)

```kotlin
// commonMain/ui/theme/Color.kt
package com.vibely.pos.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary Colors
    val Primary = Color(0xFF6366F1)        // Indigo
    val PrimaryVariant = Color(0xFF4F46E5)
    val PrimaryLight = Color(0xFFEEF2FF)

    // Secondary Colors
    val Secondary = Color(0xFF10B981)      // Emerald
    val SecondaryVariant = Color(0xFF059669)
    val SecondaryLight = Color(0xFFD1FAE5)

    // Accent Colors
    val Accent = Color(0xFFF59E0B)         // Amber
    val AccentLight = Color(0xFFFEF3C7)

    // Neutral Colors
    val Background = Color(0xFFF9FAFB)     // Gray-50
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF3F4F6) // Gray-100

    // Text Colors
    val TextPrimary = Color(0xFF111827)    // Gray-900
    val TextSecondary = Color(0xFF6B7280)  // Gray-500
    val TextTertiary = Color(0xFF9CA3AF)   // Gray-400

    // Status Colors
    val Success = Color(0xFF10B981)        // Green
    val Warning = Color(0xFFF59E0B)        // Amber
    val Error = Color(0xFFEF4444)          // Red
    val Info = Color(0xFF3B82F6)           // Blue

    // Border Colors
    val Border = Color(0xFFE5E7EB)         // Gray-200
    val BorderStrong = Color(0xFFD1D5DB)   // Gray-300

    // Interactive States
    val Hover = Color(0xFFF3F4F6)
    val Pressed = Color(0xFFE5E7EB)
    val Disabled = Color(0xFFF9FAFB)
    val DisabledText = Color(0xFFD1D5DB)
}

// Dark Theme Support
object AppColorsDark {
    val Primary = Color(0xFF818CF8)
    val Background = Color(0xFF111827)
    val Surface = Color(0xFF1F2937)
    val TextPrimary = Color(0xFFF9FAFB)
    val TextSecondary = Color(0xFFD1D5DB)
    // ... extend for all colors
}
```

### Typography System (from Figma)

```kotlin
// commonMain/ui/theme/Typography.kt
package com.vibely.pos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    // Display Styles
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline Styles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title Styles
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body Styles
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label Styles
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Shape System

```kotlin
// commonMain/ui/theme/Shape.kt
package com.vibely.pos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),    // Chips, small buttons
    small = RoundedCornerShape(8.dp),         // Buttons, inputs
    medium = RoundedCornerShape(12.dp),       // Cards, modals
    large = RoundedCornerShape(16.dp),        // Large cards, sheets
    extraLarge = RoundedCornerShape(28.dp)    // Dialogs, bottom sheets
)

// Custom shapes for specific components
object CustomShapes {
    val pill = RoundedCornerShape(percent = 50)
    val bottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    val topSheet = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 28.dp,
        bottomEnd = 28.dp
    )
}
```

### Theme Configuration

```kotlin
// commonMain/ui/theme/Theme.kt
package com.vibely.pos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.PrimaryVariant,

    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SecondaryLight,
    onSecondaryContainer = AppColors.SecondaryVariant,

    tertiary = AppColors.Accent,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.AccentLight,

    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.TextSecondary,

    error = AppColors.Error,
    onError = Color.White,

    outline = AppColors.Border,
    outlineVariant = AppColors.BorderStrong
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColorsDark.Primary,
    background = AppColorsDark.Background,
    surface = AppColorsDark.Surface,
    onBackground = AppColorsDark.TextPrimary,
    onSurface = AppColorsDark.TextPrimary,
    // ... extend for all colors
)

@Composable
fun VibelyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
```

---

## Reusable Components

### 1. Button Components

```kotlin
// commonMain/ui/components/buttons/PrimaryButton.kt
package com.vibely.pos.ui.components.buttons

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            icon?.let {
                it()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        icon?.let {
            it()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled
    ) {
        icon()
    }
}
```

### 2. Input Components

```kotlin
// commonMain/ui/components/inputs/TextField.kt
package com.vibely.pos.ui.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(errorMessage)
                } else if (supportingText != null) {
                    Text(supportingText)
                }
            },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    onClear: () -> Unit = {}
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar"
                    )
                }
            }
        } else null,
        singleLine = true
    )
}
```

### 3. Card Components

```kotlin
// commonMain/ui/components/cards/AppCard.kt
package com.vibely.pos.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            elevation = elevation,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            elevation = elevation,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun ProductCard(
    name: String,
    price: String,
    imageUrl: String?,
    stock: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.width(160.dp),
        onClick = onClick
    ) {
        // Product image
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        // Product name
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        // Price
        Text(
            text = price,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Stock indicator
        if (stock < 10) {
            Text(
                text = "Stock: $stock",
                style = MaterialTheme.typography.bodySmall,
                color = if (stock == 0) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.warning
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trend: String? = null
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                trend?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            icon()
        }
    }
}
```

### 4. Toast/Snackbar Components

```kotlin
// commonMain/ui/components/feedback/Toast.kt
package com.vibely.pos.ui.components.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS, ERROR, WARNING, INFO
}

data class ToastData(
    val message: String,
    val type: ToastType,
    val duration: Long = 3000L,
    val action: ToastAction? = null
)

data class ToastAction(
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun ToastHost(
    toastState: MutableState<ToastData?>,
    modifier: Modifier = Modifier
) {
    val currentToast = toastState.value

    LaunchedEffect(currentToast) {
        if (currentToast != null) {
            delay(currentToast.duration)
            toastState.value = null
        }
    }

    if (currentToast != null) {
        CustomToast(
            data = currentToast,
            onDismiss = { toastState.value = null },
            modifier = modifier
        )
    }
}

@Composable
private fun CustomToast(
    data: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, containerColor, contentColor) = when (data.type) {
        ToastType.SUCCESS -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        ToastType.ERROR -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        ToastType.WARNING -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ToastType.INFO -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = data.message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            data.action?.let { action ->
                TextButton(onClick = action.onClick) {
                    Text(
                        text = action.label,
                        color = contentColor
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = contentColor
                )
            }
        }
    }
}

// Usage example in ViewModel
class ToastManager {
    val toastState = mutableStateOf<ToastData?>(null)

    fun showSuccess(message: String) {
        toastState.value = ToastData(message, ToastType.SUCCESS)
    }

    fun showError(message: String) {
        toastState.value = ToastData(message, ToastType.ERROR)
    }

    fun showWarning(message: String) {
        toastState.value = ToastData(message, ToastType.WARNING)
    }

    fun showInfo(message: String) {
        toastState.value = ToastData(message, ToastType.INFO)
    }
}
```

### 5. Dialog Components

```kotlin
// commonMain/ui/components/dialogs/Dialogs.kt
package com.vibely.pos.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun LoadingDialog(
    message: String = "Cargando...",
    onDismissRequest: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(message)
            }
        },
        confirmButton = {}
    )
}
```

### 6. Navigation Components

```kotlin
// commonMain/ui/components/navigation/NavigationBar.kt
package com.vibely.pos.ui.components.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route)
                            item.selectedIcon
                        else
                            item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun NavigationRail(
    items: List<NavigationItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationRail {
        items.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route)
                            item.selectedIcon
                        else
                            item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
```

---

## Screen Architecture (MVVM)

### ViewModel Base

```kotlin
// commonMain/ui/base/BaseViewModel.kt
package com.vibely.pos.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected fun updateState(update: State.() -> State) {
        _state.value = _state.value.update()
    }

    abstract fun onEvent(event: Event)

    protected fun launchOperation(
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    protected open fun handleError(error: Exception) {
        // Override in child classes for specific error handling
    }
}
```

### Screen State Pattern

```kotlin
// Example: Sales Screen
// commonMain/ui/screens/sales/SalesState.kt
package com.vibely.pos.ui.screens.sales

data class SalesState(
    val cartItems: List<CartItem> = emptyList(),
    val selectedCustomer: Customer? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isProcessing: Boolean = false,
    val searchQuery: String = "",
    val filteredProducts: List<Product> = emptyList(),
    val error: String? = null
)

sealed interface SalesEvent {
    data class AddToCart(val product: Product) : SalesEvent
    data class RemoveFromCart(val itemId: String) : SalesEvent
    data class UpdateQuantity(val itemId: String, val quantity: Int) : SalesEvent
    data class SelectCustomer(val customer: Customer?) : SalesEvent
    data class SelectPaymentMethod(val method: PaymentMethod) : SalesEvent
    data class SearchProducts(val query: String) : SalesEvent
    object ProcessSale : SalesEvent
    object ClearCart : SalesEvent
}
```

### ViewModel Implementation

```kotlin
// commonMain/ui/screens/sales/SalesViewModel.kt
package com.vibely.pos.ui.screens.sales

import com.vibely.pos.domain.usecases.*
import com.vibely.pos.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest

class SalesViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val processSaleUseCase: ProcessSaleUseCase,
    private val searchProductsUseCase: SearchProductsUseCase
) : BaseViewModel<SalesState, SalesEvent>(SalesState()) {

    init {
        loadProducts()
    }

    override fun onEvent(event: SalesEvent) {
        when (event) {
            is SalesEvent.AddToCart -> addToCart(event.product)
            is SalesEvent.RemoveFromCart -> removeFromCart(event.itemId)
            is SalesEvent.UpdateQuantity -> updateQuantity(event.itemId, event.quantity)
            is SalesEvent.SelectCustomer -> selectCustomer(event.customer)
            is SalesEvent.SelectPaymentMethod -> selectPaymentMethod(event.method)
            is SalesEvent.SearchProducts -> searchProducts(event.query)
            SalesEvent.ProcessSale -> processSale()
            SalesEvent.ClearCart -> clearCart()
        }
    }

    private fun addToCart(product: Product) {
        updateState {
            val existingItem = cartItems.find { it.product.id == product.id }
            val updatedItems = if (existingItem != null) {
                cartItems.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + 1)
                    } else it
                }
            } else {
                cartItems + CartItem(product, 1)
            }
            copy(cartItems = updatedItems).recalculateTotals()
        }
    }

    private fun processSale() {
        launchOperation {
            updateState { copy(isProcessing = true) }

            processSaleUseCase(
                items = state.value.cartItems,
                customer = state.value.selectedCustomer,
                paymentMethod = state.value.paymentMethod
            ).onSuccess {
                updateState {
                    SalesState() // Reset to initial state
                }
            }.onFailure { error ->
                updateState {
                    copy(
                        isProcessing = false,
                        error = error.message
                    )
                }
            }
        }
    }

    private fun loadProducts() {
        launchOperation {
            getProductsUseCase().collectLatest { products ->
                updateState { copy(filteredProducts = products) }
            }
        }
    }

    private fun SalesState.recalculateTotals(): SalesState {
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val tax = subtotal * 0.16 // 16% IVA
        val total = subtotal + tax
        return copy(subtotal = subtotal, tax = tax, total = total)
    }

    // ... other private methods
}
```

### Screen Composable (Pure UI)

```kotlin
// commonMain/ui/screens/sales/SalesScreen.kt
package com.vibely.pos.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SalesScreen(
    viewModel: SalesViewModel = koinViewModel(),
    onNavigateToCustomers: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    SalesScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToCustomers = onNavigateToCustomers
    )
}

@Composable
private fun SalesScreenContent(
    state: SalesState,
    onEvent: (SalesEvent) -> Unit,
    onNavigateToCustomers: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Product catalog (left side)
        ProductCatalog(
            products = state.filteredProducts,
            searchQuery = state.searchQuery,
            onSearchQueryChange = { onEvent(SalesEvent.SearchProducts(it)) },
            onProductClick = { onEvent(SalesEvent.AddToCart(it)) },
            modifier = Modifier.weight(2f)
        )

        // Shopping cart (right side)
        ShoppingCart(
            items = state.cartItems,
            customer = state.selectedCustomer,
            subtotal = state.subtotal,
            tax = state.tax,
            total = state.total,
            paymentMethod = state.paymentMethod,
            isProcessing = state.isProcessing,
            onRemoveItem = { onEvent(SalesEvent.RemoveFromCart(it)) },
            onUpdateQuantity = { id, qty ->
                onEvent(SalesEvent.UpdateQuantity(id, qty))
            },
            onSelectCustomer = onNavigateToCustomers,
            onSelectPaymentMethod = {
                onEvent(SalesEvent.SelectPaymentMethod(it))
            },
            onProcessSale = { onEvent(SalesEvent.ProcessSale) },
            onClearCart = { onEvent(SalesEvent.ClearCart) },
            modifier = Modifier.weight(1f)
        )
    }
}
```

---

## Navigation System

### Navigation Setup

```kotlin
// commonMain/ui/navigation/Navigation.kt
package com.vibely.pos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Sales : Screen("sales")
    object Inventory : Screen("inventory")
    object Customers : Screen("customers")
    object Reports : Screen("reports")
    object Settings : Screen("settings")

    // Nested routes
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    object CustomerDetail : Screen("customer/{customerId}") {
        fun createRoute(customerId: String) = "customer/$customerId"
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Sales.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Sales.route) {
            SalesScreen(
                onNavigateToCustomers = {
                    navController.navigate(Screen.Customers.route)
                }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onProductClick = { productId ->
                    navController.navigate(
                        Screen.ProductDetail.createRoute(productId)
                    )
                }
            )
        }

        composable(Screen.Customers.route) {
            CustomersScreen(
                onCustomerClick = { customerId ->
                    navController.navigate(
                        Screen.CustomerDetail.createRoute(customerId)
                    )
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        // Detail screens
        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.CustomerDetail.route) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            CustomerDetailScreen(
                customerId = customerId,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
```

### Platform-Specific Navigation

```kotlin
// commonMain/ui/navigation/PlatformNavigation.kt
package com.vibely.pos.ui.navigation

expect class PlatformNavigationHandler {
    fun handleDeepLink(url: String)
    fun canGoBack(): Boolean
    fun goBack()
}

// desktopMain/ui/navigation/PlatformNavigation.desktop.kt
actual class PlatformNavigationHandler {
    actual fun handleDeepLink(url: String) {
        // Desktop deep link handling
    }

    actual fun canGoBack(): Boolean = false

    actual fun goBack() {
        // Desktop back navigation
    }
}

// androidMain/ui/navigation/PlatformNavigation.android.kt
actual class PlatformNavigationHandler {
    actual fun handleDeepLink(url: String) {
        // Android deep link handling
    }

    actual fun canGoBack(): Boolean {
        // Check Android back stack
    }

    actual fun goBack() {
        // Android back navigation
    }
}
```

---

## Platform Adaptations

### Responsive Layout

```kotlin
// commonMain/ui/utils/WindowSizeClass.kt
package com.vibely.pos.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT,   // Mobile portrait
    MEDIUM,    // Mobile landscape, tablets
    EXPANDED   // Desktop, large tablets
}

@Composable
expect fun rememberWindowSizeClass(): WindowSizeClass

data class WindowInfo(
    val sizeClass: WindowSizeClass,
    val width: Dp,
    val height: Dp
)

fun WindowSizeClass.isCompact() = this == WindowSizeClass.COMPACT
fun WindowSizeClass.isMedium() = this == WindowSizeClass.MEDIUM
fun WindowSizeClass.isExpanded() = this == WindowSizeClass.EXPANDED
```

### Adaptive Layouts

```kotlin
// commonMain/ui/components/adaptive/AdaptiveLayout.kt
package com.vibely.pos.ui.components.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveLayout(
    windowSizeClass: WindowSizeClass,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> compactContent()
        WindowSizeClass.MEDIUM -> mediumContent()
        WindowSizeClass.EXPANDED -> expandedContent()
    }
}

// Usage in Sales Screen
@Composable
fun SalesScreenAdaptive(state: SalesState, onEvent: (SalesEvent) -> Unit) {
    val windowSize = rememberWindowSizeClass()

    AdaptiveLayout(
        windowSizeClass = windowSize,
        compactContent = {
            // Mobile: Vertical layout with tabs
            SalesScreenMobile(state, onEvent)
        },
        expandedContent = {
            // Desktop: Side-by-side layout
            SalesScreenDesktop(state, onEvent)
        }
    )
}
```

### Platform-Specific Components

```kotlin
// commonMain/ui/components/platform/PlatformComponents.kt
package com.vibely.pos.ui.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformDatePicker(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun PlatformTimePicker(
    selectedTime: Int?,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun PlatformFilePicker(
    onFileSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## Figma to Compose Mapping

### Screen Mapping Table

| Figma Screen | Compose Screen | ViewModel | Key Components |
|-------------|----------------|-----------|----------------|
| Login | `LoginScreen.kt` | `LoginViewModel` | AppTextField, PrimaryButton |
| Dashboard | `DashboardScreen.kt` | `DashboardViewModel` | SummaryCard, NavigationBar |
| Sales/POS | `SalesScreen.kt` | `SalesViewModel` | ProductCard, ShoppingCart |
| Product Catalog | `ProductCatalogScreen.kt` | `ProductViewModel` | SearchField, ProductCard, GridLayout |
| Product Detail | `ProductDetailScreen.kt` | `ProductDetailViewModel` | AppCard, ImageCarousel |
| Inventory | `InventoryScreen.kt` | `InventoryViewModel` | DataTable, FilterChips |
| Add Product | `AddProductScreen.kt` | `AddProductViewModel` | AppTextField, ImagePicker |
| Customers | `CustomersScreen.kt` | `CustomersViewModel` | SearchField, CustomerCard |
| Customer Detail | `CustomerDetailScreen.kt` | `CustomerDetailViewModel` | AppCard, TransactionHistory |
| Reports | `ReportsScreen.kt` | `ReportsViewModel` | ChartComponent, DateRangePicker |
| Settings | `SettingsScreen.kt` | `SettingsViewModel` | SettingsList, SwitchItem |

### Component Mapping

| Figma Component | Compose Component | Location |
|----------------|-------------------|----------|
| Primary Button | `PrimaryButton` | `ui/components/buttons/` |
| Secondary Button | `SecondaryButton` | `ui/components/buttons/` |
| Text Input | `AppTextField` | `ui/components/inputs/` |
| Search Bar | `SearchField` | `ui/components/inputs/` |
| Product Card | `ProductCard` | `ui/components/cards/` |
| Summary Card | `SummaryCard` | `ui/components/cards/` |
| Toast | `CustomToast` | `ui/components/feedback/` |
| Modal | `ConfirmationDialog` | `ui/components/dialogs/` |
| Navigation Bar | `BottomNavigationBar` | `ui/components/navigation/` |
| Tab Bar | `TabRow` | Material3 built-in |

---

## Best Practices

### 1. Composable Guidelines

```kotlin
// ✅ GOOD: Pure, stateless composable
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure UI rendering
}

// ❌ BAD: Business logic in composable
@Composable
fun ProductCard(product: Product) {
    val viewModel = viewModel<ProductViewModel>()

    // Don't do this!
    LaunchedEffect(product) {
        viewModel.updateProduct(product)
    }
}
```

### 2. State Management

```kotlin
// ✅ GOOD: State hoisting
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange
    )
}

// ❌ BAD: Internal state
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    TextField(
        value = query,
        onValueChange = { query = it }
    )
}
```

### 3. Performance Optimization

```kotlin
// Use remember for expensive calculations
@Composable
fun ExpensiveList(items: List<Item>) {
    val filteredItems = remember(items) {
        items.filter { it.isActive }
    }

    LazyColumn {
        items(
            items = filteredItems,
            key = { it.id } // Important for performance
        ) { item ->
            ItemRow(item)
        }
    }
}

// Use derivedStateOf for computed values
@Composable
fun CartSummary(items: List<CartItem>) {
    val total by remember {
        derivedStateOf {
            items.sumOf { it.price * it.quantity }
        }
    }

    Text("Total: $total")
}
```

### 4. Error Handling

```kotlin
// Centralized error handling in ViewModel
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// In Composable
@Composable
fun DataScreen(uiState: UiState<Data>) {
    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> DataContent(uiState.data)
        is UiState.Error -> ErrorView(uiState.message)
    }
}
```

---

## Testing Strategy

### UI Tests

```kotlin
// commonTest/ui/components/ProductCardTest.kt
@Test
fun productCard_displaysCorrectInformation() {
    composeTestRule.setContent {
        ProductCard(
            product = Product(
                id = "1",
                name = "Test Product",
                price = 100.0
            ),
            onClick = {}
        )
    }

    composeTestRule
        .onNodeWithText("Test Product")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText("$100.00")
        .assertIsDisplayed()
}
```

### ViewModel Tests

```kotlin
// commonTest/ui/viewmodels/SalesViewModelTest.kt
@Test
fun addToCart_increasesCartItemCount() = runTest {
    val viewModel = SalesViewModel(/* dependencies */)
    val product = Product(id = "1", name = "Test", price = 10.0)

    viewModel.onEvent(SalesEvent.AddToCart(product))

    val state = viewModel.state.value
    assertEquals(1, state.cartItems.size)
    assertEquals(product, state.cartItems.first().product)
}
```

---

## File Structure

```
commonMain/
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   ├── Shape.kt
│   │   └── Theme.kt
│   ├── components/
│   │   ├── buttons/
│   │   │   └── Buttons.kt
│   │   ├── inputs/
│   │   │   └── TextField.kt
│   │   ├── cards/
│   │   │   └── Cards.kt
│   │   ├── feedback/
│   │   │   ├── Toast.kt
│   │   │   └── Snackbar.kt
│   │   ├── dialogs/
│   │   │   └── Dialogs.kt
│   │   ├── navigation/
│   │   │   └── NavigationBar.kt
│   │   └── adaptive/
│   │       └── AdaptiveLayout.kt
│   ├── screens/
│   │   ├── sales/
│   │   │   ├── SalesScreen.kt
│   │   │   ├── SalesViewModel.kt
│   │   │   └── SalesState.kt
│   │   ├── inventory/
│   │   ├── customers/
│   │   ├── reports/
│   │   └── settings/
│   ├── navigation/
│   │   ├── Navigation.kt
│   │   └── PlatformNavigation.kt
│   ├── base/
│   │   └── BaseViewModel.kt
│   └── utils/
│       ├── WindowSizeClass.kt
│       └── Extensions.kt
```

---

## Summary

This UI architecture ensures:

✅ **Clean separation** - No business logic in composables
✅ **Testability** - ViewModels and UI components are independently testable
✅ **Reusability** - Shared components across all screens
✅ **Scalability** - Clear structure for adding new screens and features
✅ **Platform adaptation** - Responsive layouts for desktop/web/mobile
✅ **Maintainability** - Consistent patterns and conventions
✅ **Performance** - Optimized state management and rendering

All Figma designs will be mapped to these Compose components following the patterns defined in this document.
