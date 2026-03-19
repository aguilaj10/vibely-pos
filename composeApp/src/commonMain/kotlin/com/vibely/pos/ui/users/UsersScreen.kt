package com.vibely.pos.ui.users

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.util.FormatUtils
import com.vibely.pos.ui.components.AppButton
import com.vibely.pos.ui.components.AppButtonStyle
import com.vibely.pos.ui.components.AppCard
import com.vibely.pos.ui.components.AppCardStyle
import com.vibely.pos.ui.components.AppTextField
import com.vibely.pos.ui.components.AppTextFieldVariant
import com.vibely.pos.ui.components.PaginationControls
import com.vibely.pos.ui.dialogs.ConfirmationDialog
import com.vibely.pos.ui.dialogs.UserFormData
import com.vibely.pos.ui.dialogs.UserFormDialog
import com.vibely.pos.ui.navigation.Screen
import com.vibely.pos.ui.theme.AppColors
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronDown
import compose.icons.fontawesomeicons.solid.Edit
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Times
import compose.icons.fontawesomeicons.solid.Trash
import compose.icons.fontawesomeicons.solid.UserCheck
import compose.icons.fontawesomeicons.solid.UserPlus
import compose.icons.fontawesomeicons.solid.Users
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import vibely_pos.composeapp.generated.resources.Res
import vibely_pos.composeapp.generated.resources.filter_all_roles
import vibely_pos.composeapp.generated.resources.filter_all_statuses
import vibely_pos.composeapp.generated.resources.filter_clear_filters
import vibely_pos.composeapp.generated.resources.users_add
import vibely_pos.composeapp.generated.resources.users_kpi_active_users
import vibely_pos.composeapp.generated.resources.users_kpi_total_users
import vibely_pos.composeapp.generated.resources.users_no_users_found
import vibely_pos.composeapp.generated.resources.users_search_placeholder

@Composable
fun UsersScreen(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier, viewModel: UsersViewModel = koinInject()) {
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
            UsersHeader(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onClearSearch = viewModel::onClearSearch,
                onAddUser = viewModel::onAddUser,
                selectedRole = state.selectedRoleFilter,
                onRoleChange = viewModel::onRoleFilterChange,
                selectedStatus = state.selectedStatusFilter,
                onStatusChange = viewModel::onStatusFilterChange,
                onClearFilters = viewModel::onClearFilters,
            )

            Spacer(modifier = Modifier.height(16.dp))

            KpiCardsRow(
                totalUsers = state.totalUsers,
                activeUsers = state.activeUsers,
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
                UsersTable(
                    users = state.users,
                    state = state,
                    viewModel = viewModel,
                    onEditUser = viewModel::onEditUser,
                    onDeleteUser = viewModel::onDeleteUser,
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

        if (state.showUserDialog) {
            val editingUser = state.editingUser
            UserFormDialog(
                isEdit = editingUser != null,
                initialData =
                editingUser?.let {
                    UserFormData(
                        id = it.id,
                        email = it.email.value,
                        fullName = it.fullName,
                        role = it.role,
                        status = it.status,
                    )
                },
                onSave = viewModel::onSaveUser,
                onDismiss = viewModel::onDismissUserDialog,
            )
        }

        if (state.showDeleteDialog) {
            ConfirmationDialog(
                title = "Delete User",
                message = "Are you sure you want to delete this user? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::onConfirmDelete,
                onDismiss = viewModel::onDismissDeleteDialog,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun UsersHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddUser: () -> Unit,
    selectedRole: UserRole?,
    onRoleChange: (UserRole?) -> Unit,
    selectedStatus: UserStatus?,
    onStatusChange: (UserStatus?) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.width(480.dp),
                variant = AppTextFieldVariant.Outlined,
                placeholder = stringResource(Res.string.users_search_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Times,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                singleLine = true,
            )

            AppButton(
                text = stringResource(Res.string.users_add),
                onClick = onAddUser,
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoleFilterDropdown(
                selectedRole = selectedRole,
                onRoleChange = onRoleChange,
            )

            StatusFilterDropdown(
                selectedStatus = selectedStatus,
                onStatusChange = onStatusChange,
            )

            if (selectedRole != null || selectedStatus != null) {
                AppButton(
                    text = stringResource(Res.string.filter_clear_filters),
                    onClick = onClearFilters,
                    style = AppButtonStyle.Text,
                    icon = {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Times,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RoleFilterDropdown(selectedRole: UserRole?, onRoleChange: (UserRole?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedRole?.displayName() ?: stringResource(Res.string.filter_all_roles),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.filter_all_roles)) },
                onClick = {
                    onRoleChange(null)
                    expanded = false
                },
            )
            UserRole.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role.displayName()) },
                    onClick = {
                        onRoleChange(role)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusFilterDropdown(selectedStatus: UserStatus?, onStatusChange: (UserStatus?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                    selectedStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                        ?: stringResource(Res.string.filter_all_statuses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.filter_all_statuses)) },
                onClick = {
                    onStatusChange(null)
                    expanded = false
                },
            )
            UserStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun KpiCardsRow(totalUsers: Int, activeUsers: Int) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KpiCard(
            icon = FontAwesomeIcons.Solid.Users,
            label = stringResource(Res.string.users_kpi_total_users),
            value = totalUsers.toString(),
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        KpiCard(
            icon = FontAwesomeIcons.Solid.UserCheck,
            label = stringResource(Res.string.users_kpi_active_users),
            value = activeUsers.toString(),
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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun UsersTable(
    users: List<User>,
    state: UsersState,
    viewModel: UsersViewModel,
    onEditUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
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

            if (users.isEmpty()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Users,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.users_no_users_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(users) { user ->
                        TableRow(
                            user = user,
                            onEdit = { onEditUser(user.id) },
                            onDelete = { onDeleteUser(user.id) },
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
        TableHeaderCell("Name", modifier = Modifier.weight(1f))
        TableHeaderCell("Email", modifier = Modifier.weight(1f))
        TableHeaderCell("Role", modifier = Modifier.width(140.dp))
        TableHeaderCell("Status", modifier = Modifier.width(100.dp))
        TableHeaderCell("Last Login", modifier = Modifier.width(120.dp))
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
private fun TableRow(user: User, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell(user.fullName, modifier = Modifier.weight(1f))
        TableCell(user.email.value, modifier = Modifier.weight(1f))

        RoleChip(role = user.role, modifier = Modifier.width(140.dp))
        StatusChip(status = user.status, modifier = Modifier.width(100.dp))

        TableCell(
            text = user.lastLoginAt?.let { FormatUtils.formatDateTime(it) } ?: "Never",
            modifier = Modifier.width(120.dp),
        )

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
private fun RoleChip(role: UserRole, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) =
        when (role) {
            UserRole.ADMIN -> Color(0xFF7C3AED) to Color.White
            UserRole.MANAGER -> Color(0xFF3B82F6) to Color.White
            UserRole.CASHIER -> Color(0xFF14B8A6) to Color.White
            UserRole.WAREHOUSE -> Color(0xFFF97316) to Color.White
            UserRole.VIEWER -> Color(0xFF6B7280) to Color.White
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = role.displayName(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatusChip(status: UserStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) =
        when (status) {
            UserStatus.ACTIVE -> AppColors.Success to Color.White
            UserStatus.INACTIVE -> Color(0xFF6B7280) to Color.White
            UserStatus.SUSPENDED -> AppColors.ErrorDark to Color.White
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
