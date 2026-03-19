package com.vibely.pos.ui.suppliers

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
import com.vibely.pos.shared.domain.supplier.entity.Supplier
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.PaginationControls
import com.vibely.pos.ui.dialogs.ConfirmationDialog
import com.vibely.pos.ui.dialogs.SupplierFormData
import com.vibely.pos.ui.dialogs.SupplierFormDialog
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Store
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.Truck
import compose.icons.fontawesomeicons.solid.TruckLoading
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.suppliers_add
import vibely_pos.composeapp.generated.resources.suppliers_no_suppliers_found

@Composable
fun SuppliersScreen(
    onNavigate: (com.vibely.pos.ui.navigation.Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SuppliersViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onErrorDismiss()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSuccessMessageDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SuppliersHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onAddSupplier = viewModel::onAddSupplier,
            )

            Spacer(modifier = Modifier.height(16.dp))

            KpiCardsRow(
                totalSuppliers = state.totalSuppliers,
                activeSuppliers = state.activeSuppliers,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SuppliersTable(
                    suppliers = state.suppliers,
                    state = state,
                    viewModel = viewModel,
                    onEditSupplier = viewModel::onEditSupplier,
                    onDeleteSupplier = viewModel::onDeleteSupplier,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (state.showSupplierDialog) {
            val editingSupplier = state.editingSupplier
            SupplierFormDialog(
                isEdit = editingSupplier != null,
                initialData =
                editingSupplier?.let {
                    SupplierFormData(
                        id = it.id,
                        name = it.name,
                        contactPerson = it.contactPerson ?: "",
                        email = it.email ?: "",
                        phone = it.phone ?: "",
                        address = it.address ?: "",
                        isActive = it.isActive,
                    )
                },
                onSave = viewModel::onSaveSupplier,
                onDismiss = viewModel::onDismissSupplierDialog,
            )
        }

        if (state.showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete Supplier",
                message = "Are you sure you want to delete this supplier? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::onConfirmDelete,
                onDismiss = viewModel::onDismissDeleteDialog,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun SuppliersHeader(searchQuery: String, onSearchQueryChange: (String) -> Unit, onClearSearch: () -> Unit, onAddSupplier: () -> Unit) {
    Row(
        modifier =
        Modifier
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
                            imageVector = FontAwesomeIcons.Solid.Truck,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
        )

        AppButton(
            text = stringResource(Res.string.suppliers_add),
            onClick = onAddSupplier,
            style = AppButtonStyle.Primary,
            icon = {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.TruckLoading,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}

@Composable
private fun KpiCardsRow(totalSuppliers: Int, activeSuppliers: Int) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.Truck,
            label = "Total Suppliers",
            value = totalSuppliers.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.Store,
            label = "Active Suppliers",
            value = activeSuppliers.toString(),
            valueColor = AppColors.Success,
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
private fun SuppliersTable(
    suppliers: List<Supplier>,
    state: SuppliersState,
    viewModel: SuppliersViewModel,
    onEditSupplier: (String) -> Unit,
    onDeleteSupplier: (String) -> Unit,
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

            if (suppliers.isEmpty()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Truck,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.suppliers_no_suppliers_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(suppliers) { supplier ->
                        TableRow(
                            supplier = supplier,
                            onEdit = { onEditSupplier(supplier.id) },
                            onDelete = { onDeleteSupplier(supplier.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }

                PaginationControls(
                    paginationState = state.pagination,
                    onPreviousPage = viewModel::onPreviousPage,
                    onNextPage = viewModel::onNextPage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell("Code", modifier = Modifier.width(80.dp))
        TableHeaderCell("Name", modifier = Modifier.weight(1f))
        TableHeaderCell("Contact", modifier = Modifier.weight(1f))
        TableHeaderCell("Email", modifier = Modifier.weight(1f))
        TableHeaderCell("Phone", modifier = Modifier.width(100.dp))
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
private fun TableRow(supplier: Supplier, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(supplier.code, modifier = Modifier.width(80.dp))
        TableCell(supplier.name, modifier = Modifier.weight(1f))
        TableCell(supplier.contactPerson ?: "-", modifier = Modifier.weight(1f))
        TableCell(supplier.email ?: "-", modifier = Modifier.weight(1f))
        TableCell(supplier.phone ?: "-", modifier = Modifier.width(100.dp))

        StatusChip(isActive = supplier.isActive, modifier = Modifier.width(80.dp))

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
private fun StatusChip(isActive: Boolean, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, label) =
        if (isActive) {
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
