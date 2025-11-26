package com.questua.app.presentation.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.network.TokenManager
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserAccount
import com.questua.app.domain.model.UserLanguage
import com.questua.app.domain.usecase.onboarding.GetLanguageDetailsUseCase // Importe o novo UseCase
import com.questua.app.domain.usecase.user.GetUserProfileUseCase
import com.questua.app.domain.usecase.user.GetUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubState(
    val isLoading: Boolean = false,
    val user: UserAccount? = null,
    val activeLanguage: UserLanguage? = null,
    val currentLanguageDetails: Language? = null, // <--- Novo campo para os detalhes visuais
    val error: String? = null
)

@HiltViewModel
class HubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getLanguageDetailsUseCase: GetLanguageDetailsUseCase, // <--- Injeção
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(HubState())
    val state = _state.asStateFlow()

    init {
        observeUserSession()
    }

    private fun observeUserSession() {
        viewModelScope.launch {
            tokenManager.userId.collectLatest { userId ->
                if (!userId.isNullOrEmpty()) {
                    loadHubData(userId)
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadHubData(userId: String) {
        _state.value = _state.value.copy(isLoading = true)

        // 1. Perfil
        getUserProfileUseCase(userId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(user = result.data)
            }
        }.launchIn(viewModelScope)

        // 2. Estatísticas + Detalhes do Idioma
        getUserStatsUseCase(userId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val userLang = result.data!!
                    _state.value = _state.value.copy(activeLanguage = userLang, isLoading = false)

                    // <--- O PULO DO GATO: Busca os detalhes assim que temos o ID
                    fetchLanguageDetails(userLang.languageId)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> {
                    // Opcional: manter loading
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchLanguageDetails(languageId: String) {
        getLanguageDetailsUseCase(languageId).onEach { result ->
            if (result is Resource.Success) {
                _state.value = _state.value.copy(currentLanguageDetails = result.data)
            }
        }.launchIn(viewModelScope)
    }
}