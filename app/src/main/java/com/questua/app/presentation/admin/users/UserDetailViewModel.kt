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
import java.io.File
import javax.inject.Inject

data class UserDetailState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val availableLanguages: List<Language> = emptyList(),
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase,
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

        viewModelScope.launch {
            getAvailableLanguagesUseCase().collect { result ->
                if (result is Resource.Success) {
                    _state.value = _state.value.copy(availableLanguages = result.data ?: emptyList())
                }
            }
        }

        viewModelScope.launch {
            getUserDetailsUseCase(userId).collect { result ->
                when(result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> _state.value = _state.value.copy(isLoading = false, user = result.data)
                    is Resource.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun updateUser(displayName: String, email: String, role: UserRole, nativeLangId: String, password: String?, avatarFile: File?) {
        val currentUser = _state.value.user ?: return
        viewModelScope.launch {
            updateUserUseCase(currentUser.id, email, displayName, nativeLangId, role, password, avatarFile).collect { result ->
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