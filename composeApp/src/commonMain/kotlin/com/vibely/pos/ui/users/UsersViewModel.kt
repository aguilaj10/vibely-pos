package com.vibely.pos.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.pos.shared.domain.auth.entity.User
import com.vibely.pos.shared.domain.auth.valueobject.UserRole
import com.vibely.pos.shared.domain.auth.valueobject.UserStatus
import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.user.usecase.GetAllUsersUseCase
import com.vibely.pos.shared.domain.user.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the users management screen.
 *
 * @param users List of users to display.
 * @param searchQuery Current search query.
 * @param selectedRoleFilter Optional role filter.
 * @param selectedStatusFilter Optional status filter.
 * @param isLoading True while loading data.
 * @param errorMessage Error message to display, if any.
 * @param totalUsers Total count of users.
 * @param activeUsers Count of active users.
 */
data class UsersState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedRoleFilter: UserRole? = null,
    val selectedStatusFilter: UserStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
)

/**
 * ViewModel for the users management screen.
 *
 * Handles loading, searching, and filtering users.
 *
 * @param getAllUsersUseCase Use case to fetch all users with optional filters.
 * @param searchUsersUseCase Use case to search users by query.
 */
class UsersViewModel(private val getAllUsersUseCase: GetAllUsersUseCase, private val searchUsersUseCase: SearchUsersUseCase) : ViewModel() {

    private val _state = MutableStateFlow(UsersState())
    val state: StateFlow<UsersState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadUsers()
    }

    /**
     * Loads users with current filters applied.
     */
    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val role = _state.value.selectedRoleFilter
            val status = _state.value.selectedStatusFilter

            when (val result = getAllUsersUseCase(role = role, status = status)) {
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
    }

    /**
     * Updates the search query with debouncing.
     *
     * @param query The new search query.
     */
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

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

    /**
     * Clears the current search query and reloads users.
     */
    fun onClearSearch() {
        _state.update { it.copy(searchQuery = "") }
        loadUsers()
    }

    /**
     * Updates the role filter and reloads users.
     *
     * @param role The new role filter, or null to clear.
     */
    fun onRoleFilterChange(role: UserRole?) {
        _state.update { it.copy(selectedRoleFilter = role, searchQuery = "") }
        loadUsers()
    }

    /**
     * Updates the status filter and reloads users.
     *
     * @param status The new status filter, or null to clear.
     */
    fun onStatusFilterChange(status: UserStatus?) {
        _state.update { it.copy(selectedStatusFilter = status, searchQuery = "") }
        loadUsers()
    }

    /**
     * Clears all filters and reloads users.
     */
    fun onClearFilters() {
        _state.update {
            it.copy(
                selectedRoleFilter = null,
                selectedStatusFilter = null,
                searchQuery = "",
            )
        }
        loadUsers()
    }

    /**
     * Handles delete user action (placeholder).
     *
     * @param userId The ID of the user to delete.
     */
    fun onDeleteUser(userId: String) {
        _state.update {
            it.copy(errorMessage = "Delete functionality not yet implemented for: $userId")
        }
    }

    /**
     * Handles edit user action (placeholder).
     *
     * @param userId The ID of the user to edit.
     */
    fun onEditUser(userId: String) {
        _state.update {
            it.copy(errorMessage = "Edit navigation not yet implemented for: $userId")
        }
    }

    /**
     * Handles add user action (placeholder).
     */
    fun onAddUser() {
        _state.update {
            it.copy(errorMessage = "Add user navigation not yet implemented")
        }
    }

    /**
     * Dismisses the current error message.
     */
    fun onErrorDismiss() {
        _state.update { it.copy(errorMessage = null) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
