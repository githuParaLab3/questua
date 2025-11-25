package com.questua.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            _state.value = _state.value.copy(error = "Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            loginUseCase(email.value, password.value).onEach { result ->
                when (result) {
                    is Resource.Loading -> _state.value = LoginState(isLoading = true)
                    is Resource.Success -> _state.value = LoginState(isLoggedIn = true)
                    is Resource.Error -> _state.value = LoginState(error = result.message)
                }
            }.launchIn(this)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}