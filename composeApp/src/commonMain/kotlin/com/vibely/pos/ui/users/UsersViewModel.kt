package com.vibely.pos.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.usecase.CreateUserUseCase
import com.vibely.pos.shared.domain.user.usecase.DeleteUserUseCase
import com.vibely.pos.shared.domain.user.usecase.GetAllUsersUseCase
import com.vibely.pos.shared.domain.user.usecase.SearchUsersUseCase
import com.vibely.pos.shared.domain.user.usecase.UpdateUserUseCase
import com.vibely.pos.ui.common.PaginatedResult
import com.vibely.pos.ui.common.PaginationState
import com.vibely.pos.ui.dialogs.UserFormData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: UserRole? = null,
    val selectedStatusFilter: UserStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val showUserDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val editingUser: User? = null,
    val deletingUserId: String? = null,
    val pagination: PaginationState = PaginationState(),
)

class UsersViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersState())
    val state: StateFlow<UsersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val role = _state.value.selectedRoleFilter
            val status = _state.value.selectedStatusFilter
            val currentPagination = _state.value.pagination

            when (
                val result = getAllUsersUseCase(
                    role = role,
                    status = status,
                    page = currentPagination.currentPage,
                    pageSize = currentPagination.pageSize,
                )
            ) {
                is Result.Success -> {
                    val users = result.data
                    val paginatedResult = PaginatedResult.from(users, currentPagination.pageSize)

                    _state.update {
                        it.copy(
                            users = users,
                            isLoading = false,
                            totalUsers = users.size,
                            activeUsers = users.count { u -> u.status == UserStatus.ACTIVE },
                            pagination = currentPagination.withHasMore(paginatedResult.hasMore),
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query, pagination = it.pagination.reset()) }

        searchJob?.cancel()
        if (query.isBlank()) {
            loadUsers()
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchUsers(query)
        }
    }

    private suspend fun searchUsers(query: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        when (val result = searchUsersUseCase(query)) {
            is Result.Success -> {
                val users = result.data
                _state.update {
                    it.copy(
                        users = users,
                        isLoading = false,
                        totalUsers = users.size,
                        activeUsers = users.count { u -> u.status == UserStatus.ACTIVE },
                    )
                }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "", pagination = it.pagination.reset()) }
        loadUsers()
    }

    fun onRoleFilterChange(role: UserRole?) {
        _state.update { it.copy(selectedRoleFilter = role, searchQuery = "", pagination = it.pagination.reset()) }
        loadUsers()
    }

    fun onStatusFilterChange(status: UserStatus?) {
        _state.update { it.copy(selectedStatusFilter = status, searchQuery = "", pagination = it.pagination.reset()) }
        loadUsers()
    }

    fun onClearFilters() {
        _state.update {
            it.copy(
                selectedRoleFilter = null,
                selectedStatusFilter = null,
                searchQuery = "",
                pagination = it.pagination.reset(),
            )
        }
        loadUsers()
    }

    fun onAddUser() {
        _state.update { it.copy(showUserDialog = true, editingUser = null) }
    }

    fun onEditUser(userId: String) {
        val user = _state.value.users.find { it.id == userId }
        _state.update { it.copy(showUserDialog = true, editingUser = user) }
    }

    fun onDeleteUser(userId: String) {
        _state.update { it.copy(showDeleteDialog = true, deletingUserId = userId) }
    }

    fun onDismissUserDialog() {
        _state.update { it.copy(showUserDialog = false, editingUser = null) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, deletingUserId = null) }
    }

    fun onSaveUser(formData: UserFormData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = if (formData.id.isBlank()) {
                createUserUseCase(formData.email, formData.password, formData.fullName, formData.role)
            } else {
                val existingUser = _state.value.users.find { it.id == formData.id }
                val updatedUser = existingUser?.copy(
                    fullName = formData.fullName,
                    role = formData.role,
                    status = formData.status,
                )
                if (updatedUser != null) {
                    updateUserUseCase(updatedUser)
                } else {
                    Result.Error(message = "User not found", code = "NOT_FOUND")
                }
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showUserDialog = false,
                            editingUser = null,
                            successMessage = if (formData.id.isBlank()) "User created successfully" else "User updated successfully",
                        )
                    }
                    loadUsers()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onConfirmDelete() {
        val userId = _state.value.deletingUserId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }

            when (val result = deleteUserUseCase(userId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingUserId = null,
                            successMessage = "User deleted successfully",
                        )
                    }
                    loadUsers()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            deletingUserId = null,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSuccessMessageDismiss() {
        _state.update { it.copy(successMessage = null) }
    }

    fun onNextPage() {
        _state.update { it.copy(pagination = it.pagination.nextPage()) }
        loadUsers()
    }

    fun onPreviousPage() {
        _state.update { it.copy(pagination = it.pagination.previousPage()) }
        loadUsers()
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
