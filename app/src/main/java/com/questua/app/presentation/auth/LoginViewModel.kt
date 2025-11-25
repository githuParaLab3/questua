package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado definido AQUI DENTRO para não criar arquivo novo
data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Gerenciamento de Estado Único (Padrão Google para Compose)
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    // Two-way binding simples para os inputs
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun login() {
        val currentEmail = email.value
        val currentPassword = password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            repository.login(currentEmail, currentPassword).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message ?: "Erro desconhecido")
                    }
                }
            }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}