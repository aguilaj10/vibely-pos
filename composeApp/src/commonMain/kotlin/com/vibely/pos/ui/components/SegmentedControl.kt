package com.vibely.pos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.theme.AppColors

data class SegmentedSegment(val label: String, val enabled: Boolean = true)

@Composable
fun SegmentedControl(segments: List<SegmentedSegment>, selectedIndex: Int, onSegmentSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.NeutralLight100)
            .border(1.dp, AppColors.OutlineLight, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        segments.forEachIndexed { index, segment ->
            SegmentedButton(
                label = segment.label,
                selected = index == selectedIndex,
                enabled = segment.enabled,
                onClick = { onSegmentSelected(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentedButton(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AppColors.SurfaceLight else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "segmentBg",
    )

    val elevation by animateDpAsState(
        targetValue = if (selected) 1.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "segmentElevation",
    )

    val textColor = when {
        !enabled -> AppColors.TextDisabledLight
        selected -> AppColors.Primary
        else -> AppColors.TextSecondaryLight
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = AppColors.OutlineLight,
                        shape = RoundedCornerShape(6.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Composable
fun SegmentedControlWithContent(
    segments: List<SegmentedSegment>,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    SegmentedControl(
        segments = segments,
        selectedIndex = selectedIndex,
        onSegmentSelected = onSegmentSelected,
        modifier = modifier,
    )
    content(selectedIndex)
}
