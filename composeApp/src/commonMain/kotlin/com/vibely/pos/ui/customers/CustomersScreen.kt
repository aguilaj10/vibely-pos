package com.vibely.pos.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.customer.entity.Customer
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Store
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserPlus
import org.koin.compose.koinInject

@Composable
fun CustomersScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomersViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CustomersHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onAddCustomer = viewModel::onAddCustomer,
            )

            Spacer(modifier = Modifier.height(16.dp))

            KpiCardsRow(
                totalCustomers = state.totalCustomers,
                activeCustomers = state.activeCustomers,
                totalLoyaltyPoints = state.totalLoyaltyPoints,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                CustomersTable(
                    customers = state.customers,
                    onEditCustomer = viewModel::onEditCustomer,
                    onDeleteCustomer = viewModel::onDeleteCustomer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun CustomersHeader(searchQuery: String, onSearchQueryChange: (String) -> Unit, onClearSearch: () -> Unit, onAddCustomer: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.width(480.dp),
            variant = AppTextFieldVariant.Outlined,
            placeholder = "Search by name, email, or phone...",
            leadingIcon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.User,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
        )

        AppButton(
            text = "Add Customer",
            onClick = onAddCustomer,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.UserPlus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun KpiCardsRow(totalCustomers: Int, activeCustomers: Int, totalLoyaltyPoints: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.User,
            label = "Total Customers",
            value = totalCustomers.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.Store,
            label = "Active Customers",
            value = activeCustomers.toString(),
            valueColor = AppColors.Success,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.UserPlus,
            label = "Total Loyalty Points",
            value = formatNumber(totalLoyaltyPoints),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KpiCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        style = AppCardStyle.Elevated,
        elevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = valueColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CustomersTable(
    customers: List<Customer>,
    onEditCustomer: (String) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.padding(horizontal = 16.dp),
        style = AppCardStyle.Elevated,
        elevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TableHeader()

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.User,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No customers found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(customers) { customer ->
                        TableRow(
                            customer = customer,
                            onEdit = { onEditCustomer(customer.id) },
                            onDelete = { onDeleteCustomer(customer.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell("Code", modifier = Modifier.width(80.dp))
        TableHeaderCell("Name", modifier = Modifier.weight(1f))
        TableHeaderCell("Email", modifier = Modifier.weight(1f))
        TableHeaderCell("Phone", modifier = Modifier.width(100.dp))
        TableHeaderCell("Points", modifier = Modifier.width(80.dp))
        TableHeaderCell("Tier", modifier = Modifier.width(80.dp))
        TableHeaderCell("Status", modifier = Modifier.width(80.dp))
        TableHeaderCell("Actions", modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun TableRow(customer: Customer, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(customer.code, modifier = Modifier.width(80.dp))
        TableCell(customer.fullName, modifier = Modifier.weight(1f))
        TableCell(customer.email ?: "-", modifier = Modifier.weight(1f))
        TableCell(customer.phone ?: "-", modifier = Modifier.width(100.dp))
        TableCell(customer.loyaltyPoints.toString(), modifier = Modifier.width(80.dp))

        LoyaltyTierChip(tier = customer.loyaltyTier, modifier = Modifier.width(80.dp))

        StatusChip(isActive = customer.isActive, modifier = Modifier.width(80.dp))

        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Trash,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.ErrorDark,
                )
            }
        }
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun LoyaltyTierChip(tier: String?, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) = when (tier) {
        "Platinum" -> Triple(AppColors.NeutralLight800, Color.White, "Platinum")
        "Gold" -> Triple(Color(0xFFFFD700), Color.Black, "Gold")
        "Silver" -> Triple(AppColors.NeutralLight400, Color.White, "Silver")
        "Bronze" -> Triple(Color(0xFFCD7F32), Color.White, "Bronze")
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "-")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatusChip(isActive: Boolean, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) = if (isActive) {
        Triple(AppColors.Success, Color.White, "Active")
    } else {
        Triple(AppColors.ErrorDark, Color.White, "Inactive")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

private fun formatNumber(number: Int): String = when {
    number >= 1_000_000 -> "${number / 1_000_000.0}M"
    number >= 1_000 -> "${number / 1_000.0}K"
    else -> number.toString()
}
