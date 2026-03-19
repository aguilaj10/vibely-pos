package com.vibely.pos.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonSize
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.dashboard.QuickAction
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.BoxOpen
import compose.icons.fontawesomeicons.solid.ChartBar
import compose.icons.fontawesomeicons.solid.ShoppingCart
import org.jetbrains.compose.resources.stringResource
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.quick_action_inventory
import vibely_pos.composeapp.generated.resources.quick_action_new_sale
import vibely_pos.composeapp.generated.resources.quick_action_reports

/**
 * Quick action buttons component for dashboard.
 *
 * Provides convenient access to common POS operations:
 * - New Sale: Start a new checkout transaction
 * - Inventory: View and manage inventory
 * - Reports: View sales and business reports
 *
 * @param onActionClick Callback when a quick action is clicked.
 * @param modifier Optional modifier for customization.
 */
@Composable
fun QuickActionButtons(onActionClick: (QuickAction) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // New Sale button
        AppButton(
            text = stringResource(Res.string.quick_action_new_sale),
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            onClick = { onActionClick(QuickAction.NEW_SALE) },
            style = AppButtonStyle.Primary,
            size = AppButtonSize.Medium,
            modifier = Modifier.weight(1f),
        )

        // Inventory button
        AppButton(
            text = stringResource(Res.string.quick_action_inventory),
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.BoxOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            onClick = { onActionClick(QuickAction.INVENTORY) },
            style = AppButtonStyle.Primary,
            size = AppButtonSize.Medium,
            modifier = Modifier.weight(1f),
        )

        // Reports button
        AppButton(
            text = stringResource(Res.string.quick_action_reports),
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ChartBar,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            onClick = { onActionClick(QuickAction.REPORTS) },
            style = AppButtonStyle.Primary,
            size = AppButtonSize.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}
