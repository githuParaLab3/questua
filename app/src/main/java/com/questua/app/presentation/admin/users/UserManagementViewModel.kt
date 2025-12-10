package com.questua.app.presentation.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.usecase.admin.users.CreateUserUseCase
import com.questua.app.domain.usecase.admin.users.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementState(
    val isLoading: Boolean = false,
    val users: List<UserAccount> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val roleFilter: UserRole? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserManagementState())
    val state = _state.asStateFlow()

    private var allUsersCache: List<UserAccount> = emptyList()

    init {
        loadUsers()
    }

    fun loadUsers() {
        getAllUsersUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                is Resource.Success -> {
                    allUsersCache = result.data ?: emptyList()
                    applyFilters()
                }
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onRoleFilterChange(role: UserRole?) {
        val newRole = if (_state.value.roleFilter == role) null else role
        _state.value = _state.value.copy(roleFilter = newRole)
        applyFilters()
    }

    private fun applyFilters() {
        val query = _state.value.searchQuery.trim().lowercase()
        val role = _state.value.roleFilter

        val filtered = allUsersCache.filter { user ->
            val matchesQuery = if (query.isEmpty()) true else {
                user.displayName.lowercase().contains(query) ||
                        user.email.lowercase().contains(query) ||
                        user.id.lowercase().contains(query)
            }
            val matchesRole = role == null || user.role == role
            matchesQuery && matchesRole
        }

        _state.value = _state.value.copy(isLoading = false, users = filtered)
    }

    fun createUser(name: String, email: String, pass: String, langId: String, role: UserRole) {
        viewModelScope.launch {
            createUserUseCase(email, name, pass, langId, role).collect { result ->
                when(result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        loadUsers() // Recarrega lista completa
                    }
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}