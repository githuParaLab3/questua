package com.questua.app.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    savedStateHandle: SavedStateHandle // Injeta os argumentos da navegação
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    val displayName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    // Pega o argumento "languageId" definido na rota no NavGraph
    private val languageId: String? = savedStateHandle["languageId"]

    fun register() {
        if (displayName.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return
        }

        if (languageId.isNullOrBlank()) {
            _state.value = _state.value.copy(error = "Erro: Idioma não selecionado. Volte e tente novamente.")
            return
        }

        viewModelScope.launch {
            registerUseCase(
                email = email.value,
                // CORREÇÃO AQUI: O parâmetro no UseCase chama-se 'name', não 'displayName'
                name = displayName.value,
                pass = password.value,
                langId = languageId
            ).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = RegisterState(isLoading = true)
                    is Resource.Success -> _state.value = RegisterState(isRegistered = true)
                    is Resource.Error -> _state.value = RegisterState(error = result.message)
                }
            }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}