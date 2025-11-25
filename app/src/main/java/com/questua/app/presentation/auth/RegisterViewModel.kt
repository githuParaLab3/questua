package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.model.Language
import com.questua.app.domain.usecase.auth.RegisterUseCase
import com.questua.app.domain.usecase.onboarding.GetAvailableLanguagesUseCase
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
    val error: String? = null,
    val languages: List<Language> = emptyList()
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val getLanguagesUseCase: GetAvailableLanguagesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    val displayName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val selectedLanguageId = MutableStateFlow("")

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            getLanguagesUseCase().onEach { result ->
                when(result) {
                    is Resource.Success -> _state.value = _state.value.copy(languages = result.data ?: emptyList())
                    is Resource.Error -> _state.value = _state.value.copy(error = result.message)
                    else -> {}
                }
            }.launchIn(this)
        }
    }

    fun register() {
        if (displayName.value.isBlank() || email.value.isBlank() || password.value.isBlank() || selectedLanguageId.value.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos e selecione um idioma")
            return
        }

        viewModelScope.launch {
            registerUseCase(
                email.value,
                displayName.value,
                password.value,
                selectedLanguageId.value
            ).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = _state.value.copy(isLoading = true, error = null)
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, isRegistered = true)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}