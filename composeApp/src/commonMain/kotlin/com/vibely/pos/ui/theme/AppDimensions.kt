package com.vibely.pos.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Screen-width threshold below which the compact (phone) layout is applied.
 * Matches the navigation breakpoint in AppNavigation.
 */
val COMPACT_WIDTH_THRESHOLD: Dp = 600.dp

/**
 * Responsive spacing and sizing tokens for Vibely POS.
 *
 * Two instances exist — [CompactDimensions] for phones (< 600dp) and
 * [ExpandedDimensions] for tablets and desktop (≥ 600dp).
 * Consume via [LocalAppDimensions].
 *
 * @param spacingXs Extra-small gap (4dp) — icon badges, tight label pairs.
 * @param spacingSm Small gap (8dp) — related items within a group.
 * @param spacingMd Medium gap — between form fields and list items.
 * @param spacingLg Large gap — before action buttons, between card sections.
 * @param spacingXl Extra-large gap — between major form sections.
 * @param screenPadding Horizontal outer padding applied to screen/card containers.
 * @param cardPadding Internal padding inside cards.
 */
data class AppDimensions(
    val spacingXs: Dp,
    val spacingSm: Dp,
    val spacingMd: Dp,
    val spacingLg: Dp,
    val spacingXl: Dp,
    val screenPadding: Dp,
    val cardPadding: Dp,
)

/** Compact dimensions for phone screens (width < 600dp). */
val CompactDimensions = AppDimensions(
    spacingXs = 4.dp,
    spacingSm = 8.dp,
    spacingMd = 12.dp,
    spacingLg = 16.dp,
    spacingXl = 24.dp,
    screenPadding = 16.dp,
    cardPadding = 20.dp,
)

/** Expanded dimensions for tablet and desktop screens (width ≥ 600dp). */
val ExpandedDimensions = AppDimensions(
    spacingXs = 4.dp,
    spacingSm = 8.dp,
    spacingMd = 16.dp,
    spacingLg = 24.dp,
    spacingXl = 32.dp,
    screenPadding = 32.dp,
    cardPadding = 32.dp,
)

/** Provides the current [AppDimensions] down the composition tree. */
val LocalAppDimensions = staticCompositionLocalOf { ExpandedDimensions }
