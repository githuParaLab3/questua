package com.questua.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.core.ui.theme.ThemeManager
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
import java.io.File
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
    private val tokenManager: TokenManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    // Campos de edição
    val editName = MutableStateFlow("")
    val editEmail = MutableStateFlow("")
    val newPassword = MutableStateFlow("") // Novo campo
    val selectedAvatarUri = MutableStateFlow<String?>(null) // Novo campo para Preview

    private var selectedAvatarFile: File? = null // Arquivo real para upload

    init {
        loadProfile()
        // Sincronizar Switch de Tema com Estado Global
        viewModelScope.launch {
            themeManager.isDarkTheme.collect { isDark ->
                _state.value = _state.value.copy(darkThemeEnabled = isDark)
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { id ->
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
                    // Preenche campos iniciais
                    editName.value = result.data?.displayName ?: ""
                    editEmail.value = result.data?.email ?: ""
                }
                is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun onImageSelected(file: File, uriString: String) {
        selectedAvatarFile = file
        selectedAvatarUri.value = uriString
    }

    fun toggleEditMode() {
        val current = _state.value.isEditing
        if (current) {
            saveChanges()
        } else {
            _state.value = _state.value.copy(isEditing = true)
        }
    }

    private fun saveChanges() {
        val user = _state.value.user ?: return

        val updatedUser = user.copy(
            displayName = editName.value,
            // Não alteramos email aqui por segurança, a menos que o backend suporte
        )

        val passwordToSend = newPassword.value.ifBlank { null }

        viewModelScope.launch {
            // Chama o UseCase atualizado (que suporta password e file)
            updateUserProfileUseCase(
                user = updatedUser,
                password = passwordToSend,
                avatarFile = selectedAvatarFile
            ).collect { result ->
                when(result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isEditing = false, user = result.data)
                        // Limpa campos sensíveis/temporários
                        newPassword.value = ""
                        selectedAvatarFile = null
                        selectedAvatarUri.value = null
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
        val newStatus = user.role != UserRole.ADMIN

        viewModelScope.launch {
            toggleAdminModeUseCase(user.id, newStatus).collect { result ->
                if (result is Resource.Success) {
                    fetchUserData(user.id)
                }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(notificationsEnabled = enabled)
    }

    fun toggleTheme(enabled: Boolean) {
        themeManager.toggleTheme(enabled)
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