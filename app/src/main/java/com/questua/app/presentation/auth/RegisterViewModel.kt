package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import com.questua.app.domain.repository.AuthRepository
import com.questua.app.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado definido internamente
data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false,
    val availableLanguages: List<Language> = emptyList()
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val languageRepository: LanguageRepository // Injeção do Repositório de Idiomas
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    // Inputs da tela
    val displayName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val selectedLanguageId = MutableStateFlow("")

    init {
        fetchLanguages()
    }

    private fun fetchLanguages() {
        viewModelScope.launch {
            languageRepository.getAvailableLanguages().onEach { result ->
                when(result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            availableLanguages = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Falha ao carregar idiomas: ${result.message}"
                        )
                    }
                    else -> {} // Loading silencioso ou pode adicionar isLoading no state
                }
            }.launchIn(this)
        }
    }

    fun register() {
        if (!validateInput()) return

        viewModelScope.launch {
            authRepository.register(
                email = email.value,
                password = password.value,
                displayName = displayName.value,
                nativeLanguageId = selectedLanguageId.value
            ).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isRegistered = true)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }.launchIn(this)
        }
    }

    private fun validateInput(): Boolean {
        if (displayName.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return false
        }
        if (selectedLanguageId.value.isBlank()) {
            _state.value = _state.value.copy(error = "Selecione seu idioma nativo")
            return false
        }
        return true
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}