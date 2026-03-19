package com.vibely.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.common.PaginationState
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronLeft
import compose.icons.fontawesomeicons.solid.ChevronRight
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.pagination_page

@Composable
fun PaginationControls(paginationState: PaginationState, onPreviousPage: () -> Unit, onNextPage: () -> Unit, modifier: Modifier = Modifier) {
    if (!paginationState.hasPreviousPage && !paginationState.hasNextPage) return

    val pageText = stringResource(Res.string.pagination_page, paginationState.currentPage)

    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPreviousPage,
            enabled = paginationState.hasPreviousPage,
            modifier =
            Modifier.semantics {
                contentDescription = "Previous page"
            },
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.ChevronLeft,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (paginationState.hasPreviousPage) AppColors.Primary else AppColors.NeutralLight400,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = pageText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
            Modifier.semantics {
                contentDescription = pageText
            },
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onNextPage,
            enabled = paginationState.hasNextPage,
            modifier =
            Modifier.semantics {
                contentDescription = "Next page"
            },
        ) {
            Icon(
                imageVector = FontAwesomeIcons.Solid.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (paginationState.hasNextPage) AppColors.Primary else AppColors.NeutralLight400,
            )
        }
    }
}
