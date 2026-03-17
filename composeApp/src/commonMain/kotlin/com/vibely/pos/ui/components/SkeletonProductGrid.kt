package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle

/**
 * A single product card skeleton with image placeholder and text lines.
 * Used in checkout product grid.
 *
 * @param modifier Modifier for customization
 * @param imageHeight Height of the image placeholder
 */
@Composable
fun SkeletonProductCard(modifier: Modifier = Modifier, imageHeight: Dp = 120.dp) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        ShimmerPlaceholder(
            width = 200.dp,
            height = imageHeight,
            cornerRadius = 10.dp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ShimmerPlaceholder(
            width = 120.dp,
            height = 16.dp,
            cornerRadius = 4.dp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        ShimmerPlaceholder(
            width = 80.dp,
            height = 20.dp,
            cornerRadius = 4.dp,
        )
    }
}

/**
 * Grid layout of product card skeletons.
 * Use case: Checkout product grid loading state.
 *
 * @param itemCount Number of product skeletons to show
 * @param columns Number of columns in the grid
 * @param contentPadding Padding around the grid
 * @param imageHeight Height of image placeholders
 */
@Composable
fun SkeletonProductGrid(
    itemCount: Int = 12,
    columns: Int = 4,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    imageHeight: Dp = 120.dp,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(itemCount) {
            SkeletonProductCard(imageHeight = imageHeight)
        }
    }
}

/**
 * Single product card skeleton matching exact ProductCard dimensions.
 */
@Composable
fun SkeletonCheckoutProductCard(modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            ShimmerPlaceholder(
                width = 150.dp,
                height = 100.dp,
                cornerRadius = 8.dp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            ShimmerPlaceholder(
                width = 100.dp,
                height = 18.dp,
                cornerRadius = 4.dp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            ShimmerPlaceholder(
                width = 70.dp,
                height = 22.dp,
                cornerRadius = 4.dp,
            )
        }
    }
}

/**
 * Grid of checkout product card skeletons.
 *
 * @param itemCount Number of skeletons
 * @param columns Number of columns
 */
@Composable
fun SkeletonCheckoutProductGrid(itemCount: Int = 12, columns: Int = 4, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(itemCount) {
            SkeletonCheckoutProductCard()
        }
    }
}
