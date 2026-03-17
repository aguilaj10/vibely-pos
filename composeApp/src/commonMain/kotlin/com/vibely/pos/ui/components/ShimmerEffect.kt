package com.vibely.pos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors

/**
 * Shimmer effect modifier that creates an animated gradient animation.
 * This is the foundation for all skeleton loading components.
 *
 * The animation moves a grayscale gradient from left to right, creating
 * a smooth shimmer effect that indicates loading state.
 *
 * @param shape The shape to apply to the shimmer effect
 * @param baseColor The base color for the skeleton (default: surface variant)
 * @param highlightColor The highlight color for the shimmer (default: neutral light)
 * @param animationDuration Duration of the shimmer animation in milliseconds (default: 1000ms)
 */
fun Modifier.shimmerEffect(
    shape: Shape,
    baseColor: Color = AppColors.NeutralLight300,
    highlightColor: Color = AppColors.NeutralLight100,
    animationDuration: Int = 1000,
): Modifier = composed {
    val shimmerColors = listOf(
        baseColor,
        highlightColor.copy(alpha = 0.5f),
        baseColor,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim),
    )

    return@composed this
        .background(brush = brush, shape = shape)
}

/**
 * Simpler shimmer modifier that uses default parameters.
 * Applies shimmer effect with rounded corners (10dp).
 */
fun Modifier.shimmer(animationDuration: Int = 1000): Modifier = composed {
    val baseColor = AppColors.NeutralLight300
    val highlightColor = AppColors.NeutralLight100

    val shimmerColors = listOf(
        baseColor,
        highlightColor.copy(alpha = 0.5f),
        baseColor,
    )

    val transition = rememberInfiniteTransition(label = "shimmer_simple")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate_simple",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim),
    )

    return@composed this.background(brush = brush)
}

/**
 * Composable that wraps content with shimmer effect.
 * Useful for creating skeleton loaders with custom content shapes.
 *
 * @param modifier Modifier for customization
 * @param isLoading Whether to show the shimmer effect
 * @param shape The shape to apply
 * @param content The content to display (or shimmer placeholder)
 */
@Composable
fun ShimmerContainer(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .shimmerEffect(shape = shape),
        )
    } else {
        Box(modifier = modifier, content = content)
    }
}

/**
 * Creates a simple rectangle shimmer placeholder.
 *
 * @param width Width of the shimmer placeholder
 * @param height Height of the shimmer placeholder
 * @param cornerRadius Corner radius for rounded corners
 */
@Composable
fun ShimmerPlaceholder(width: Dp, height: Dp, cornerRadius: Dp = 10.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width, height)
            .shimmerEffect(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
            ),
    )
}
