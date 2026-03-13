package com.vibely.pos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Custom shape system for Vibely POS
 * Provides consistent corner radius across the application
 */
val AppShapes = Shapes(
    // Extra small - Chips, tags
    extraSmall = RoundedCornerShape(4.dp),

    // Small - Buttons, small cards
    small = RoundedCornerShape(8.dp),

    // Medium - Standard cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Large - Bottom sheets, large cards
    large = RoundedCornerShape(16.dp),

    // Extra large - Modal overlays
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * Additional custom shapes for POS-specific use cases
 */
object PosShapes {
    // Product cards
    val ProductCard = RoundedCornerShape(12.dp)

    // Action buttons (checkout, pay, etc.)
    val ActionButton = RoundedCornerShape(16.dp)

    // Input fields
    val InputField = RoundedCornerShape(8.dp)

    // Modal bottom sheets
    val BottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    // Toast/Snackbar
    val Toast = RoundedCornerShape(12.dp)

    // Image containers
    val ImageContainer = RoundedCornerShape(8.dp)

    // Dialog boxes
    val Dialog = RoundedCornerShape(20.dp)

    // Chip badges
    val Chip = RoundedCornerShape(100.dp) // Fully rounded

    // Top app bar (if rounded)
    val TopBar = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)

    // Navigation rail/bar (if rounded)
    val NavBar = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
