package com.vibely.pos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Custom shape system for Vibely POS
 * Provides consistent corner radius across the application
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

/**
 * Additional custom shapes for POS-specific use cases
 */
object PosShapes {
    val ProductCard = RoundedCornerShape(10.dp)
    val ActionButton = RoundedCornerShape(8.dp)
    val InputField = RoundedCornerShape(8.dp)
    val BottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val Toast = RoundedCornerShape(8.dp)
    val ImageContainer = RoundedCornerShape(8.dp)
    val Dialog = RoundedCornerShape(16.dp)
    val Chip = RoundedCornerShape(100.dp)
    val TopBar = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    val NavBar = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
}
