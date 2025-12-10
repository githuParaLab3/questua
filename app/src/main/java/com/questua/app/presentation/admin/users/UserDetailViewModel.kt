package com.questua.app.presentation.admin.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.enums.UserRole
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.usecase.admin.users.DeleteUserUseCase
import com.questua.app.domain.usecase.admin.users.GetUserDetailsUseCase
import com.questua.app.domain.usecase.admin.users.UpdateUserUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDetailState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val availableLanguages: List<Language> = emptyList(), // Para lookup de códigos e edição
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase, // Injetado
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    private val _state = MutableStateFlow(UserDetailState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _state.value = _state.value.copy(isLoading = true)

        // Carrega Usuário e Idiomas em paralelo (ou sequencial simples)
        viewModelScope.launch {
            // 1. Carregar Idiomas (para termos os códigos disponíveis)
            getAvailableLanguagesUseCase().collect { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(availableLanguages = result.data ?: emptyList())
                }
                // Não paramos o loading aqui, esperamos o usuário
            }
        }

        viewModelScope.launch {
            // 2. Carregar Usuário
            getUserDetailsUseCase(userId).collect { result ->
                when(result) {
                    is Resource.Loading -> {} // Já setamos loading no início
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, user = result.data)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun updateUser(displayName: String, email: String, role: UserRole, nativeLangId: String, password: String?) {
        val currentUser = _state.value.user ?: return
        viewModelScope.launch {
            updateUserUseCase(currentUser.id, email, displayName, nativeLangId, role, password).collect { result ->
                when(result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, user = result.data)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            deleteUserUseCase(userId).collect { result ->
                when(result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, deleteSuccess = true)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}