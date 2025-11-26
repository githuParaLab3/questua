package com.questua.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.usecase.auth.LogoutUseCase
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.ToggleAdminModeUseCase
import com.questua.app.domain.usecase.user.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val isEditing: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val toggleAdminModeUseCase: ToggleAdminModeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    // Campos de edição
    val editName = MutableStateFlow("")
    val editEmail = MutableStateFlow("")

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val userId = tokenManager.userId.collectLatest { id ->
                if (!id.isNullOrEmpty()) {
                    fetchUserData(id)
                }
            }
        }
    }

    private fun fetchUserData(userId: String) {
        getUserProfileUseCase(userId).onEach { result ->
            when(result) {
                is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, user = result.data)
                    // Inicializa campos de edição
                    editName.value = result.data?.displayName ?: ""
                    editEmail.value = result.data?.email ?: ""
                }
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun toggleEditMode() {
        val current = _state.value.isEditing
        if (current) {
            // Salvar alterações (se houver validação, fazer antes)
            saveChanges()
        } else {
            _state.value = _state.value.copy(isEditing = true)
        }
    }

    private fun saveChanges() {
        val user = _state.value.user ?: return
        val updatedUser = user.copy(
            displayName = editName.value,
            email = editEmail.value
        )

        viewModelScope.launch {
            updateUserProfileUseCase(updatedUser).collect { result ->
                when(result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isEditing = false, user = result.data)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun toggleAdminMode() {
        val user = _state.value.user ?: return
        // Lógica simples de toggle (se já é admin, vira user e vice-versa, ou apenas ativa funcionalidades)
        // Aqui assumimos que a UI só mostra se ele TIVER permissão, e o botão ativa/desativa o "Modo"
        // Mas se a intenção é dar permissão de admin (debug), usamos o usecase:
        val newStatus = user.role != UserRole.ADMIN

        viewModelScope.launch {
            toggleAdminModeUseCase(user.id, newStatus).collect { result ->
                if (result is Resource.Success) {
                    fetchUserData(user.id) // Recarrega para atualizar role
                }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(notificationsEnabled = enabled)
        // Persistir preferência localmente se necessário
    }

    fun toggleTheme(enabled: Boolean) {
        _state.value = _state.value.copy(darkThemeEnabled = enabled)
        // Persistir preferência
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase().collect {
                onLogoutSuccess()
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}